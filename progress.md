# Progress

## Status
Completed

## Tasks
- [x] Task 1: Set config.canvas from globalThis.canvas via @JSBody method in MiniGameApplication
- [x] Task 2: Fix getAudio() to return MiniGameAudio instance (added field + fixed return)
- [x] Task 3: Fix atob polyfill — replaced Buffer.from() with pure JS base64 decoder
- [x] Task 4: Fix WebAssembly patch condition — patch when WXWebAssembly exists, create WebAssembly if needed
- [x] Task 5: Remove non-existent game-code/ subpackage from game.json, remove wx.loadSubpackage game call
- [x] Task 6: Add 30s timeout to Gdx busy-wait loop in game.js
- [x] Task 7: Add warning for empty appId in setupMinigame()
- [x] Task 8: Add XMLHttpRequest polyfill wrapping wx.request() in adapter/index.js
- [x] Task 9: Remove unused Window import from MiniGameNet.java
- [x] Task 10: Track removed keys and delete from WX storage during flush()
- [x] Task 11: Add getLoop() getter to WXAudioContext, fix isLooping() in MiniGameAudio
- [x] Task 12: Scale touch coordinates by pixel ratio (cached scale factors)

## Files Changed
- `src/main/java/.../MiniGameApplication.java` - Tasks 1, 2: added getGlobalCanvas() JSBody, added audio field, fixed getAudio()
- `src/main/java/.../config/backend/MiniGameBackend.java` - Tasks 3-8: atob polyfill, WASM patch, subpackage fix, timeout, appId warning, XHR polyfill
- `src/main/java/.../MiniGameNet.java` - Task 9: removed unused Window import
- `src/main/java/.../MiniGamePreferences.java` - Task 10: added removedKeys tracking, flush() now deletes removed keys
- `src/main/java/.../bindings/WXAudioContext.java` - Task 11: added getLoop() getter
- `src/main/java/.../MiniGameAudio.java` - Task 11: fixed isLooping() to use getLoop()
- `src/main/java/.../MiniGameInput.java` - Task 12: added cached touch scale factors, scale coordinates in handler

## Notes
- All 12 bug fixes implemented successfully
- `./gradlew :backends:backend-minigame:compileJava` — BUILD SUCCESSFUL
- `./gradlew :minigame:buildRelease` — BUILD SUCCESSFUL (707 classes compiled)
- Generated adapter/index.js includes full XHR polyfill wrapping wx.request()
- Generated game.json no longer references non-existent game-code/ subpackage
- Generated game.js has 30s timeout on Gdx wait loop and no game subpackage load

## Review — 2026-05-01 (Round 2: Post-12-Fix Verification)

### Part 1: Built Output Verification

**`adapter/index.js`** — All five sections verified:
- ✅ `window` polyfill: correct, includes `requestAnimationFrame`, `devicePixelRatio`, etc.
- ✅ `WebAssembly` patch: correctly patches when only `WXWebAssembly` exists; creates empty `WebAssembly` if needed; redirects `ArrayBuffer/Uint8Array` calls to `WXWebAssembly.instantiate('scripts/gdx.wasm', imports)`
- ✅ `atob` polyfill: pure JS base64 decoder, tested against native — identical output for all standard inputs
- ✅ `XMLHttpRequest` polyfill: wraps `wx.request()`, implements all methods used by TeaVM's `XMLHttpRequest` JSO
- ✅ `console` polyfill: defensive, fine
- ✅ `setup()` function: stores canvas on `globalThis.canvas`

**`game.js`** — ✅ Timeout present (30s), no game-code subpackage load, canvas setup before game start, timeout errors propagate correctly

**`game.json`** — ✅ Only `engine` and `assets` subpackages (no `game-code/`)

**`project.config.json`** — ✅ Structure correct, placeholder appid

### Part 2: Source Code Diffs — All Clean

All 7+1 files modified with surgical, minimal changes. No unintended modifications. Each task's diff matches its specification exactly.

### Part 3 & 4: Findings

#### Fixed: Timeout error silently swallowed by catch block (introduced by Task 6)
- **Severity:** MEDIUM
- **File:** `MiniGameBackend.java`, game.js template, line 171
- **Problem:** The timeout error `throw new Error('Timed out waiting for Gdx to initialize (30s)')` was thrown inside a `try { ... } catch(e) { /* optional */ }` block. The catch was originally for optional gdx.wasm.js loading, but it silently swallowed the timeout error too. If Gdx fails to initialize, the game would continue to `require('./app.js')` with `window.Gdx` still `undefined`, causing a confusing crash later instead of the clear timeout message.
- **Fix:** Changed `catch(e) { /* optional */ }` to `catch(e) { if (e.message && e.message.indexOf('Timed out') >= 0) throw e; }` so timeout errors propagate to the outer try-catch which logs `'Failed to load:' err`. Non-timeout errors (e.g., gdx.wasm.js not found) are still silently ignored.
- **Verified:** Rebuilt test project — `game.js` now correctly propagates timeout errors.

