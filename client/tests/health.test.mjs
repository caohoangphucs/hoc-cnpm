import test from 'node:test';
import assert from 'node:assert/strict';
import { checkReadiness } from '../src/shared/api/health.mjs';

test('checks the same-origin proxy and accepts only UP', async () => {
  const result = await checkReadiness(async (url, options) => {
    assert.equal(url, '/api/health'); assert.equal(options.cache, 'no-store');
    assert.ok(options.signal instanceof AbortSignal);
    return Response.json({ status: 'UP' });
  });
  assert.equal(result, 'ready');
});
test('DOWN, HTTP errors, invalid JSON and network errors are unavailable', async () => {
  for (const stub of [
    async () => Response.json({ status: 'DOWN' }),
    async () => new Response('Unavailable', { status: 503 }),
    async () => new Response('Not JSON'),
    async () => { throw new Error('Connection refused'); },
  ]) assert.equal(await checkReadiness(stub), 'unavailable');
});
