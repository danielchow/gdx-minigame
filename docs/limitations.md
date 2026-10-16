# WeChat Mini Game limitations

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716.

This describes the current backend and checked-in WeChat API references. Verification was source inspection, not device testing or a live compatibility survey. A missing backend implementation does not establish that WeChat cannot provide the capability. Open implementation work is tracked in [the API-gap backlog](../backlog/wx-api-gap/INDEX.md).

## Platform and API constraints

| Capability | Platform constraint | Current backend behavior |
| --- | --- | --- |
| External browser links | No general external-browser URL opener was identified in the checked-in Mini Game API set. Mini-program navigation is a different capability. | `MiniGameNet.openURI` returns false without opening or copying the URL. |
| Exact vibration duration | The documented vibration APIs provide short (15 ms) and long (400 ms) effects, rather than an arbitrary duration. Short vibration supports light/medium/heavy strength on supporting clients. | Requests below 200 ms use medium short vibration; longer requests use long vibration. VibrationType maps to the three short strengths. Numeric amplitude is not mapped. |
| Java/native heap measurements | No documented API for those byte counts was identified. Memory warnings provide pressure notifications, not heap measurements. | `getJavaHeap` and `getNativeHeap` return zero. Use runtime tooling for investigation; do not interpret zero as no memory use. |
| GL31/GL32 | The documented canvas targets are WebGL1 and WebGL2; these do not supply the full LibGDX GL31/GL32 contracts. | Availability is false and the corresponding accessors return null. GL30 depends on obtaining a WebGL2 context. |
| Pan on InnerAudioContext | InnerAudioContext has no documented pan property. Selecting its WebAudio implementation does not expose a node graph. | Sound/music pan is ignored while the supplied volume is applied. A separate WebAudio graph is an implementation option, not a proven platform impossibility. |
| Synchronous clipboard reads | `wx.getClipboardData` is asynchronous. It cannot directly provide a fresh value to a synchronous LibGDX getter. | Reads use a Java cache set by `setContents`; external clipboard edits are not refreshed. An explicit async extension/cache policy is pending. |

For external links, applications should handle false from `openURI` and offer a deliberate “Copy link” action using the clipboard, or show the address in a modal. Do not silently turn every failed open into a clipboard write. [`wx.navigateToMiniProgram`](../references/wx-api-docs/navigate/wx.navigateToMiniProgram.md) navigates to a mini program; it does not open an arbitrary browser URL.

Sources: [network behavior](../backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameNet.java) (177–179), [input behavior](../backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameInput.java) (189–202), [short vibration](../references/wx-api-docs/device/vibrate/wx.vibrateShort.md), [long vibration](../references/wx-api-docs/device/vibrate/wx.vibrateLong.md), [memory warnings](../references/wx-api-docs/device/memory/wx.onMemoryWarning.md), [canvas contexts](../references/wx-api-docs/render/canvas/Canvas.getContext.md), [InnerAudioContext](../references/wx-api-docs/media/audio/InnerAudioContext.md), and [clipboard reads](../references/wx-api-docs/device/clipboard/wx.getClipboardData.md).

## Unsupported backend contracts

These currently fail or return fallback values, but the references do not establish a platform impossibility.

| Contract | Current behavior | Adaptation needed |
| --- | --- | --- |
| AudioDevice / AudioRecorder | Creation throws GdxRuntimeException. | WebAudioContext documents AudioBuffer creation/playback; RecorderManager documents PCM format and frame callbacks. Matching LibGDX streaming, buffering, latency and microphone-permission semantics still requires design and validation. |
| LibGDX client/server sockets | Creation throws GdxRuntimeException. | WX has event-based TCP/UDP and WebSocket APIs. These are not drop-in blocking stream sockets; define a faithful adapter or a separate async extension. |
| Text-input dialog | getTextInput posts canceled() on the next frame. | The native keyboard is a separate UI with unwired configuration/events; it is not a complete text dialog. |
| Sensors and orientation | Sensor/angle getters return zero, the rotation-matrix method does nothing, and peripheral reporting only recognizes multitouch. | WX sensor APIs exist. Bindings, coordinate conversions, availability and lifecycle handling are missing. |
| Native desktop mouse/wheel | Input registers touch handlers and synthesizes left-button events. | Native mouse/wheel event mapping is pending. Touch emulation coverage is unverified; absence of these bindings does not prove PC has no input. |

Sources: [audio](../backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameAudio.java) (196–203), [WebAudioContext](../references/wx-api-docs/media/audio/WebAudioContext.md), [recorder options](../references/wx-api-docs/media/recorder/RecorderManager.start.md), [recorder frames](../references/wx-api-docs/media/recorder/RecorderManager.onFrameRecorded.md), [socket behavior](../backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameNet.java) (162–173), and [input](../backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameInput.java) (132–182, 205–225).

## Window, rendering and lifecycle behavior

WeChat supports desktop clients. The backend follows `wx.onWindowResize` for canvas dimensions, density and touch scaling (desktop WeChat fires none while dragging or maximizing; it scales the launch-size canvas), supports separate desktop orientation in generated game.json, and handles PC minimize/restore lifecycle events. Those capabilities must not be classified as impossible mobile-only work.

The current graphics backend still leaves title, decoration, resizability, VSync and cursor setters as no-ops; `newCursor` returns null. `setWindowedMode` returns false. Input cursor capture/position are also no-ops, rotation reports zero and native orientation reports Portrait. These describe backend behavior, not proof that every corresponding desktop feature is unavailable in WeChat.

Rendering is continuous by default; `setContinuousRendering(false)` and `requestRendering` support on-demand frames (see below). `setForegroundFPS` remains a no-op despite an existing `WX.setPreferredFramesPerSecond` binding. `getVersion` returns zero; exposing SDKVersion would be a separate API decision, not an established integer mapping.

`Application.exit` already calls `wx.exitMiniProgram` when available. It is not an empty lifecycle stub. Local files already use PersistentFileStorage under `USER_DATA_PATH`; they are not inherently lost on restart.

Sources: [graphics](../backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameGraphics.java) (62–98, 130–149, 241–261, 297–363), [application](../backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameApplication.java) (133–165, 417–428, 451–454), [WX frame-rate binding](../backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/bindings/WX.java) (64–67), [file storage selection](../backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameFiles.java) (23–25), and [build configuration](../backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/config/backend/MiniGameBackend.java) (39–62, 176–180).

## On-demand rendering

`Graphics.setContinuousRendering(false)` stops the MiniGame RAF chain once the current frame
finishes. `requestRendering()` coalesces calls into one frame on the next RAF, including calls
from a bare `setTimeout` callback; it never draws synchronously. This is the deadline API for
JS event-loop pacers: schedule `Gdx.graphics.requestRendering()` with `setTimeout`, cancel or
replace the timer in the consumer. No DOM Window or Java thread is required. Continuous
rendering remains the default. Input, `postRunnable`, window resize, show and async asset/HTTP/music
completion wake the loop. Requests from a frame or a runnable are preserved for the next
frame. Hide/minimize cancels RAF in both continuous and on-demand modes, and preserves queued work for show/restore.
This intentionally stops hidden simulator rendering too, matching phone host suspension.
`postRunnable` work queued after construction but before the first RAF is retained and runs
after `ApplicationListener.create`, rather than being discarded at initialization. Hardware key
and text events wake frames, but this does not add LibGDX hardware-key translation (still
unsupported). Node regressions cover scheduling; retained-canvas visibility with
`preserveDrawingBuffer=false` must also be checked on real WeChat hosts.
