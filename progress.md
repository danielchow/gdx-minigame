# gdx-minigame: Plan Review Progress

## Review — 2026-05-01 (Final Pass)

### Verification of Previously Found Issues

All 6 previously identified issues are **FIXED** in the current plan:

| # | Issue | Status | Evidence |
|---|-------|--------|----------|
| 1 | game.js async Gdx init | ✅ Fixed | §4.3 has polling loop `while (typeof window.Gdx === 'undefined') { await new Promise(r => setTimeout(r, 50)); }` |
| 2 | Missing MiniGameAssetLoader | ✅ Fixed | §3.6 added, covers AssetLoadImpl + AssetDownloadImpl replacement |
| 3 | Missing teavm.properties | ✅ Fixed | §1.7 added with full mapPackageHierarchy directives |
| 4 | Missing Emulate.java | ✅ Fixed | §1.6 added, copies annotation to gen/ directory |
| 5 | Missing MiniGameApplicationConfiguration | ✅ Fixed | §3.2 added with field-by-field DOM removal |
| 6 | Wrong base class for MiniGameBuildConfiguration | ✅ Fixed | §4.2 now says "Don't extend it — create a standalone config class" |

---

### Fixed: New Issues Found

#### Issue 1 (CRITICAL): `TypedArrays.java` will be incorrectly skipped — breaks ALL WebGL
**Location:** File Inventory → Skip entirely → `dom/*.java`
**Problem:** The plan says skip "dom/*.java (all DOM wrappers)". But `dom/typedarray/TypedArrays.java` is NOT a DOM wrapper — it's a pure TeaVM typed-array-to-Java-NIO bridge with zero DOM dependencies. It's imported by `WebGL20.java` (26+ call sites) and `WebGL30.java` (25+ call sites), both of which the plan marks "Copy as-is." If TypedArrays is skipped, those files won't compile.
**Fix:** Add `dom/typedarray/TypedArrays.java` to the "Copy as-is" table with note: "In dom/ package but has NO DOM deps — pure TeaVM typed array ↔ Java NIO bridge. Required by WebGL20 and WebGL30."

#### Issue 2 (CRITICAL): `ClasspathStorage.java` and `InternalStorage.java` missing from all tables
**Location:** File Inventory tables
**Problem:** Both files are simple empty classes (`extends MemoryFileStorage`). They're imported and instantiated by `WebFiles.java` (line 23–24: `new InternalStorage()` / `new ClasspathStorage()`). The plan lists `WebFiles.java` in the "Fork and modify" table, but the fork can't compile without these two classes.
**Fix:** Add to "Copy as-is" table:
- `filesystem/types/ClasspathStorage.java` — Empty class extending MemoryFileStorage, used by WebFiles
- `filesystem/types/InternalStorage.java` — Empty class extending MemoryFileStorage, used by WebFiles

#### Issue 3 (HIGH): `WebApplicationLogger.java` missing from all tables
**Location:** File Inventory tables
**Problem:** Zero DOM deps — pure `System.out.println`/`System.err.println`. Used by `WebApplication` line 138: `logger = new WebApplicationLogger()`. If not copied, `MiniGameApplication` has no logger implementation. The plan's Phase 3.1 doesn't mention creating a replacement.
**Fix:** Add to "Copy as-is" table: `WebApplicationLogger.java` — Pure System.out/err logging, no DOM.

#### Issue 4 (HIGH): `WebPreferences.java` missing from "Fork and modify" table
**Location:** File Inventory → Fork and modify table
**Problem:** Phase 3.8 explicitly describes forking `WebPreferences.java` → `MiniGamePreferences.java`, but the file is not listed in the "Fork and modify" table. An agent following the tables will miss it.
**Fix:** Add to "Fork and modify" table: `WebPreferences.java` → `MiniGamePreferences.java` | Replace `Storage.getLocalStorage()` with `WX.setStorageSync/getStorageSync`

