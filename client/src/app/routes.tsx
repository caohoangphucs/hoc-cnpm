import { Navigate, Route, Routes } from 'react-router'
import { workspaces, workspacePath } from './navigation'
import { AppShell } from './layout/AppShell'
import { OverviewPage } from './pages/OverviewPage'
import { FeatureFrame } from './pages/FeatureFrame'
import { NotFoundPage } from './pages/NotFoundPage'

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/student" replace />} />
      {workspaces.map((workspace) => (
        <Route key={workspace.id} path={workspacePath(workspace)} element={<AppShell workspace={workspace} />}>
          <Route index element={<OverviewPage workspace={workspace} />} />
          {workspace.navigation.filter((item) => item.path).map((item) => (
            <Route key={item.path} path={item.path} element={<FeatureFrame workspace={workspace} item={item} />} />
          ))}
          <Route path="*" element={<NotFoundPage home={workspacePath(workspace)} />} />
        </Route>
      ))}
      <Route path="*" element={<NotFoundPage standalone />} />
    </Routes>
  )
}
