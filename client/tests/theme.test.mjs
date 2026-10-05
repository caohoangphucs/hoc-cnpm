import { test } from 'node:test'
import assert from 'node:assert/strict'
import { revealTheme } from '../src/theme/theme.ts'

function environment(t, { supported = true, reduced = false, failedSnapshot = false, failedAnimation = false, animationFinished = Promise.resolve(), snapshotReady } = {}) {
  const calls = { updates: 0, transitions: 0, skipped: 0, animations: [], saved: [], rates: [] }
  const classes = new Set()
  const root = {
    dataset: {},
    classList: { add: value => classes.add(value), remove: value => classes.delete(value) },
    animate: (frames, options) => {
      if (failedAnimation) throw new Error('Animation unavailable')
      calls.animations.push({ frames, options })
      return { finished: animationFinished, playState: 'running', updatePlaybackRate: rate => calls.rates.push(rate) }
    },
  }
  const values = {
    document: {
      documentElement: root,
      startViewTransition: supported ? update => {
        calls.transitions++
        update()
        return {
          ready: snapshotReady ?? (failedSnapshot ? Promise.reject(new Error('Snapshot skipped')) : Promise.resolve()),
          finished: Promise.resolve(),
          skipTransition: () => calls.skipped++,
        }
      } : undefined,
    },
    window: { innerWidth: 1000, innerHeight: 700, matchMedia: () => ({ matches: reduced }) },
    localStorage: { setItem: (...args) => calls.saved.push(args) },
  }
  for (const [key, value] of Object.entries(values)) {
    const previous = Object.getOwnPropertyDescriptor(globalThis, key)
    Object.defineProperty(globalThis, key, { configurable: true, value })
    t.after(() => {
      if (previous) Object.defineProperty(globalThis, key, previous)
      else delete globalThis[key]
    })
  }
  const button = { getBoundingClientRect: () => ({ left: 920, top: 20, width: 48, height: 48 }) }
  return { calls, root, classes, button, update: () => calls.updates++ }
}

test('rapid toggles reverse the existing wave and preserve the last requested theme', async t => {
  const end = Promise.withResolvers()
  const env = environment(t, { animationFinished: end.promise })
  const first = revealTheme('dark', env.button, env.update)
  await Promise.resolve()
  const second = revealTheme('light', env.button, env.update)
  assert.equal(env.root.dataset.theme, 'light')
  const third = revealTheme('dark', env.button, env.update)
  assert.equal(env.root.dataset.theme, 'dark')
  assert.equal(env.calls.transitions, 1)
  assert.equal(env.calls.animations.length, 1)
  assert.deepEqual(env.calls.rates, [-1, 1])
  assert.equal(env.calls.updates, 3)
  end.resolve()
  await Promise.all([first, second, third])
  assert.equal(env.classes.size, 0)
  assert.equal(env.calls.saved.at(-1)[1], 'dark')
})

test('a second request while snapshots are pending wins without starting another transition', async t => {
  const ready = Promise.withResolvers()
  const env = environment(t, { snapshotReady: ready.promise })
  const first = revealTheme('dark', env.button, env.update)
  const second = revealTheme('light', env.button, env.update)
  ready.resolve()
  await Promise.all([first, second])
  assert.equal(env.root.dataset.theme, 'light')
  assert.equal(env.calls.transitions, 1)
  assert.equal(env.calls.animations.length, 0)
  assert.equal(env.calls.updates, 2)
  assert.equal(env.calls.skipped, 1)
  assert.equal(env.classes.size, 0)
})

test('theme reveal starts at the button centre and covers every viewport corner', async t => {
  const env = environment(t)
  await revealTheme('dark', env.button, env.update)
  const { frames, options } = env.calls.animations[0]
  assert.equal(frames.clipPath[0], 'circle(0px at 944px 44px)')
  const radius = Number(frames.clipPath[1].match(/^circle\((\d+)px/)[1])
  for (const [x, y] of [[0, 0], [1000, 0], [0, 700], [1000, 700]]) {
    assert.ok(radius > Math.hypot(x - 944, y - 44))
  }
  assert.equal(options.pseudoElement, '::view-transition-new(root)')
  assert.equal(env.root.dataset.theme, 'dark')
  assert.equal(env.calls.updates, 1)
  assert.deepEqual(env.calls.saved, [['ktx-theme', 'dark']])
  assert.equal(env.classes.size, 0)
})

for (const [name, options] of [
  ['unsupported browser', { supported: false }],
  ['reduced motion', { reduced: true }],
]) {
  test(`theme changes immediately without animation for ${name}`, async t => {
    const env = environment(t, options)
    await revealTheme('light', env.button, env.update)
    assert.equal(env.root.dataset.theme, 'light')
    assert.equal(env.calls.updates, 1)
    assert.equal(env.calls.transitions, 0)
    assert.equal(env.calls.animations.length, 0)
    assert.deepEqual(env.calls.saved, [['ktx-theme', 'light']])
  })
}

for (const [name, options] of [
  ['snapshot is skipped', { failedSnapshot: true }],
  ['animation throws', { failedAnimation: true }],
]) {
  test(`theme remains applied once and transition is cleaned up when ${name}`, async t => {
    const env = environment(t, options)
    await revealTheme('dark', env.button, env.update)
    assert.equal(env.root.dataset.theme, 'dark')
    assert.equal(env.calls.updates, 1)
    assert.equal(env.calls.saved.length, 1)
    assert.equal(env.calls.skipped, 1)
    assert.equal(env.classes.size, 0)
  })
}
