/** @param {typeof fetch} [transport]
 * @returns {Promise<'ready'|'unavailable'>}
 */
export async function checkReadiness(transport = fetch) {
  try {
    const response = await transport('/api/health', {
      signal: AbortSignal.timeout(5000), cache: 'no-store',
    });
    if (!response.ok) return 'unavailable';
    const body = await response.json();
    return body.status === 'UP' ? 'ready' : 'unavailable';
  } catch { return 'unavailable'; }
}