### Note: Observations

#### A. XHR `open()` doesn't fire `onreadystatechange` for OPENED state (spec deviation)
- **Severity:** LOW (no functional impact)
- **Evidence:** `adapter/index.js`, `XHR.prototype.open` sets `this.readyState = XHR.OPENED` but never calls `onreadystatechange`. Standard XHR fires it. However, `MiniGameNet.java` only checks `readyState == DONE`, so this deviation has zero functional impact.

#### B. Touch scale factors are cached at construction time
- **Severity:** LOW (no known impact)
- **Evidence:** `MiniGameInput.java` lines 53-55: `touchScaleX = (float) canvas.getWidth() / (float) WX.getScreenWidth()` cached in constructor. If `MiniGameGraphics.setWindowedMode()` were called later, the cached factors would be stale. However, `setWindowedMode` is never called for this Mini Game backend (always fullscreen), so this is safe in practice.

#### C. XHR `abort()` is a no-op
- **Severity:** LOW
- **Evidence:** `adapter/index.js` `XHR.prototype.abort = function() {}` does nothing. `MiniGameNet.cancelHttpRequest()` doesn't call `abort()`, so this doesn't affect current code.

#### D. Touch coordinate scaling formula is correct
- **Verified against reference doc** (`02-TechPrinciple.md` lines 105-106): Formula `x = (touch.pageX - rect.left) * (canvas.width / rect.width)`. Our implementation `x = touch.getClientX() * (canvas.getWidth() / WX.getScreenWidth())` is equivalent for full-screen Mini Game where `rect.left = 0` and `rect.width = screenWidth`. Default canvas has `width = screenWidth` so `scaleX = 1.0` (identity). For high-DPI canvas (`width = screenWidth * pixelRatio`), `scaleX = pixelRatio`, correctly scaling CSS touch coords to physical pixels.

#### E. Runtime timing is correct
- `game.js` calls `setup(canvas)` synchronously before `startGame()` begins
- `startGame()` loads engine subpackage, then requires `app.js`
- `app.js` triggers `MiniGameApplication.init()` → `getGlobalCanvas()` reads `globalThis.canvas` which was set by `setup(canvas)`
- Canvas is valid by the time `MiniGameGraphics` constructor casts it to `HTMLCanvasElement`

#### F. Preferences put-after-remove is wasteful but correct
- If `remove(key)` then `put(key, val)` before `flush()`, the key is in both `removedKeys` and `values`. `flush()` first removes from storage, then writes back — net effect is correct (key exists with new value). Slightly inefficient but not a bug.

#### G. WASM path is correct
- `WXWebAssembly.instantiate('scripts/gdx.wasm', imports)` uses a relative path. The WASM file is in the `scripts/` subpackage, accessible via relative path from the game root. No `wx.env.USER_DATA_PATH` prefix needed for subpackage files.

#### H. `WX.getScreenWidth()` is available at MiniGameInput construction time
- `wx.getSystemInfoSync()` is a synchronous API available from the start of the Mini Game runtime. Called once during `MiniGameInput` constructor, so no performance concern.

## Review — 2026-05-01 (Round 3: Final Pass)

### Scope
Thorough final review of ALL source files, ALL built output, ALL emu files, ALL gen files, and cross-reference with WeChat Mini Game documentation. Pattern-matched for 15+ categories of known browser-only APIs.

### Built Output — All Verified Clean
- **`adapter/index.js`**: ✅ All 5 polyfill sections (window, WASM, atob, console, XHR) + `setup()` function. No browser-only APIs.
- **`game.js`**: ✅ Correct launch sequence, timeout propagation, subpackage loading
- **`game.json`**: ✅ Correct subpackages, device orientation, network timeouts
- **`project.config.json`**: ✅ Correct structure, compile type, ES6 enabled
- **`app.js`** (first/last 50 lines): ✅ UMD module pattern, exports `main`. Tree-shaking eliminates all browser-only APIs (`document.*`, `URL.createObjectURL`, `new Image`, `eval`, `fetch`, `Blob`) — confirmed zero occurrences via grep.

