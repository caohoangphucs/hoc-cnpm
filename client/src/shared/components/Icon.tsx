const paths = {
  home: 'M3 10 12 3l9 7v10h-6v-7H9v7H3V10Z',
  building: 'M4 21V3h12v18M16 9h4v12M2 21h20M8 7h4M8 11h4M8 15h4M8 21v-2h4v2',
  document: 'M14 2H5v20h14V7l-5-5Zm0 0v5h5M8 12h8M8 16h8',
  wallet: 'M20 8V5H5a3 3 0 0 0 0 6h15v9H5a3 3 0 0 1-3-3V8m18 3h2v5h-6v-5h4Zm-2 2.5h.01',
  tool: 'm14 6 4 4 4-4a7 7 0 0 1-9 9L6 22l-4-4 7-7a7 7 0 0 1 9-9l-4 4Z',
  people: 'M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2m20 0v-2a4 4 0 0 0-3-3.87M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm8-7a4 4 0 0 1 0 8',
  community: 'M12 21 3 12a5.6 5.6 0 0 1 9-7 5.6 5.6 0 0 1 9 7l-9 9Z',
  bell: 'M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9ZM10 21h4',
  pricing: 'M20 13 11 22 2 13V2h11l9 9v2ZM7 7h.01M10 14l4-4',
  chart: 'M3 3v18h18M7 16v-4M12 16V7M17 16v-7',
  box: 'm12 3 9 5-9 5-9-5 9-5Zm-9 5v9l9 5 9-5V8M12 13v9M7.5 5.5l9 5',
  meter: 'M4 19a10 10 0 1 1 16 0H4ZM12 13l4-5M6 10l1 1M12 5v2M17 11l1-1',
  shield: 'M12 2 3 6v6c0 5 9 10 9 10s9-5 9-10V6l-9-4ZM12 7v6M12 17h.01',
  gate: 'M4 21V3h16v18M9 21V7h6v14M2 21h20M12 11h.01',
  logs: 'M4 3h16v18H4V3ZM8 7h8M8 12h8M8 17h5',
  menu: 'M4 6h16M4 12h16M4 18h16',
  close: 'm6 6 12 12M6 18 18 6',
  arrow: 'M5 12h14m-6-6 6 6-6 6',
  back: 'M19 12H5m6-6-6 6 6 6',
  more: 'M5 12h.01M12 12h.01M19 12h.01',
  moon: 'M21 13a9 9 0 1 1-10-10 7 7 0 0 0 10 10Z',
  sun: 'M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8ZM12 2v2M12 20v2M2 12h2M20 12h2M5 5l1.5 1.5M17.5 17.5 19 19M5 19l1.5-1.5M17.5 6.5 19 5',
  lock: 'M7 11V7a5 5 0 0 1 10 0v4M5 11h14v11H5V11ZM12 15v3',
  eye: 'M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12ZM12 9a3 3 0 1 0 0 6 3 3 0 0 0 0-6Z',
  eyeOff: 'M3 3l18 18M10.5 5.1A12 12 0 0 1 12 5c6.5 0 10 7 10 7a20 20 0 0 1-3 4M6 6a20 20 0 0 0-4 6s3.5 7 10 7a12 12 0 0 0 5-1M10 10a3 3 0 0 0 4 4',
  logout: 'M9 3H3v18h6M8 12h13m-5-5 5 5-5 5',
  info: 'M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20ZM12 11v6M12 7h.01',
  search: 'M10.5 3a7.5 7.5 0 1 0 0 15 7.5 7.5 0 0 0 0-15ZM16 16l5 5',
  refresh: 'M20 7v5h-5M4 17v-5h5M6 7a8 8 0 0 1 14 5M18 17A8 8 0 0 1 4 12',
  check: 'm5 12 4 4L19 6',
  unlock: 'M7 11V7a5 5 0 0 1 9-3M5 11h14v11H5V11ZM12 15v3',
  sortUp: 'M12 19V5m-5 5 5-5 5 5',
  sortDown: 'M12 5v14m-5-5 5 5 5-5',
  bed: 'M3 18V6m0 8h18v4M3 10h6v4M9 10h10a2 2 0 0 1 2 2v2M3 18v3m18-3v3',
} as const

