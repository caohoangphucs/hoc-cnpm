import type { ComponentType } from 'react'

export type FeaturePageModule = { route: string; default: ComponentType }

// Each feature owns its route entry. Independent UC branches share this registry
// without editing the same navigation map for every new screen.
export function featurePages(modules: Record<string, FeaturePageModule>) {
  return Object.fromEntries(Object.values(modules).map(module => [module.route, module.default]))
}
