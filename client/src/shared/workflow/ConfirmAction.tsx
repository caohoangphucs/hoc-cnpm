import { Button } from '@/shared/components/Button'
import { Dialog } from '@/shared/components/Dialog'
import { useApiAction } from '@/shared/api/hooks'
import { command } from '@/shared/api/protocol'
import { Feedback } from './Workflow'

export function ConfirmAction({ title, description, path, body, method = 'DELETE', label = 'Xác nhận', onClose, onSave }: { title: string; description: string; path: string; body?: unknown; method?: string; label?: string; onClose: () => void; onSave: () => void }) {
  const action = useApiAction()
  return <Dialog title={title} onClose={onClose} busy={action.busy} actions={<><Button variant="text" onClick={onClose} disabled={action.busy}>Hủy</Button><Button pending={action.busy} onClick={async () => { if (await action.run(path, body, command, method)) onSave() }}>{label}</Button></>}><p>{description}</p><Feedback error={action.error} /></Dialog>
}