export type IconName = keyof typeof paths

// Các path filled lấy từ SVG trong hai HTML mẫu người dùng cung cấp.
// Giữ path nét cho các biểu tượng thao tác và nhóm chưa có trong mẫu.
const filledPaths: Partial<Record<IconName, string>> = {
  home: 'M12 3L2 12h3v8h6v-6h2v6h6v-8h3L12 3zm0 2.84L17.5 10v8h-3v-6h-5v6h-3v-8L12 5.84z',
  building: 'M17 11V3H7v4H3v14h8v-4h2v4h8V11h-4zM7 19H5v-2h2v2zm0-4H5v-2h2v2zm0-4H5V9h2v2zm4 4H9v-2h2v2zm0-4H9V9h2v2zm0-4H9V5h2v2zm4 8h-2v-2h2v2zm0-4h-2V9h2v2zm0-4h-2V5h2v2zm4 12h-2v-2h2v2zm0-4h-2v-2h2v2z',
  document: 'M19.5 3.5L18 2l-1.5 1.5L15 2l-1.5 1.5L12 2l-1.5 1.5L9 2 7.5 3.5 6 2 4.5 3.5 3 2v20l1.5-1.5L6 22l1.5-1.5L9 22l1.5-1.5L12 22l1.5-1.5L15 22l1.5-1.5L18 22l1.5-1.5L21 22V2l-1.5 1.5zM15 17H6v-2h9v2zm3-4H6v-2h12v2zm0-4H6V7h12v2z',
  wallet: 'M19 14V6c0-1.1-.9-2-2-2H3c-1.1 0-2 .9-2 2v8c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zm-9-1c-1.66 0-3-1.34-3-3s1.34-3 3-3 3 1.34 3 3-1.34 3-3 3zm13-6v11c0 1.1-.9 2-2 2H4v-2h17V7h2z',
  tool: 'M22.7 19l-9.1-9.1c.9-2.3.4-5-1.5-6.9-2-2-5-2.4-7.4-1.3L9 6 6 9 1.6 4.7C.4 7.1.9 10.1 2.9 12.1c1.9 1.9 4.6 2.4 6.9 1.5l9.1 9.1c.4.4 1 .4 1.4 0l2.3-2.3c.5-.4.5-1.1.1-1.4z',
  people: 'M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z',
  community: 'M19 4h-1V2h-2v2H8V2H6v2H5c-1.11 0-1.99.9-1.99 2L3 20c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V10h14v10zm0-12H5V6h14v2z',
  bell: 'M12 22c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.64 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2zm-2 1H8v-6c0-2.48 1.51-4.5 4-4.5s4 2.02 4 4.5v6z',
  gate: 'M19 19V4h-4V3H5v16H3v2h12V6h2v15h4v-2h-2zm-6 0H7V5h6v14zm-3-8h2v2h-2z',
  moon: 'M12 3c-4.97 0-9 4.03-9 9s4.03 9 9 9 9-4.03 9-9c0-.46-.04-.92-.1-1.36-.98 1.37-2.58 2.26-4.4 2.26-2.98 0-5.4-2.42-5.4-5.4 0-1.81.89-3.42 2.26-4.4-.44-.06-.9-.1-1.36-.1z',
  bed: 'M19 7h-8v8H3V5H1v15h2v-3h18v3h2v-9c0-2.21-1.79-4-4-4zm1 8h-7V9h7c1.1 0 2 .9 2 2v4zM7 13c1.66 0 3-1.34 3-3S8.66 7 7 7s-3 1.34-3 3 1.34 3 3 3z',
}

export function Icon({ name, className = '' }: { name: IconName; className?: string }) {
  const filled = filledPaths[name]
  return (
    <svg
      className={`icon ${className}`}
      viewBox="0 0 24 24"
      fill={filled ? 'currentColor' : 'none'}
      stroke={filled ? 'none' : 'currentColor'}
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      focusable="false"
    >
      <path d={filled ?? paths[name]} />
    </svg>
  )
}
