import { useEffect, useRef, useState, type KeyboardEvent, type PointerEvent } from 'react'

type StickerState = {
  x: number; y: number; tiltX: number; tiltY: number; targetX: number; targetY: number
  lift: number; targetLift: number; angle: number; pointer: number | null
  startX: number; startY: number; originX: number; originY: number; lastX: number; lastY: number
}

// Peel mesh and directional sheen adapted from the supplied DraggableSticker.
const vertexShader = `
attribute vec2 aPosition;
attribute vec2 aUV;
uniform vec2 uFrameScale;
uniform float uPeel;
uniform float uAngle;
varying vec2 vUV;
varying float vHighlight;
varying float vShade;
void main() {
  vUV = aUV;
  vec2 p = aPosition;
  vHighlight = 0.0;
  vShade = 0.0;
  if (uPeel > 0.001) {
    vec2 axis = vec2(cos(uAngle), sin(uAngle));
    float diagonal = 0.5 - dot(aUV - vec2(0.5), axis);
    float line = -0.3 + uPeel * 2.0;
    float t = smoothstep(line - 0.6, line, diagonal);
    float ramp = smoothstep(line - 0.6, line - 0.3, diagonal) *
      (1.0 - smoothstep(line - 0.18, line + 0.05, diagonal));
    p *= 1.0 + (1.0 - t) * 0.08;
    p -= axis * ramp * 0.09;
    vHighlight = ramp * pow(1.0 - t, 1.2);
    vShade = ramp * pow(t, 1.2);
  }
  gl_Position = vec4(p * uFrameScale, 0.0, 1.0);
}`

const fragmentShader = `
precision mediump float;
uniform sampler2D uImage;
uniform vec2 uTilt;
uniform vec3 uAccent;
varying vec2 vUV;
varying float vHighlight;
varying float vShade;
void main() {
  vec4 pixel = texture2D(uImage, vUV);
  if (pixel.a < 0.01) discard;
  float gate = smoothstep(0.04, 1.0, clamp(length(uTilt) / 14.0, 0.0, 1.0));
  float gradient = fract(vUV.x * 0.5 + vUV.y * 0.5 + uTilt.x * 0.025 + uTilt.y * 0.025);
  float sheen = smoothstep(0.2, 0.5, gradient) * (1.0 - smoothstep(0.5, 0.8, gradient));
  vec3 color = mix(pixel.rgb, uAccent, 0.08);
  vec3 reflection = mix(vec3(1.0), uAccent, 0.55);
  color = mix(color, reflection, sheen * gate * 0.36);
  color = mix(color, reflection, vHighlight * 0.5);
  color *= 1.0 - vShade * 0.35;
  gl_FragColor = vec4(color, pixel.a);
}`

