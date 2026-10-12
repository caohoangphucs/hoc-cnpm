// đường dẫn api cơ sở lấy từ biến môi trường hoặc mặc định /api/v1
const baseUrl = (import.meta.env.VITE_API_BASE_URL || '/api/v1').replace(/\/$/, '')

// cấu hình tùy chọn gửi request kèm token csrf
export type ApiRequestOptions = RequestInit & { csrfToken?: string }

// Trả Response để từng tính năng xử lý JSON, file hoặc response không có body.
// hàm gọi api chuẩn của ứng dụng: tự gắn cookie và header csrf
export async function apiFetch(path: string, options: ApiRequestOptions = {}) {
  const { csrfToken, headers: initialHeaders, ...requestOptions } = options
  const headers = new Headers(initialHeaders)
  headers.set('Accept', 'application/json')

  // đính kèm token csrf để bảo vệ các thao tác thay đổi dữ liệu
  if (csrfToken) {
    headers.set('X-CSRF-TOKEN', csrfToken)
  }

  // gửi request fetch với credentials include để giữ cookie phiên đăng nhập
  const response = await fetch(`${baseUrl}/${path.replace(/^\/+/, '')}`, {
    ...requestOptions,
    headers,
    credentials: 'include',
  })
  // phát sự kiện hết hạn phiên khi nhận mã lỗi 401
  if (response.status === 401 && !path.replace(/^\/+/, '').startsWith('auth/')) {
    window.dispatchEvent(new Event('ktx:unauthorized'))
  }
  return response
}
