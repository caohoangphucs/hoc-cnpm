import { Button } from '@/shared/components/Button'
import { useResource } from '@/shared/api/hooks'
import { Facts, Status, WorkflowPage } from '@/shared/workflow/Workflow'
import { parseStudentProfile, studentStatusLabels } from './model'

/** Displays private student details returned only for the authenticated student. */
export function StudentProfilePage() {
  const profile = useResource('students/me', parseStudentProfile)
  return <WorkflowPage
    title="Thông tin cá nhân"
    description="Thông tin hồ sơ sinh viên đang lưu trong hệ thống."
    view={profile.loading ? 'loading' : profile.error ? 'error' : profile.data ? 'data' : 'empty'}
    error={profile.error}
    onRetry={profile.reload}
    actions={<Button variant="outlined" onClick={profile.reload}>Tải lại</Button>}
  >
    {profile.data && <Facts items={[
      ['Mã sinh viên', profile.data.studentId],
      ['Họ và tên', profile.data.name],
      ['Ngày sinh', profile.data.dateOfBirth || 'Chưa cập nhật'],
      ['Giới tính', profile.data.gender === 'NAM' ? 'Nam' : profile.data.gender === 'NU' ? 'Nữ' : profile.data.gender],
      ['Email', profile.data.email],
      ['Số điện thoại', profile.data.phone],
      ['Lớp', profile.data.className || 'Chưa cập nhật'],
      ['Khoa', profile.data.faculty || 'Chưa cập nhật'],
      ['Quê quán', profile.data.hometown || 'Chưa cập nhật'],
      ['Phòng hiện tại', profile.data.roomId || 'Chưa được xếp phòng'],
      ['Trạng thái', <Status tone={profile.data.status === 'DANG_O' ? 'success' : 'neutral'}>{studentStatusLabels[profile.data.status] || profile.data.status}</Status>],
    ]} />}
  </WorkflowPage>
}
