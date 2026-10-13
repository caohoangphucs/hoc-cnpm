import { useState } from 'react'
import { Button } from '@/shared/components/Button'
import { TextField } from '@/shared/components/TextField'
import { Dialog } from '@/shared/components/Dialog'
import { DataTable, Facts, Feedback, Summary } from '@/shared/workflow/Workflow'
import { useApiAction, useResource } from '@/shared/api/hooks'
import { dateTime } from '@/shared/workflow/model'
import { conversionPayload, parseCertificate, parseConversion, parsePoints } from './model'
export function CommunityPoints({ semesterId }: { semesterId: number }) {
  const query = useResource(`activities/points?semesterId=${semesterId}`, parsePoints), action = useApiAction()
  const [amount, setAmount] = useState('1'), [confirm, setConfirm] = useState<'convert' | 'certificate' | null>(null), [notice, setNotice] = useState('')
  const data = query.data, max = data ? Math.max(0, Math.min(5 - data.convertedDrl, Math.floor(data.available / 10))) : 0
  async function submit() {
    if (!data || query.loading || query.error) return
    if (confirm === 'convert') {
      let body: ReturnType<typeof conversionPayload>
      try { body = conversionPayload(semesterId, amount, data) } catch (error) { action.setError((error as Error).message); return }
      const result = await action.run('activities/conversions', body, parseConversion)
      if (result) { setNotice(`Đã gửi yêu cầu ${result.id}: ${result.pointsSpent} điểm công tác xã hội → ${result.drl} điểm rèn luyện. Đang chờ hệ thống rèn luyện xác nhận.`); setConfirm(null); query.reload() }
    } else {
      const result = await action.run('activities/certificates', { semesterId }, parseCertificate)
      if (result) { setNotice(`Yêu cầu giấy xác nhận ${result.id} đang chờ ký và tạo PDF. Chưa có tệp tải về.`); setConfirm(null) }
    }
  }
  if (query.loading) return <section className="wl-card wl-empty" role="status">Đang tải điểm học kỳ…</section>
  if (query.error || !data) return <section className="wl-card community-panel"><Feedback error={query.error} /><Button variant="outlined" onClick={query.reload}>Tải lại điểm</Button></section>
  return <div className="community-points"><Feedback notice={notice} error={confirm ? undefined : action.error} /><Summary items={[{ label: 'Điểm đã nhận', tone: 'success', value: data.earned }, { label: 'Điểm còn lại', value: data.available }, { label: 'Điểm rèn luyện đã đổi / đang chờ', tone: 'tertiary', value: `${data.convertedDrl} / 5` }]} /><section className="wl-card community-panel"><h2>Quy đổi điểm rèn luyện</h2><p>10 điểm công tác xã hội = 1 điểm rèn luyện; tối đa 5 điểm mỗi học kỳ.</p><TextField label="Số điểm rèn luyện muốn quy đổi" type="number" min={1} max={max} step={1} value={amount} onChange={e => setAmount(e.target.value)} /><p className="wl-note">Có thể đổi thêm tối đa {max} điểm. Kết quả vẫn cần hệ thống rèn luyện xác nhận.</p><Button disabled={max === 0 || action.busy} onClick={() => { setConfirm('convert'); action.setError('') }}>Xem & xác nhận quy đổi</Button></section><section className="wl-card community-panel"><h2>Giấy xác nhận công ích</h2><p>Hệ thống chỉ xác nhận các hoạt động đã hoàn thành. Tệp PDF sẽ cần được ký trước khi tải về.</p><Button variant="outlined" disabled={action.busy} onClick={() => { setConfirm('certificate'); action.setError('') }}>Yêu cầu giấy xác nhận</Button></section><section className="wl-card"><div className="wl-card-heading"><h2>Lịch sử điểm trong học kỳ {semesterId}</h2></div><DataTable label="Sổ điểm công ích" head={<tr><th>Thời gian</th><th>Loại giao dịch</th><th>Điểm</th></tr>} rows={data.ledger.map(l => <tr key={l.id}><td>{dateTime(l.time)}</td><td>{({ CONG_DIEM: 'Cộng điểm công tác xã hội', QUY_DOI: 'Quy đổi điểm', HOAN_DIEM: 'Hoàn điểm' } as Record<string, string>)[l.kind] || l.kind}</td><td>{l.delta > 0 ? '+' : ''}{l.delta}</td></tr>)} /></section>{confirm && <Dialog title={confirm === 'convert' ? 'Xác nhận quy đổi' : 'Yêu cầu giấy xác nhận'} busy={action.busy} onClose={() => setConfirm(null)} actions={<><Button variant="text" disabled={action.busy} onClick={() => setConfirm(null)}>Hủy</Button><Button pending={action.busy} onClick={() => void submit()}>Xác nhận</Button></>}><Feedback error={action.error} /><Facts items={[[ 'Học kỳ', semesterId ], ...(confirm === 'convert' ? [['Điểm rèn luyện', amount], ['Điểm công tác xã hội sẽ trừ', Number(amount) * 10]] as [string, string | number][] : [])]} /><p className="wl-note">{confirm === 'convert' ? 'Điểm được trừ khi yêu cầu được chấp nhận; trạng thái quy đổi ban đầu là chờ xác nhận.' : 'Yêu cầu tạo giấy chỉ thành công khi có hoạt động đã hoàn thành trong học kỳ.'}</p></Dialog>}</div>
}
