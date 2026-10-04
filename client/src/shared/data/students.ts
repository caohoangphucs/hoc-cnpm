export type StudentInfo = {
  id: string
  name: string
  fullName?: string
  room?: string
  faculty?: string
  classId?: string
}

export const sampleStudents: readonly StudentInfo[] = [
  { id: '21110001', name: 'Trần Hữu Đạt', room: 'A-102', classId: '21110B', faculty: 'Công Nghệ Phần Mềm' },
  { id: '21110002', name: 'Lê Thu Linh', room: 'B-103', classId: '21110C', faculty: 'Hệ Thống Thông Tin' },
  { id: '21110003', name: 'Phạm Hoàng Huy', room: 'A-101', classId: '21110D', faculty: 'Khoa Học Máy Tính' },
  { id: '21110004', name: 'Hoàng Hồng Mai', room: 'B-102', classId: '21110E', faculty: 'Kỹ Thuật Máy Tính' },
  { id: '21110005', name: 'Huỳnh Quốc Nam', room: 'A-103', classId: '21110A', faculty: 'An Toàn Thông Tin' },
  { id: '21110006', name: 'Phan Phương Hương', room: 'B-101', classId: '21110B', faculty: 'Công Nghệ Thông Tin' },
  { id: '21110007', name: 'Vũ Thành Bảo', room: 'A-102', classId: '21110C', faculty: 'Công Nghệ Phần Mềm' },
  { id: '21110008', name: 'Võ Bảo Hà', room: 'B-103', classId: '21110D', faculty: 'Hệ Thống Thông Tin' },
  { id: '21110009', name: 'Đặng Công Dũng', room: 'A-101', classId: '21110E', faculty: 'Khoa Học Máy Tính' },
  { id: '21110010', name: 'Bùi Thị Trâm', room: 'B-102', classId: '21110A', faculty: 'Kỹ Thuật Máy Tính' },
  { id: '21110011', name: 'Đỗ Hữu Phong', room: 'A-103', classId: '21110B', faculty: 'An Toàn Thông Tin' },
  { id: '21110012', name: 'Hồ Thu Quyên', room: 'B-101', classId: '21110C', faculty: 'Công Nghệ Thông Tin' },
  { id: '21110013', name: 'Ngô Hoàng Khôi', room: 'A-102', classId: '21110D', faculty: 'Công Nghệ Phần Mềm' },
  { id: '21110014', name: 'Dương Hồng Yến', room: 'B-103', classId: '21110E', faculty: 'Hệ Thống Thông Tin' },
  { id: '21110015', name: 'Nguyễn Quốc Phúc', room: 'A-101', classId: '21110A', faculty: 'Khoa Học Máy Tính' },
  { id: '21110016', name: 'Trần Phương Trang', room: 'B-102', classId: '21110B', faculty: 'Kỹ Thuật Máy Tính' },
  { id: '21110017', name: 'Lê Thành Khoa', room: 'A-103', classId: '21110C', faculty: 'An Toàn Thông Tin' },
  { id: '21110018', name: 'Phạm Bảo Lan', room: 'B-101', classId: '21110D', faculty: 'Công Nghệ Thông Tin' },
  { id: '21110019', name: 'Hoàng Công Tùng', room: 'A-102', classId: '21110E', faculty: 'Công Nghệ Phần Mềm' },
  { id: '21110020', name: 'Huỳnh Thị Thảo', room: 'B-103', classId: '21110A', faculty: 'Hệ Thống Thông Tin' },
  { id: '24110123', name: 'Nguyễn Phúc An', room: 'A-101', classId: '24110A', faculty: 'Công nghệ thông tin' },
  { id: '24110456', name: 'Trần Minh Châu', room: 'A-101', classId: '24110B', faculty: 'Công nghệ thông tin' },
  { id: '24110889', name: 'Lê Hoàng Nam', room: 'A-101', classId: '24110C', faculty: 'Cơ khí' },
  { id: '23110045', name: 'Trần Minh Châu', room: 'A-101', classId: '23110A', faculty: 'Công nghệ thông tin' },
  { id: '24110321', name: 'Lê Hoàng Nam', room: 'A-101', classId: '24110B', faculty: 'Cơ khí' },
  { id: '23110412', name: 'Đỗ Hoàng Anh', room: 'A-101', classId: '23110B', faculty: 'Điện - Điện tử' },
  { id: '24110915', name: 'Vũ Đức Long', room: 'A-101', classId: '24110C', faculty: 'Công nghệ thông tin' },
]

export function searchStudents(query: string): StudentInfo[] {
  const q = query.trim().toLowerCase()
  if (!q) return sampleStudents.slice(0, 10)
  return sampleStudents.filter(
    (s) =>
      s.id.toLowerCase().includes(q) ||
      s.name.toLowerCase().includes(q) ||
      (s.room && s.room.toLowerCase().includes(q)) ||
      (s.faculty && s.faculty.toLowerCase().includes(q)) ||
      (s.classId && s.classId.toLowerCase().includes(q)),
  ).slice(0, 15)
}

export function findStudent(idOrQuery: string): StudentInfo | undefined {
  const q = idOrQuery.trim().toLowerCase()
  const found = sampleStudents.find(s => s.id.toLowerCase() === q)
  if (found) return { ...found, fullName: found.name }
  return undefined
}
