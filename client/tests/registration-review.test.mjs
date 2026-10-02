import test from 'node:test'
import assert from 'node:assert/strict'
import {approvalIssue} from '../src/features/registration-review/model.ts'
test('UC-12 requires a pending unexpired hold, available profile and reviewer verification',()=>{
 const now=Date.parse('2026-10-03T08:00:00Z'), valid={status:'CHO_DUYET',expiresAt:new Date(now+1).toISOString(),bedStatus:'GIU_CHO',profile:'VALID',verified:true}
 assert.equal(approvalIssue(valid,now),'')
 for(const patch of [{status:'DA_DUYET'},{expiresAt:new Date(now).toISOString()},{bedStatus:'KHOA'},{bedStatus:'DANG_O'},{profile:'MISSING'},{verified:false}])assert.notEqual(approvalIssue({...valid,...patch},now),'')
})
