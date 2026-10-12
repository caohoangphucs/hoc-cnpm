import type { ComponentType } from 'react'

// định dạng module route được xuất từ các tính năng
export type FeaturePageModule = { route: string; default: ComponentType }

// gom các module tính năng thành bảng tra cứu route -> component
export function featurePages(modules: Record<string, FeaturePageModule>) {
  return Object.fromEntries(Object.values(modules).map(module => [module.route, module.default]))
}
