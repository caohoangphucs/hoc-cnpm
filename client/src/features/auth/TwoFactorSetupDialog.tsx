import { useState } from 'react'
import { Button } from '@/shared/components/Button'
import { Dialog } from '@/shared/components/Dialog'
import { TextField } from '@/shared/components/TextField'
import { Feedback } from '@/shared/workflow/Workflow'
import { useApiAction } from '@/shared/api/hooks'
import { object, text } from '@/shared/api/protocol'
import { parseSession } from './model'
import { useAuth } from './context'
function parseEnrollment(value: unknown) { const v = object(value); return { secret: text(v.secret), provisioningUri: text(v.provisioningUri) } }
export function TwoFactorSetupDialog({ onClose }: { onClose: () => void }) {
  const [setup, setSetup] = useState<ReturnType<typeof parseEnrollment> | null>(null), [code, setCode] = useState(''), [saved, setSaved] = useState(false)
  const action = useApiAction(), auth = useAuth()
  async function begin() { const result = await action.run('auth/2fa/setup', undefined, parseEnrollment); if (result) setSetup(result) }
  async function confirm() {
    if (!/^\d{6}$/.test(code)) { action.setError('Nhập mã gồm 6 chữ số từ ứng dụng xác thực.'); return }
    if (await action.run('auth/2fa/setup/confirm', { code }, parseSession)) { setSetup(null); setCode(''); setSaved(true); await auth.refresh().catch(() => {}) }
  }
  return <Dialog title="Thiết lập ứng dụng xác thực" onClose={onClose} busy={action.busy} actions={<><Button variant="text" onClick={onClose} disabled={action.busy}>Đóng</Button>{!saved && <Button type={setup ? 'submit' : 'button'} form={setup ? 'totp-setup' : undefined} pending={action.busy} onClick={setup ? undefined : () => void begin()}>{setup ? 'Xác nhận thiết lập' : 'Bắt đầu thiết lập'}</Button>}</>}><Feedback error={action.error} notice={saved ? 'Đã lưu ứng dụng xác thực.' : undefined} />{setup ? <form id="totp-setup" className="wl-form" onSubmit={e => { e.preventDefault(); void confirm() }}><p>Thêm tài khoản TOTP trong ứng dụng xác thực bằng khóa dưới đây. Chỉ hoàn tất khi mã được hệ thống xác nhận.</p><TextField label="Khóa thiết lập" value={setup.secret} readOnly autoComplete="off" /><TextField label="URI nhập vào ứng dụng xác thực" value={setup.provisioningUri} readOnly autoComplete="off" /><TextField label="Mã xác thực 6 chữ số" value={code} disabled={action.busy} onChange={e => setCode(e.target.value)} inputMode="numeric" autoComplete="one-time-code" maxLength={6} required /><p className="wl-note">Giữ kín khóa thiết lập. Khóa chỉ hiển thị trong hộp thoại này và không lưu vào trình duyệt.</p></form> : !saved && <p>Dành cho tài khoản chưa thiết lập ứng dụng xác thực. Nếu tài khoản đã có ứng dụng, hệ thống sẽ giữ cấu hình hiện tại.</p>}</Dialog>
}
