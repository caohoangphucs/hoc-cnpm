import { parseMaterials } from './materials'
import { useState } from 'react'
import { Button } from '@/shared/components/Button'
import { Dialog } from '@/shared/components/Dialog'
import { TextField } from '@/shared/components/TextField'
import { DataTable, Feedback, WorkflowPage } from '@/shared/workflow/Workflow'
import { useApiAction, useResource } from '@/shared/api/hooks'
import { command } from '@/shared/api/protocol'
import { searchText } from '@/shared/workflow/model'

/** Kho dùng dữ liệu thật, tồn kho thay đổi theo xuất vật tư trong nghiệp vụ sửa chữa. */
export function MaterialsPage() {
  const resource = useResource('materials', parseMaterials)
  const action = useApiAction()
  const [search, setSearch] = useState(''), [creating, setCreating] = useState(false), [error, setError] = useState('')
  const [id, setId] = useState(''), [name, setName] = useState(''), [unit, setUnit] = useState(''), [stock, setStock] = useState(0), [price, setPrice] = useState(0)
  const shown = (resource.data ?? []).filter(m => searchText(`${m.id} ${m.name}`).includes(searchText(search)))

  async function save() {
    if (!/^[A-Za-z0-9_-]{1,20}$/.test(id) || !name.trim() || name.trim().length > 100 || !unit.trim() || unit.trim().length > 20 || !Number.isInteger(stock) || stock < 0 || stock > 2147483647 || !Number.isSafeInteger(price) || price < 0 || price > 999999999999999) {
      setError('Kiểm tra mã, tên, đơn vị và số lượng/đơn giá không âm.'); return
    }
    setError('')
    if (await action.run('materials', { id, name: name.trim(), unit: unit.trim(), stock, price }, command)) { setCreating(false); resource.reload() }
  }

  return <><WorkflowPage title="Kho vật tư" description="Danh mục và tồn kho thực tế để sử dụng khi nghiệm thu sửa chữa." view={resource.loading ? 'loading' : resource.error ? 'error' : 'data'} error={resource.error} onRetry={resource.reload} actions={<div className="wl-actions"><Button onClick={() => { setId(''); setName(''); setUnit(''); setStock(0); setPrice(0); setError(''); setCreating(true) }}>Thêm vật tư</Button><Button variant="outlined" onClick={resource.reload}>Tải lại</Button></div>}>
    <section className="wl-card"><div className="wl-toolbar"><TextField label="Tìm vật tư" type="search" value={search} onChange={e => setSearch(e.target.value)} /></div><DataTable label="Danh mục vật tư" head={<tr><th>Mã</th><th>Tên vật tư</th><th>Đơn vị</th><th>Tồn kho</th><th>Đơn giá nhập</th></tr>} rows={shown.map(m => <tr key={m.id}><td>{m.id}</td><td>{m.name}</td><td>{m.unit}</td><td>{m.stock}</td><td>{m.price.toLocaleString('vi-VN')} đ</td></tr>)} /></section>
  </WorkflowPage>{creating && <Dialog title="Thêm vật tư" busy={action.busy} onClose={() => setCreating(false)} actions={<><Button variant="text" disabled={action.busy} onClick={() => setCreating(false)}>Hủy</Button><Button pending={action.busy} onClick={() => void save()}>Lưu vật tư</Button></>}><Feedback error={action.error || error} /><div className="wl-form"><TextField label="Mã vật tư" value={id} maxLength={20} onChange={e => setId(e.target.value)} /><TextField label="Tên vật tư" value={name} maxLength={100} onChange={e => setName(e.target.value)} /><TextField label="Đơn vị tính" value={unit} maxLength={20} onChange={e => setUnit(e.target.value)} /><TextField label="Tồn kho ban đầu" type="number" min={0} step={1} value={stock} onChange={e => setStock(e.target.valueAsNumber)} /><TextField label="Đơn giá nhập" type="number" min={0} step={1} value={price} onChange={e => setPrice(e.target.valueAsNumber)} /></div></Dialog>}</>
}
