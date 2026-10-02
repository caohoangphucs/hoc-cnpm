import assert from 'node:assert/strict'
import test from 'node:test'
import { availableBeds, compatible, fileMetadataError, maxFileBytes, validateDocuments } from '../src/features/booking/model.ts'

test('UC-04 only matches the student gender or a TAT_CA building', () => {
  const student = { name: 'Test', code: 'SV', gender: 'NAM' }
  assert.equal(compatible({ gender: 'NAM' }, student), true)
  assert.equal(compatible({ gender: 'NU' }, student), false)
  assert.equal(compatible({ gender: 'TAT_CA' }, student), true)
})
test('held, occupied and locked beds are not available for booking', () => {
  assert.equal(availableBeds({ beds: ['TRONG', 'GIU_CHO', 'DANG_O', 'KHOA', 'TRONG'].map(status => ({ status })) }), 2)
})
test('UC-04 accepts JPG/PNG up to 5 MB and rejects empty, oversized or other files', () => {
  assert.equal(fileMetadataError({ name: 'photo.PNG', type: 'image/png', size: maxFileBytes }), undefined)
  assert.equal(fileMetadataError({ name: 'photo.jpeg', type: 'image/jpeg', size: 1 }), undefined)
  for (const file of [
    { name: 'photo.png', type: 'image/png', size: maxFileBytes + 1 },
    { name: 'photo.png', type: 'image/png', size: 0 },
    { name: 'photo.pdf', type: 'application/pdf', size: 1 },
    { name: 'script.html', type: 'image/png', size: 1 },
  ]) assert.equal(typeof fileMetadataError(file), 'string')
})
test('identity, student card and consent are required; total attachments cannot exceed five', () => {
  const identity = new File(['test'], 'identity.png', { type: 'image/png' })
  const studentCard = new File(['test'], 'student.png', { type: 'image/png' })
  assert.deepEqual(Object.keys(validateDocuments({ identity: null, studentCard: null, evidence: [] }, false)).sort(), ['identity', 'studentCard', 'terms'])
  assert.deepEqual(validateDocuments({ identity, studentCard, evidence: [identity, identity, identity] }, true), {})
  assert.equal(typeof validateDocuments({ identity, studentCard, evidence: [identity, identity, identity, identity] }, true).evidence, 'string')
})