function createRenderer(canvas: HTMLCanvasElement) {
  const gl = canvas.getContext('webgl', { alpha: true, antialias: true, premultipliedAlpha: false, powerPreference: 'low-power' })
  if (!gl) return null
  const shaders: WebGLShader[] = []
  const buffers: WebGLBuffer[] = []
  const program = gl.createProgram()
  const texture = gl.createTexture()
  const dispose = () => {
    shaders.forEach(shader => gl.deleteShader(shader))
    buffers.forEach(buffer => gl.deleteBuffer(buffer))
    gl.deleteTexture(texture)
    gl.deleteProgram(program)
  }
  if (!program || !texture) { dispose(); return null }
  for (const [type, source] of [[gl.VERTEX_SHADER, vertexShader], [gl.FRAGMENT_SHADER, fragmentShader]] as const) {
    const shader = gl.createShader(type)
    if (!shader) { dispose(); return null }
    shaders.push(shader)
    gl.shaderSource(shader, source)
    gl.compileShader(shader)
    if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) { dispose(); return null }
    gl.attachShader(program, shader)
  }
  gl.linkProgram(program)
  if (!gl.getProgramParameter(program, gl.LINK_STATUS)) { dispose(); return null }
  const vertices: number[] = [], indices: number[] = []
  const grid = 24
  for (let y = 0; y <= grid; y++) for (let x = 0; x <= grid; x++) {
    vertices.push(x / grid * 2 - 1, y / grid * 2 - 1, x / grid, 1 - y / grid)
  }
  for (let y = 0; y < grid; y++) for (let x = 0; x < grid; x++) {
    const index = y * (grid + 1) + x
    indices.push(index, index + 1, index + grid + 1, index + 1, index + grid + 2, index + grid + 1)
  }
  const vertexBuffer = gl.createBuffer(), indexBuffer = gl.createBuffer()
  if (!vertexBuffer || !indexBuffer) {
    gl.deleteBuffer(vertexBuffer); gl.deleteBuffer(indexBuffer); dispose(); return null
  }
  buffers.push(vertexBuffer, indexBuffer)
  gl.useProgram(program)
  gl.bindBuffer(gl.ARRAY_BUFFER, vertexBuffer)
  gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(vertices), gl.STATIC_DRAW)
  gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, indexBuffer)
  gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, new Uint16Array(indices), gl.STATIC_DRAW)
  for (const [name, offset] of [['aPosition', 0], ['aUV', 8]] as const) {
    const attribute = gl.getAttribLocation(program, name)
    gl.enableVertexAttribArray(attribute)
    gl.vertexAttribPointer(attribute, 2, gl.FLOAT, false, 16, offset)
  }
  const frameScale = gl.getUniformLocation(program, 'uFrameScale')
  const peel = gl.getUniformLocation(program, 'uPeel')
  const angle = gl.getUniformLocation(program, 'uAngle')
  const tilt = gl.getUniformLocation(program, 'uTilt')
  gl.activeTexture(gl.TEXTURE0)
  gl.bindTexture(gl.TEXTURE_2D, texture)
  gl.uniform1i(gl.getUniformLocation(program, 'uImage'), 0)
  gl.enable(gl.BLEND)
  gl.blendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA)
  let loaded = false
  return {
    tint(red: number, green: number, blue: number) {
      gl.uniform3f(gl.getUniformLocation(program, 'uAccent'), red, green, blue)
    },
    load(image: HTMLImageElement) {
      gl.bindTexture(gl.TEXTURE_2D, texture)
      gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, image)
      gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE)
      gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE)
      gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR)
      gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR)
      loaded = true
    },
    resize(width: number, height: number) {
      const padding = 24, density = Math.min(window.devicePixelRatio || 1, 2)
      canvas.width = Math.round((width + padding * 2) * density)
      canvas.height = Math.round((height + padding * 2) * density)
      gl.viewport(0, 0, canvas.width, canvas.height)
      gl.uniform2f(frameScale, width / (width + padding * 2), height / (height + padding * 2))
    },
    draw(state: StickerState) {
      if (!loaded || gl.isContextLost()) return
      gl.clearColor(0, 0, 0, 0)
      gl.clear(gl.COLOR_BUFFER_BIT)
      gl.uniform1f(peel, state.lift)
      gl.uniform1f(angle, state.angle)
      gl.uniform2f(tilt, state.tiltX, state.tiltY)
      gl.drawElements(gl.TRIANGLES, indices.length, gl.UNSIGNED_SHORT, 0)
    },
    dispose,
  }
}

