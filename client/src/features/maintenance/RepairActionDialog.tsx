import { useId, useState } from 'react'
import { Button } from '@/shared/components/Button'
import { Dialog } from '@/shared/components/Dialog'
import { TextField } from '@/shared/components/TextField'
import { Feedback, NotesField, SelectField } from '@/shared/workflow/Workflow'
import { useApiAction } from '@/shared/api/hooks'
import { command } from '@/shared/api/protocol'
import { parseCompletion, priorityLabels, repairPayload, type Repair, type SupplyLine, type Priority } from './model'
export function RepairActionDialog({ repair, mode, onClose, onSave }: { repair: Repair; mode: 'schedule' | 'complete'; onClose: () => void; onSave: (message: string) => void }) {
  const action = useApiAction(), form = useId()
  const [note,setNote]=useState(''), [schedule,setSchedule]=useState(''), [warranty,setWarranty]=useState(''), [supplies,setSupplies]=useState<SupplyLine[]>([]), [errors,setErrors]=useState<Record<string,string>>({}), [shortage,setShortage]=useState<string[]>([])
  async function submit() {
    const {errors,body}=repairPayload(mode,repair.version,note,schedule,warranty,supplies);setErrors(errors)
    if(Object.keys(errors).length) return
    if(mode==='schedule') { const result=await action.run(`repairs/${repair.id}/schedule`,body,command,'PUT'); if(result) onSave('Đã lưu lịch xử lý.'); }
    else { const result=await action.run(`repairs/${repair.id}/complete`,body,parseCompletion,'POST',true); if(result?.status==='HOAN_THANH') onSave('Đã hoàn thành phiếu và ghi nhận xuất vật tư.'); else if(result) setShortage(result.shortages) }
  }
  function changeLine(index:number,patch:Partial<SupplyLine>) { setSupplies(s=>s.map((line,i)=>i===index?{...line,...patch}:line));setShortage([]) }
  return <Dialog title={mode==='schedule'?'Đổi lịch sửa chữa':'Hoàn thành sửa chữa'} busy={action.busy} onClose={onClose} actions={<><Button variant="text" disabled={action.busy} onClick={onClose}>Quay lại</Button><Button type="submit" form={form} pending={action.busy} disabled={shortage.length>0}>{mode==='schedule'?'Lưu lịch':'Xác nhận hoàn thành'}</Button></>}><p className="repair-dialog-intro">SC-{repair.id} · Phòng {repair.roomId}<br />{repair.title}</p><Feedback error={action.error} /><form id={form} className="wl-form repair-form" noValidate onSubmit={e=>{e.preventDefault();void submit()}}><fieldset className="repair-fieldset" disabled={action.busy}>
    {mode==='schedule' && <TextField label="Lịch sửa chữa (giờ Việt Nam)" type="datetime-local" value={schedule} onChange={e=>setSchedule(e.target.value)} required errorText={errors.schedule} />}
    <NotesField label={mode==='complete'?'Nội dung đã sửa chữa':'Lý do đổi lịch'} value={note} onChange={e=>setNote(e.target.value)} required error={errors.note} />
    {mode==='schedule' && <p className="wl-note">Chưa tra cứu được hạn bảo hành hiện tại. Nhập lại ngày cần giữ; để trống sẽ bỏ hạn bảo hành khi lưu lịch mới.</p>}
    <TextField label="Hạn bảo hành (nếu có)" type="date" value={warranty} onChange={e=>setWarranty(e.target.value)} errorText={errors.warrantyUntil} />
    {mode==='complete' && <section className="wl-form"><h3>Vật tư đã dùng</h3><p>Chỉ nhập vật tư thực tế. Nếu không dùng vật tư, để trống danh sách.</p>{supplies.map((s,i)=><div key={i} className="repair-supply-row"><TextField label={`Mã vật tư ${i+1}`} value={s.supplyId} onChange={e=>changeLine(i,{supplyId:e.target.value})} /><TextField label={`Số lượng ${i+1}`} type="number" min={1} max={2147483647} step={1} value={Number.isNaN(s.quantity)?'':s.quantity} onChange={e=>changeLine(i,{quantity:e.target.valueAsNumber})} /><Button variant="text" onClick={()=>{setSupplies(v=>v.filter((_,j)=>j!==i));setShortage([])}}>Xóa dòng {i+1}</Button></div>)}{errors.supplies && <p className="wl-error" role="alert">{errors.supplies}</p>}<Button variant="outlined" disabled={supplies.length>=100} onClick={()=>{setSupplies(v=>[...v,{supplyId:'',quantity:1}]);setShortage([])}}>Thêm vật tư</Button>{shortage.length>0 && <div className="repair-shortage" role="status"><h3>Đã đề nghị mua vật tư thiếu</h3><p>Mã vật tư: {shortage.join(', ')}. Phiếu vẫn đang xử lý; chưa xuất bất kỳ vật tư nào.</p><p>Điều chỉnh vật tư thực tế hoặc chờ kho bổ sung rồi tải lại phiếu.</p></div>}</section>}
  </fieldset></form></Dialog>
}
export function ReportRepairDialog({onClose,onSave}:{onClose:()=>void;onSave:()=>void}) {
  const action=useApiAction(), form=useId();const [title,setTitle]=useState(''),[description,setDescription]=useState(''),[priority,setPriority]=useState<Priority>('TRUNG_BINH'),[error,setError]=useState('')
  async function submit(){if(!title.trim()||title.trim().length>1000||!description.trim()||description.trim().length>1000){setError('Nhập tiêu đề và mô tả từ 1 đến 1.000 ký tự.');return}setError('');const result=await action.run('repairs',{title:title.trim(),description:description.trim(),priority,fileIds:[]},command);if(result)onSave()}
  return <Dialog title="Báo sự cố trong phòng" busy={action.busy} onClose={onClose} actions={<><Button variant="text" disabled={action.busy} onClick={onClose}>Hủy</Button><Button type="submit" form={form} pending={action.busy}>Gửi yêu cầu</Button></>}><Feedback error={action.error||error} /><form id={form} className="wl-form repair-form" noValidate onSubmit={e=>{e.preventDefault();void submit()}}><fieldset className="repair-fieldset" disabled={action.busy}><TextField label="Tiêu đề sự cố" value={title} maxLength={1000} required onChange={e=>setTitle(e.target.value)} /><NotesField label="Mô tả sự cố" value={description} required onChange={e=>setDescription(e.target.value)} /><SelectField label="Mức ưu tiên" value={priority} onChange={e=>setPriority(e.target.value as Priority)}>{Object.entries(priorityLabels).map(([v,l])=><option key={v} value={v}>{l}</option>)}</SelectField><p className="wl-note">Phiếu được gắn với phòng lưu trú của bạn. Chức năng đính kèm ảnh chưa sẵn sàng.</p></fieldset></form></Dialog>
}
