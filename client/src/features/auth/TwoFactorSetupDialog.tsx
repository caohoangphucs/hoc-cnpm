import { useState } from 'react'
import { QRCodeSVG } from 'qrcode.react'
import { Button } from '@/shared/components/Button'
import { Dialog } from '@/shared/components/Dialog'
import { TextField } from '@/shared/components/TextField'
import { Feedback } from '@/shared/workflow/Workflow'
import { useApiAction } from '@/shared/api/hooks'
import { object, text } from '@/shared/api/protocol'
import { parseSession } from './model'
import { useAuth } from './context'

function parseEnrollment(value: unknown) {
  const enrollment = object(value)
  return { provisioningUri: text(enrollment.provisioningUri) }
}

export function TwoFactorSetupDialog({ onClose }: { onClose: () => void }) {
  const [setup, setSetup] = useState<ReturnType<typeof parseEnrollment> | null>(null)
  const [code, setCode] = useState('')
  const [saved, setSaved] = useState(false)
  const action = useApiAction()
  const auth = useAuth()

  // Encode the enrollment URI locally; the QR contains the same key used by the backend.
  async function begin() {
    const result = await action.run('auth/2fa/setup', undefined, parseEnrollment)
    if (result) setSetup(result)
  }

  // Keep enrollment pending until the backend verifies a code from the authenticator app.
  async function confirm() {
    if (!/^\d{6}$/.test(code)) {
      action.setError('Nhập mã gồm 6 chữ số từ ứng dụng xác thực.')
      return
    }

    if (await action.run('auth/2fa/setup/confirm', { code }, parseSession)) {
      setSetup(null)
      setCode('')
      setSaved(true)
      await auth.refresh().catch(() => {})
    }
  }

  return (
    <Dialog
      title="Thiết lập ứng dụng xác thực"
      onClose={onClose}
      busy={action.busy}
      actions={
        <>
          <Button variant="text" onClick={onClose} disabled={action.busy}>Đóng</Button>
          {!saved && (
            <Button
              type={setup ? 'submit' : 'button'}
              form={setup ? 'totp-setup' : undefined}
              pending={action.busy}
              onClick={setup ? undefined : () => void begin()}
            >
              {setup ? 'Xác nhận thiết lập' : 'Bắt đầu thiết lập'}
            </Button>
          )}
        </>
      }
    >
      <Feedback error={action.error} notice={saved ? 'Đã lưu ứng dụng xác thực.' : undefined} />
      {setup ? (
        <form id="totp-setup" className="wl-form" onSubmit={event => { event.preventDefault(); void confirm() }}>
          <p>Mở Google Authenticator, nhấn dấu +, chọn “Quét mã QR” và quét mã bên dưới.</p>
          <div className="auth-setup-qr">
            <QRCodeSVG
              value={setup.provisioningUri}
              size={240}
              level="M"
              marginSize={4}
              bgColor="#ffffff"
              fgColor="#000000"
              title="Mã QR thiết lập Google Authenticator"
              role="img"
              aria-label="Mã QR thiết lập Google Authenticator"
            />
          </div>
          <TextField
            label="Mã xác thực 6 chữ số"
            value={code}
            disabled={action.busy}
            onChange={event => setCode(event.target.value)}
            inputMode="numeric"
            autoComplete="one-time-code"
            maxLength={6}
            required
          />
          <p className="wl-note">Nhập mã từ ứng dụng để hoàn tất thiết lập. Giữ kín mã QR này.</p>
        </form>
      ) : !saved && (
        <p>Dành cho tài khoản chưa thiết lập ứng dụng xác thực. Nếu tài khoản đã có ứng dụng, hệ thống sẽ giữ cấu hình hiện tại.</p>
      )}
    </Dialog>
  )
}