export function DraggableSticker({ image, alt }: { image: string; alt: string }) {
  const stageRef = useRef<HTMLDivElement>(null)
  const buttonRef = useRef<HTMLButtonElement>(null)
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const wake = useRef<() => void>(() => {})
  const [ready, setReady] = useState(false)
  const state = useRef<StickerState>({ x: 0, y: 0, tiltX: 0, tiltY: 0, targetX: 0, targetY: 0,
    lift: 0, targetLift: 0, angle: -Math.PI / 4, pointer: null,
    startX: 0, startY: 0, originX: 0, originY: 0, lastX: 0, lastY: 0 })

  useEffect(() => {
    const stage = stageRef.current, button = buttonRef.current, canvas = canvasRef.current
    if (!stage || !button || !canvas) return
    const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)')
    const renderer = reducedMotion.matches ? null : createRenderer(canvas)
    let frame = 0, lastTime = 0, disposed = false
    const tick = (time: number) => {
      frame = 0
      if (disposed) return
      const current = state.current
      const elapsed = Math.min((time - lastTime) / 1000 || 1 / 60, 0.05)
      const smooth = 1 - Math.exp(-elapsed * 18)
      const peelSmooth = 1 - Math.exp(-elapsed * 5)
      lastTime = time
      if (reducedMotion.matches) { current.lift = 0; current.tiltX = 0; current.tiltY = 0 }
      else {
        current.lift += (current.targetLift - current.lift) * peelSmooth
        current.tiltX += (current.targetX - current.tiltX) * smooth
        current.tiltY += (current.targetY - current.tiltY) * smooth
      }
      const unsettled = Math.abs(current.targetLift - current.lift) + Math.abs(current.targetX - current.tiltX) + Math.abs(current.targetY - current.tiltY)
      if (unsettled <= 0.002) {
        current.lift = current.targetLift; current.tiltX = current.targetX; current.tiltY = current.targetY
      }
      button.style.transform = `translate3d(${current.x}px, ${current.y}px, 0) perspective(800px) rotateX(${current.tiltX}deg) rotateY(${current.tiltY}deg)`
      renderer?.draw(current)
      if (!reducedMotion.matches && unsettled > 0.002) frame = requestAnimationFrame(tick)
      else lastTime = 0
    }
    const requestDraw = () => { if (!disposed && !frame) frame = requestAnimationFrame(tick) }
    wake.current = requestDraw
    const resize = () => {
      const box = stage.getBoundingClientRect()
      const width = Math.min(stage.clientWidth * 0.88, 400)
      button.style.width = `${width}px`
      button.style.left = `${box.left - 32}px`
      button.style.top = `${box.top + (stage.clientHeight - width * 2 / 3) / 2}px`
      renderer?.resize(button.clientWidth, button.clientHeight)
      requestDraw()
    }
    const tint = getComputedStyle(stage).getPropertyValue('--sticker-tint-rgb').trim().split(',').map(value => Number(value) / 255)
    renderer?.tint(tint[0], tint[1], tint[2])
    const observer = new ResizeObserver(resize)
    observer.observe(stage)
    observer.observe(button)
    const picture = new Image()
    picture.onload = () => {
      if (disposed || !renderer) return
      try { renderer.load(picture); resize(); setReady(true) }
      catch { setReady(false) }
    }
    picture.src = image
    const contextLost = (event: Event) => { event.preventDefault(); setReady(false) }
    canvas.addEventListener('webglcontextlost', contextLost)
    reducedMotion.addEventListener('change', requestDraw)
    window.addEventListener('resize', resize)
    window.addEventListener('scroll', resize, { passive: true })
    resize()
    return () => {
      disposed = true
      cancelAnimationFrame(frame)
      observer.disconnect()
      picture.onload = null
      picture.src = ''
      canvas.removeEventListener('webglcontextlost', contextLost)
      reducedMotion.removeEventListener('change', requestDraw)
      window.removeEventListener('resize', resize)
      window.removeEventListener('scroll', resize)
      renderer?.dispose()
      wake.current = () => {}
    }
  }, [image])

  function move(x: number, y: number) {
    state.current.x = x
    state.current.y = y
    wake.current()
  }
  function hover(event: PointerEvent<HTMLButtonElement>) {
    if (event.pointerType === 'touch' || state.current.pointer !== null) return
    const current = state.current, box = event.currentTarget.getBoundingClientRect()
    current.angle = Math.atan2(event.clientY - box.top - box.height / 2, event.clientX - box.left - box.width / 2)
    current.targetLift = 1
    wake.current()
  }
  function leave() {
    if (state.current.pointer !== null) return
    state.current.targetLift = 0
    state.current.targetX = 0; state.current.targetY = 0
    wake.current()
  }
  function start(event: PointerEvent<HTMLButtonElement>) {
    if (event.button !== 0 || state.current.pointer !== null) return
    const current = state.current, box = event.currentTarget.getBoundingClientRect()
    current.pointer = event.pointerId
    current.startX = current.lastX = event.clientX
    current.startY = current.lastY = event.clientY
    current.originX = current.x; current.originY = current.y
    current.angle = Math.atan2(event.clientY - box.top - box.height / 2, event.clientX - box.left - box.width / 2)
    current.targetLift = 1
    event.currentTarget.setPointerCapture(event.pointerId)
    event.currentTarget.classList.add('is-dragging')
    wake.current()
  }
  function drag(event: PointerEvent<HTMLButtonElement>) {
    const current = state.current
    if (current.pointer !== event.pointerId) return
    current.targetX = Math.max(-14, Math.min(14, -(event.clientY - current.lastY) * 0.8))
    current.targetY = Math.max(-14, Math.min(14, (event.clientX - current.lastX) * 0.8))
    current.lastX = event.clientX; current.lastY = event.clientY
    move(current.originX + event.clientX - current.startX, current.originY + event.clientY - current.startY)
  }
  function end(event: PointerEvent<HTMLButtonElement>) {
    const current = state.current
    if (current.pointer !== event.pointerId) return
    current.pointer = null
    current.targetLift = 0; current.targetX = 0; current.targetY = 0
    event.currentTarget.classList.remove('is-dragging')
    if (event.currentTarget.hasPointerCapture(event.pointerId)) event.currentTarget.releasePointerCapture(event.pointerId)
    wake.current()
  }
  function keyboard(event: KeyboardEvent<HTMLButtonElement>) {
    const offset = { ArrowLeft: [-12, 0], ArrowRight: [12, 0], ArrowUp: [0, -12], ArrowDown: [0, 12] }[event.key]
    if (offset) { event.preventDefault(); move(state.current.x + offset[0], state.current.y + offset[1]) }
    else if (event.key === 'Home' || event.key === 'Escape') { event.preventDefault(); move(0, 0) }
  }
  return <div ref={stageRef} className="draggable-sticker">
    <button ref={buttonRef} type="button" className="sticker-handle" aria-label={`${alt}. Kéo để di chuyển; dùng phím mũi tên hoặc Home để đặt lại.`}
      onPointerEnter={hover} onPointerLeave={leave} onPointerDown={start} onPointerMove={drag} onPointerUp={end} onPointerCancel={end} onLostPointerCapture={end} onKeyDown={keyboard}
      onClick={event => { if (event.detail === 0) move(0, 0) }}>
      <img className={ready ? 'sticker-image sticker-image--rendered' : 'sticker-image'} src={image} alt="" draggable={false} />
      <canvas ref={canvasRef} className={ready ? 'sticker-canvas sticker-canvas--ready' : 'sticker-canvas'} aria-hidden="true" />
    </button>
  </div>
}
