import wordmark from '@/assets/ktx-smart-hub-logo.png'

export function BrandLogo({ symbol = false }: { symbol?: boolean }) {
  // Frame the visible artwork without changing the supplied transparent PNGs.
  const image = symbol ? `${import.meta.env.BASE_URL}favicon.png` : wordmark
  return (
    <svg
      className={`brand-logo${symbol ? ' brand-logo--symbol' : ''}`}
      viewBox={symbol ? '71 258 1130 825' : '95 182 1987 364'}
      aria-hidden="true"
      focusable="false"
    >
      <image href={image} width={symbol ? 1254 : 2172} height={symbol ? 1254 : 724} />
    </svg>
  )
}
