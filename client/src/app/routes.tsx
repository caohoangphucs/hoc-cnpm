import { Route, Routes } from 'react-router'
import { workspaces, workspacePath } from './navigation'
import { AppShell } from './layout/AppShell'
import { OverviewPage } from './pages/OverviewPage'
import { FeatureFrame } from './pages/FeatureFrame'
import { NotFoundPage } from './pages/NotFoundPage'
import { LoginPage } from '@/features/auth/LoginPage'
import { TwoFactorPage } from '@/features/auth/TwoFactorPage'
import { LoginHelpPage } from '@/features/auth/LoginHelpPage'
import { HomeRedirect, RequireSession } from '@/features/auth/RouteGuards'

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<HomeRedirect />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/login/2fa" element={<TwoFactorPage />} />
      <Route path="/login/help" element={<LoginHelpPage />} />
      {workspaces.map((workspace) => (
        <Route key={workspace.id} element={<RequireSession workspace={workspace.id} />}>
          <Route path={workspacePath(workspace)} element={<AppShell workspace={workspace} />}>
            <Route index element={<OverviewPage workspace={workspace} />} />
            {workspace.navigation.filter((item) => item.path).map((item) => (
              <Route key={item.path} path={item.path} element={<FeatureFrame workspace={workspace} item={item} />} />
            ))}
            <Route path="*" element={<NotFoundPage home={workspacePath(workspace)} />} />
          </Route>
        </Route>
      ))}
      <Route path="*" element={<NotFoundPage standalone />} />
    </Routes>
  )
}
