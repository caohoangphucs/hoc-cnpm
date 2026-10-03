import { useState } from 'react'
import { Button } from '@/shared/components/Button'
import { InvoiceList } from './InvoiceList'
import { InvoiceDraft } from './InvoiceDraft'
export function InvoiceIssuancePage() {
  const [section, setSection] = useState('invoices')
  return <><div className="wl-segments"><Button variant="outlined" aria-pressed={section === 'invoices'} onClick={() => setSection('invoices')}>Hóa đơn đã phát hành</Button><Button variant="outlined" aria-pressed={section === 'draft'} onClick={() => setSection('draft')}>Lập hóa đơn tháng</Button></div>{section === 'invoices' ? <InvoiceList manage /> : <InvoiceDraft />}</>
}