### Source Files — All 44 Java Files Reviewed
- **`MiniGameApplication.java`**: ✅ Lifecycle (wx.onShow/onHide), game loop (WX.requestAnimationFrame), error handling
- **`MiniGameGraphics.java`**: ✅ Canvas from config, no DOM references, correct GL version detection
- **`MiniGameInput.java`**: ✅ Touch scaling with cached factors, correct coordinate mapping
- **`MiniGameAudio.java`**: ✅ Audio context pool with steal mechanism, proper lifecycle management
- **`MiniGameFiles.java`** / **`MiniGameFileHandle.java`**: ✅ MemoryFileStorage, correct type routing
- **`MiniGameNet.java`**: ✅ Uses XMLHttpRequest (polyfilled), no browser Window import
- **`MiniGamePreferences.java`**: ✅ WX storage, removedKeys tracking
- **`MiniGameClipboard.java`**: ✅ wx.setClipboardData
- **`MiniGameBackend.java`**: ✅ All JS templates syntactically valid, correct string concatenation
- **`WX.java`**: ✅ All wx.* API bindings correct, bare requestAnimationFrame (polyfilled by adapter)
- **`WXAudioContext.java`**: ✅ getLoop() getter present
- **`Timer.java`**: ✅ Uses bare setTimeout/setInterval (available in WeChat)
- **`KeyCodes.java`**: ✅ `navigator.keyboard` usage guarded by `if (!navigator.keyboard)` check
- **Plugins (3 files)**: ✅ Standard TeaVM plugin infrastructure
- **Emu files (38 files)**: ✅ All checked, no browser-only API issues

### Browser-Only API Scan — All Clear
| Pattern | In Source? | In Compiled Output? | Status |
|---------|-----------|---------------------|--------|
| `console.groupCollapsed/End` | ✅ MiniGameApplication | ✅ app.js (1 occurrence) | See Note A |
| `navigator.*` | ✅ KeyCodes (guarded) | ❌ Tree-shaken | Safe |
| `document.*` | ❌ Comments only | ❌ | ✅ Clean |
| `window.location/history` | ❌ | ❌ | ✅ Clean |
| `eval()` / `new Function()` | ❌ | ❌ | ✅ Clean |
| `Blob` / `URL.createObjectURL` | ❌ | ❌ | ✅ Clean |
| `performance.now()` | ❌ | ❌ | ✅ Clean |
| `fetch()` | ❌ | ❌ | ✅ Clean |
| `WebSocket` | ❌ | ❌ | ✅ Clean |
| `Image()` constructor | ❌ | ❌ | ✅ Clean |
| `localStorage` | ❌ | ❌ | ✅ Clean |

### Findings

#### Fixed: FileDB.list(suffix) uses wrong variable in filter — Issue #13
- **Severity:** MEDIUM
- **File:** `filesystem/FileDB.java`, line 97
- **Problem:** `list(MiniGameFileHandle file, String suffix)` iterates child files with variable `f`, but the filter condition checks `file.path().endsWith(suffix)` (the **directory's** path) instead of `f.path().endsWith(suffix)` (the **child's** path). This means suffix-based file listing always returns an empty array unless the directory path itself ends with the suffix (which it never does in practice). Any code calling `FileHandle.list(String suffix)` would get zero results.
- **Fix:** Changed `file.path().endsWith(suffix)` to `f.path().endsWith(suffix)` on line 97.
- **Verified:** `compileJava` BUILD SUCCESSFUL.

### Notes

#### A. `console.groupCollapsed`/`console.groupEnd` in error handler
- **Severity:** LOW
- **Evidence:** `MiniGameApplication.printErrorStack()` uses `@JSBody` to call `console.groupCollapsed()` / `console.groupEnd()`. These appear in compiled `app.js` (1 occurrence each). The adapter's console polyfill only covers `log`/`error`/`warn`. However:
  - WeChat Mini Game provides a `console` object by default (polyfill doesn't activate)
  - WeChat's console API does support `groupCollapsed`/`groupEnd` in DevTools
  - On some real devices, these may be no-ops (WeChat often stubs them)
  - If they throw, it's inside the error handler, caught by `step()`'s try-catch
  - Only triggered during fatal error reporting, not normal gameplay
- **No fix needed** — the worst case is slightly degraded error output formatting.

#### B. `KeyCodes.initLayoutLabels()` uses `navigator.keyboard` (browser-only API)
- **Severity:** LOW
- **Evidence:** `KeyCodes.java` lines 952-966 use `navigator.keyboard.getLayoutMap()` via `@JSBody`. This is a Chrome-only Keyboard Layout API. However, the JS code starts with `if (!navigator.keyboard || !navigator.keyboard.getLayoutMap) return;` which safely exits early on WeChat. No error, no functional impact. The `window._gdxLayoutLabels` references also work because `window` is polyfilled by the adapter.

#### C. Total issues found across all 3 review rounds
- Round 1-2: 12+1 issues fixed
- Round 3: 1 additional issue found and fixed (FileDB.list suffix filter bug)
- **Total: 14 issues found and resolved. Final pass clean.**