#### Issue 5 (HIGH): MiniGameGraphics DOM replacement table is incomplete
**Location:** Phase 3.3 — `MiniGameGraphics.java`
**Problem:** The replacement table covers fullscreen, setTitle, and `getClientWidth/Height()`, but misses four additional DOM dependencies in `WebGraphics.java`:
1. `getNativeScreenDensityNATIVE()` → bare `@JSBody(script = "return devicePixelRatio || 1;")` — `devicePixelRatio` is not a WeChat global
2. `getScreenWidthNATIVE()` → bare `@JSBody(script = "return screen.width;")` — `screen` is not a WeChat global
3. `getScreenHeightNATIVE()` → bare `@JSBody(script = "return screen.height;")` — `screen` is not a WeChat global
4. `setCanvasSize()` uses `canvas.getStyle().setProperty("width", width + "px")` — CSS API not available in WeChat
5. `newCursor()` / `setCursor()` / `setSystemCursor()` use `WebCursor` and `canvas.getStyle()` — CSS cursor not available on mobile

**Fix:** Add to the §3.3 DOM dependency table:

| Line(s) | DOM Call | Replacement |
|---------|---------|-------------|
| 372 | `@JSBody(script = "return devicePixelRatio \|\| 1;")` | `@JSBody(script = "return wx.getSystemInfoSync().pixelRatio;")` |
| 375 | `@JSBody(script = "return screen.width;")` | `@JSBody(script = "return wx.getSystemInfoSync().screenWidth;")` |
| 378 | `@JSBody(script = "return screen.height;")` | `@JSBody(script = "return wx.getSystemInfoSync().screenHeight;")` |
| 290 | `canvas.getStyle().setProperty(...)` in `setCanvasSize()` | Remove CSS style lines — just set `canvas.setWidth()` / `canvas.setHeight()` directly |
| 338–348 | `new WebCursor(...)`, `canvas.getStyle().setProperty("cursor", ...)` | Stub: throw `GdxRuntimeException("Not supported")` or no-op |

#### Issue 6 (HIGH): Missing files in "Skip entirely" table + undocumented dependency
**Location:** File Inventory → Skip entirely table
**Problem:** Several files are correctly skippable but not listed, leaving the agent without a complete inventory. More critically, `WebPermissions.java` is used by `WebClipboard.java` (being forked) at lines 70 and 81, but Phase 3.10 doesn't mention removing this dependency.

Missing from "Skip entirely" table:
- `WebAudio.java` — Empty interface, only used by skipped `HowlTeaAudio`
- `WebPermissions.java` — Uses `navigator.permissions` API; **IMPORTANT:** WebClipboard.java calls `WebPermissions.queryPermission("clipboard-write")` — the fork must remove this call
- `WebWindowListener.java` — File drop interface, not needed on mobile
- `WebTool.java` — Unused utility (not imported by any other file)
- `agent/WebWebAgent.java` — Uses `navigator.userAgent`; Phase 3.1 mentions replacing `WebWebAgent.computeAgentInfo()` but file is not in any table
- `dom/audio/Audio.java` — Uses `HTMLAudioElement`, not imported by any other file

**Fix:** 
1. Add all above to "Skip entirely" table with reasons
2. Add to Phase 3.10 (MiniGameClipboard) note: "Remove `WebPermissions.queryPermission("clipboard-write")` call — WeChat clipboard API doesn't need permission checks"

#### Issue 7 (MEDIUM): Phase 6 ordering is correct but dependency is underspecified
**Location:** Phase 6 — Build Plugin
**Problem:** Phase 6 copies `WebClassTransformer.java` which uses reflection to scan for `@Emulate` annotations. The scan package path will need to change from `com.github.xpenatan.gdx.teavm.backends.web` to `com.github.xpenatan.gdx.teavm.backends.minigame`. The plan doesn't mention this.
**Fix:** Add note to §6.2: "When copying WebClassTransformer, update the Reflections scan package from `backends.web` to `backends.minigame`."

