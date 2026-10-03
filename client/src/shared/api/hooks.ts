import { useEffect, useRef, useState } from 'react'
import { useAuth } from '@/features/auth/context'
import { apiFetch } from './client'
import { ApiError, errorMessage, readJson, requestIdentity, type RequestIdentity } from './protocol'

export function useResource<T>(path: string | null, decode: (value: unknown) => T, externalRevision = 0) {
  const [revision, setRevision] = useState(0)
  const [result, setResult] = useState<{ key: string; data?: T; error?: string }>({ key: '' })
  const key = `${path}:${revision}:${externalRevision}`
  useEffect(() => {
    if (path === null) return
    const abort = new AbortController()
    void apiFetch(path, { signal: abort.signal }).then(response => readJson(response, decode))
      .then(data => { if (!abort.signal.aborted) setResult({ key, data }) })
      .catch((error: unknown) => { if (!abort.signal.aborted) setResult({ key, error: errorMessage(error) }) })
    return () => abort.abort()
  }, [path, decode, key])
  const current = result.key === key ? result : undefined
  return { data: current?.data, error: current?.error, loading: path !== null && !current, reload: () => setRevision(v => v + 1) }
}

export function useApiAction() {
  const { refresh } = useAuth()
  const [busy, setBusy] = useState(false), [error, setError] = useState('')
  const lock = useRef(false), mounted = useRef(true), identity = useRef<RequestIdentity | null>(null)
  useEffect(() => { mounted.current = true; return () => { mounted.current = false } }, [])
  async function run<T>(path: string, body: unknown, decode: (value: unknown) => T, method = 'POST', retainIdentity = false): Promise<T | undefined> {
    if (lock.current) return undefined
    lock.current = true; setBusy(true); setError('')
    const json = body === undefined ? undefined : JSON.stringify(body)
    identity.current = requestIdentity(identity.current, JSON.stringify([method, path, json]), () => crypto.randomUUID())
    const current = identity.current
    try {
      const session = await refresh()
      if (!mounted.current) return undefined
      if (!session.authenticated) throw new ApiError('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.', 401)
      const response = await apiFetch(path, { method, csrfToken: session.csrfToken,
        headers: { 'Content-Type': 'application/json', 'Idempotency-Key': current.key, 'X-Request-Id': current.key }, body: json })
      const result = await readJson(response, decode)
      if (!mounted.current) return undefined
      if (!retainIdentity) identity.current = null
      return result
    } catch (error) { if (mounted.current) setError(errorMessage(error)); return undefined }
    finally { lock.current = false; if (mounted.current) setBusy(false) }
  }
  return { busy, error, run, setError }
}
