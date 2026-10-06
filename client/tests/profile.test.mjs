import { test } from 'node:test'
import assert from 'node:assert/strict'
import { parseStudentProfile, studentStatusLabels } from '../src/features/student-profile/model.ts'

const profile = {
  studentId: 'SV-001', name: 'Nguyễn Văn An', dateOfBirth: '2005-01-02', gender: 'NAM',
  email: 'student@example.test', phone: '0900000000', className: 'CNTT1', faculty: 'Công nghệ thông tin',
  hometown: 'Hà Nội', roomId: 'A-101', status: 'DANG_O',
}

test('student profile parser keeps the signed-in profile fields and nullable optional details', () => {
  assert.deepEqual(parseStudentProfile(profile), profile)
  assert.equal(parseStudentProfile({ ...profile, dateOfBirth: null, className: null, faculty: null, hometown: null, roomId: null }).roomId, null)
  assert.throws(() => parseStudentProfile({ ...profile, studentId: '' }))
  assert.throws(() => parseStudentProfile({ ...profile, dateOfBirth: 'not-a-date' }))
})

test('profile status labels cover current and former residents', () => {
  assert.equal(studentStatusLabels.DANG_O, 'Đang lưu trú')
  assert.equal(studentStatusLabels.DA_ROI_KTX, 'Đã rời ký túc xá')
})