---

### Note: Observations

#### A. Complete File Inventory Audit (all 72 Java files accounted for)

Every `.java` file in `backend-web/src/main/java/` has been cross-referenced against the plan's tables:

**"Fork and modify" table (12 entries):** ✅ Complete
- WebGraphics, WebGLGraphics, WebInput, WebFiles, WebFileHandle, WebNet, WebClipboard, WebWindow, WebPlugin, WebClassTransformer, WebClassFilter, **WebPreferences (ADDED)**

**"Copy as-is" table (18 entries):** ✅ Complete after fixes
- WebGL20, WebGL30, WebGL20Debug, WebGL30Debug, gl/*.java (3), gen/Emulate.java, emu/*, KeyCodes, PNG, FileData, HEXCoder, MemoryFileStorage, FileDB, WebAgentInfo, JavaObjectExporterDependency, **TypedArrays (ADDED)**, **ClasspathStorage (ADDED)**, **InternalStorage (ADDED)**, **WebApplicationLogger (ADDED)**

**"Skip entirely" table (17 entries):** ✅ Complete after fixes
- dom/impl/WebWindow, dom/*.java (except typedarray/), assetloader/*, webaudio/howler/*, WebNavigator, WebBaseUrlProvider, WebDefaultBaseUrlProvider, Timer, TeaBuildConfiguration, DefaultWebApp, BaseWebApp, TeaBuilder, JettyServer, indexeddb/*, LocalDBStorage, WebPreloadApplicationListener, WebAssetPreloadListener, WebCursor, **WebAudio (ADDED)**, **WebPermissions (ADDED)**, **WebWindowListener (ADDED)**, **WebTool (ADDED)**, **WebWebAgent (ADDED)**, **dom/audio/Audio.java (ADDED)**

**"Create from scratch" (14 entries):** ✅ Complete
- WX, WXCanvas, TouchCallback, LifecycleCallback, KeyboardCallback, WXAudioContext, MiniGameApplication, MiniGameApplicationConfiguration, MiniGameAudio, MiniGameAssetLoader, MiniGamePreferences, MiniGameBackend, MiniGameBuildConfiguration, adapter/index.js

**Unaccounted:** 0 files. ✅

#### B. Adapter polyfill completeness

The adapter (§5.1) correctly covers:
- `window` polyfill ✅ (needed by gdx.wasm.js: `window.Gdx = ...`)
- `WebAssembly` patch ✅ (WeChat's WXWebAssembly only accepts file paths)
- `atob` polyfill ✅ (needed by freetype.js)
- `console` polyfill ✅ (defensive)

No additional global polyfills are needed because:
- `screen` / `devicePixelRatio` globals are removed by MiniGameGraphics fork (replaced with `wx.getSystemInfoSync()`)
- `setTimeout` is provided natively by WeChat
- `requestAnimationFrame` / `cancelAnimationFrame` are global in WeChat
- `Promise`, `ArrayBuffer`, typed arrays are all available
- `localStorage` / `indexedDB` are replaced by wx.* storage APIs in Java code

#### C. Phase ordering is valid

The dependency graph is correct:
- Phase 6 (build plugin) only needs Phase 1 (scaffolding) — the plugin code doesn't depend on runtime classes
- Phase 5 (adapter) is pure JavaScript, doesn't need Phase 6
- Phase 7 (integration) needs everything else
- Phase ordering 1→2→3→4→5 and 1→6 can proceed in parallel ✅

#### D. game.js async loading is correct

The polling pattern `while (typeof window.Gdx === 'undefined') { await new Promise(r => setTimeout(r, 50)); }` correctly handles:
1. `require('./scripts/gdx.wasm.js')` starts async init ✅
2. `window` polyfill allows `window.Gdx = ...` to work ✅
3. Polling ensures Gdx is available before game code loads ✅
4. Sequential subpackage loading (engine → game) prevents race conditions ✅

