import { PreviewWorkflow } from '@/shared/workflow/Workflow'
import { RegistrationReviewPage } from './RegistrationReviewPage'
import type { Registration } from './model'
function fixtures(): Registration[] { const now=Date.now();return [
  {id:'DK-DEMO-101',student:'Nguyễn Minh An',studentCode:'SV-DEMO-101',room:'A101',bed:'01',submittedAt:new Date(now-3600000).toISOString(),expiresAt:new Date(now+47*3600000).toISOString(),priority:1,profile:'VALID',verified:true,bedStatus:'GIU_CHO',status:'CHO_DUYET'},
  {id:'DK-DEMO-102',student:'Trần Khánh Vy',studentCode:'SV-DEMO-102',room:'B201',bed:'02',submittedAt:new Date(now-7200000).toISOString(),expiresAt:new Date(now+46*3600000).toISOString(),priority:2,profile:'VALID',verified:false,bedStatus:'GIU_CHO',status:'CHO_DUYET'},
  {id:'DK-DEMO-103',student:'Lê Quốc Bảo',studentCode:'SV-DEMO-103',room:'A201',bed:'03',submittedAt:new Date(now-3600000).toISOString(),expiresAt:new Date(now+47*3600000).toISOString(),priority:2,profile:'MISSING',verified:false,bedStatus:'GIU_CHO',status:'CHO_DUYET'},
  {id:'DK-DEMO-104',student:'Phạm Thu Hà',studentCode:'SV-DEMO-104',room:'B101',bed:'01',submittedAt:new Date(now-49*3600000).toISOString(),expiresAt:new Date(now-3600000).toISOString(),priority:1,profile:'VALID',verified:true,bedStatus:'GIU_CHO',status:'CHO_DUYET'},
  {id:'DK-DEMO-105',student:'Võ Hoàng Nam',studentCode:'SV-DEMO-105',room:'A102',bed:'02',submittedAt:new Date(now-3600000).toISOString(),expiresAt:new Date(now+47*3600000).toISOString(),priority:3,profile:'VALID',verified:true,bedStatus:'KHOA',status:'CHO_DUYET'},
] }
export default function RegistrationReviewPreview() { return <PreviewWorkflow title="UC-12 · Duyệt hồ sơ">{options=><RegistrationReviewPage {...options} records={fixtures()} />}</PreviewWorkflow> }
