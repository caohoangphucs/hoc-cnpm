# React + TypeScript Integration

Đối chiếu 2026-10-02. Nguồn: [React input](https://react.dev/reference/react-dom/components/input), [React 19 custom elements/ref](https://react.dev/blog/2024/12/05/react-19), [React custom elements](https://react.dev/reference/react-dom/components#custom-html-elements).

## Ngữ cảnh client

Đọc package.json/README thay vì khóa một version giả định trong skill. Client hiện dùng React 19, TypeScript, Vite, pnpm và React Router. Alias `@/` trỏ src. Đây là SPA; không thêm SSR, framework khác hoặc state/form library chỉ để dùng M3.

Theme CSS tập trung trong `src/theme/`; component dùng chung trong `src/shared/components/`. Tận dụng API/hook hiện có; skill này không quyết định contract backend, authorization hoặc CSRF.

## React components + tokens

- Dùng native semantics cho link/button/input; styled div không thay button chỉ bằng onClick.
- Props có kiểu, forwarding native props và ref khi có nhu cầu. React 19 hỗ trợ ref như prop; không bắt buộc forwardRef cho component mới chỉ vì ví dụ cũ có nó.
- Variant thể hiện hierarchy, không cố định tên thao tác nghiệp vụ vào shared button.
- CSS example tham chiếu token phải đi kèm theme/style tương ứng trước khi gọi là component hoàn chỉnh.

### Button TSX example

```tsx
import type { ButtonHTMLAttributes, Ref } from 'react'

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  ref?: Ref<HTMLButtonElement>
  variant?: 'filled' | 'tonal' | 'outlined' | 'text'
  pending?: boolean
}

export function Button({
  ref, variant = 'filled', pending = false, disabled,
  type = 'button', className, children, ...props
}: ButtonProps) {
  return (
    <button
      {...props}
      ref={ref}
      type={type}
      disabled={disabled || pending}
      aria-busy={pending || undefined}
      className={['m3-button', `m3-button--${variant}`, className].filter(Boolean).join(' ')}
    >
      {children}
    </button>
  )
}
```

Snippet chỉ minh họa typed/native props. Cần CSS variants/states và phản hồi pending rõ ràng trước khi dùng trong sản phẩm. Disabled pending có thể thay focus behavior; kiểm tra keyboard và thông báo trạng thái. Không mặc định thay nhãn thành icon không có accessible name.

### TextField TSX example

```tsx
import { useId } from 'react'
import type { InputHTMLAttributes, Ref } from 'react'

type TextFieldProps = InputHTMLAttributes<HTMLInputElement> & {
  ref?: Ref<HTMLInputElement>
  label: string
  supportingText?: string
  errorText?: string
}

export function TextField({
  ref, id, label, supportingText, errorText,
  'aria-describedby': describedBy, 'aria-invalid': invalid, ...props
}: TextFieldProps) {
  const generatedId = useId()
  const inputId = id ?? generatedId
  const message = errorText || supportingText
  const messageId = `${inputId}-message`
  const descriptionIds = [describedBy, message ? messageId : undefined].filter(Boolean).join(' ')

  return (
    <div className="m3-field">
      <label htmlFor={inputId}>{label}</label>
      <input
        {...props}
        ref={ref}
        id={inputId}
        aria-invalid={errorText ? true : invalid}
        aria-describedby={descriptionIds || undefined}
      />
      {message && <p id={messageId}>{message}</p>}
    </div>
  )
}
```

Snippet minh họa label/error association; chưa phải đầy đủ visual text-field M3. Nơi gọi chọn controlled (`value` + onChange) hoặc uncontrolled (`defaultValue`) nhất quán. Cần type/name/autocomplete phù hợp; label không thay bằng placeholder. Lỗi async/form summary có thể cần live announcement ngoài field này.

## Dialog và tabs

Native dialog hoặc giải pháp đang dùng cần đồng bộ open/close/cancel với React state và focus. Chỉ đặt `open` trên native dialog không tương đương `showModal()`. Nếu effect gọi showModal, kiểm tra trạng thái trước khi gọi và cleanup để không lỗi khi React StrictMode chạy effect lại. Button action phải liên kết đúng form; modal appearance không tự chứng minh modal semantics.

Tabs cần keyboard/selection/panel relationships; navigation route links không tự động là ARIA tabs. Đọc [navigation](navigation-patterns.md) và [accessibility](accessibility-and-validation.md).

## Material Web khi đã được chọn

React 19 hỗ trợ custom elements; không ép mọi element phải có wrapper. Wrapper vẫn hữu ích để chuẩn hóa event/value, ref và shared props.

- Import đúng từng module và kiểm tra registration ở version cài. Không chỉ thêm tag `md-*` mà chưa đăng ký element.
- Với TypeScript, mở rộng module `react`/JSX.IntrinsicElements cho element cần dùng bằng types cụ thể của thư viện. Không mở toàn bộ JSX thành any hoặc khai báo catch-all để che lỗi.
- Distinguish attributes/properties, boolean props, slot và custom events. Event name giữ đúng case/dash theo source; dùng typed ref + effect listener khi API JSX không biểu diễn phù hợp, kèm cleanup.
- Kiểm tra controlled value, FormData/validation, disabled và event bubbling. Không assume custom input hành xử giống native input ở mọi API.
- CSS bên ngoài shadow DOM cần token/part API hỗ trợ; selector internal DOM không phải contract ổn định.
- SSR có registration/hydration riêng; khả năng hỗ trợ custom elements của React không khiến mọi thư viện DOM-only tự SSR được.
