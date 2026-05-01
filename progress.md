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


---

## Review — 2026-05-01 (Infinite Recursion Fix in MiniGameGraphics.java)

### What's correct

1. **Fix is surgical and correct.** Exactly 2 lines changed in `MiniGameGraphics.java`: `getDensity()` → `getNativeScreenDensity()` in both `getPpiX()` and `getPpiY()`. No other modifications to this file.

2. **Call chain is now cycle-free:**
   - `getPpiX()` → `getNativeScreenDensity()` → `WX.getPixelRatio()` → `@JSBody(script = "return wx.getSystemInfoSync().pixelRatio;")` — **terminal leaf**, no further Java calls
   - `getPpiY()` → same path, no cycle
   - `getDensity()` → `getPpiX()` → `getNativeScreenDensity()` → `WX.getPixelRatio()` — no loop because `getPpiX()` no longer calls `getDensity()`

3. **Pattern matches WebGraphics.java exactly.** Both implementations share identical structure:
   - `getPpiX()` / `getPpiY()`: `96f * getNativeScreenDensity()`
   - `getDensity()`: `ppiX / 160f` with overflow guard
   - `getPpcX()` / `getPpcY()`: `ppiX / 2.54f`

4. **Build verification passes:**
   - `:backends:backend-minigame:compileJava` — BUILD SUCCESSFUL ✅
   - `:backends:backend-web:compileJava` — BUILD SUCCESSFUL (no regression) ✅

5. **Terminal call confirmed.** `WX.getPixelRatio()` is a `@JSBody` native method — it executes JavaScript directly and cannot recurse back into Java code.

### Note: Observations

- **Additional files in commit:** The same commit also adds two new files (`MiniGameBuildConfiguration.java` and `MiniGameBackend.java` — 281 lines total). These are Phase 4+5 build infrastructure additions, unrelated to the recursion fix. They were reviewed separately as part of the build backend phase.

- **Original bug confirmed:** Before the fix, `getPpiX()` called `getDensity()`, which called `getPpiX()` — an immediate infinite recursion with no base case. The fix correctly breaks this by having PPI methods call the terminal `getNativeScreenDensity()` directly, matching the established WebGraphics pattern.

- **No issues found.** The fix is minimal, correct, and consistent with the reference implementation.

---

## Review — 2026-05-01 (GWT-Style Native Method Fix in MiniGameInput.java)

### What's correct

1. **`@JSBody` correctly replaces GWT `/*-{ }-*/` syntax.** TeaVM does not support GWT's `native ... /*-{ }-*/` syntax. The fix uses `@JSBody(params = "event", script = "return event.changedTouches;")` which is the idiomatic TeaVM way to write native JavaScript bridges. The parameter name `event` in `params` matches the method signature parameter, and the script is correct JS.

2. **Method is `static` as required by `@JSBody`.** TeaVM's `@JSBody` requires annotated methods to be `static` (they cannot reference `this`). The native method has no dependency on instance state — it only extracts a property from a JS object — so making it static is both correct and semantically appropriate.

3. **Built-in `org.teavm.jso.dom.events.Touch` correctly replaces custom `TouchJS`.** Verified the TeaVM API via `javap` on `teavm-jso-apis-0.13.0.jar`:
   - `int getIdentifier()` — matches usage `touch.getIdentifier()` → `int id` ✅
   - `double getClientX()` — matches usage `(int) touch.getClientX()` ✅
   - `double getClientY()` — matches usage `(int) touch.getClientY()` ✅
   - Extends `JSObject` — compatible with `JSArrayReader<Touch>` ✅

4. **`(int)` casts are correct and necessary.** The old `TouchJS` declared `getClientX()`/`getClientY()` returning `int` via `@JSProperty`. TeaVM's `Touch` returns `double` (matching the W3C Touch spec). The explicit `(int)` casts handle the narrowing conversion correctly. No data loss concern since touch coordinates are pixel values that fit in `int`.

5. **No remaining `TouchJS` references.** `grep` across the entire `backends/` directory returns zero matches. The custom interface was completely removed.

6. **Touch handling logic intact.** The three event types (touchstart=0, touchmove=1, touchend/cancel=2) and their callbacks are unchanged:
   - `TOUCH_DOWN`: records touch, sets `justTouched`, fires `processor.touchDown()` ✅
   - `TOUCH_MOVE`: computes delta, updates position, fires `processor.touchDragged()` ✅
   - `TOUCH_UP`: clears touch, computes final delta, fires `processor.touchUp()` ✅
   - All paths update `mouseLastX`/`mouseLastY` ✅
   - Bounds check `id >= 0 && id < MAX_TOUCHES` preserved ✅

7. **Build verification passes:**
   - `:backends:backend-minigame:compileJava` — BUILD SUCCESSFUL ✅
   - `:backends:backend-web:compileJava` — BUILD SUCCESSFUL (no regression) ✅

8. **Other files in same commit are unrelated.** `MiniGameGraphics.java` had a separate infinite recursion fix (already reviewed). `MiniGameBuildConfiguration.java` and `MiniGameBackend.java` are new build infrastructure files for Phase 4+5. Neither touches input logic.

### Note: Observations

- **Return type is `JSArrayReader<Touch>` not `JSArray<Touch>`.** This is fine — `JSArrayReader` is the read-only supertype and provides `getLength()` and `get(i)`, which is all that's needed here. No mutation of the array is required.

- **Null guard `if (changedTouches == null) return;` is sensible.** Defensive check against malformed touch events from the WeChat runtime.

- **No issues found.** The fix is correct, complete, and follows TeaVM conventions properly.

---

## Review — 2026-05-01 (ClassCastException Fix in Pixmap emu)

### What's correct

1. **Dead code removed cleanly.** The line `WebApplicationConfiguration config = ((WebApplication)Gdx.app).getConfig();` in the `Pixmap(FileHandle)` constructor was the only usage of both `WebApplication` and `WebApplicationConfiguration` in the file. Removing it eliminates the `ClassCastException` that would occur when `Gdx.app` is a `MiniGameApplication` (not a `WebApplication`).

2. **Unused imports correctly removed.** Both `import com.github.xpenatan.gdx.teavm.backends.minigame.WebApplication` and `import com.github.xpenatan.gdx.teavm.backends.minigame.WebApplicationConfiguration` were removed — `grep` confirms zero remaining references to either class.

3. **Pixmap(FileHandle) constructor logic fully preserved.** After the removal, the constructor still:
   - Gets `file.path()` ✅
   - Checks `file.exists()` with descriptive error message ✅
   - Calls `file.readBytes()` to load data ✅
   - Creates `new Gdx2DPixmap(bytes, 0, bytes.length, 0)` ✅

4. **ThrowableEmu.java correctly left unchanged.** Its `WebApplication` usage is legitimate — it calls `WebApplication.printErrorStack(this)` as a static utility for stack trace printing. This is not a cast and does not cause `ClassCastException`.

5. **Build verification passes:**
   - `:backends:backend-minigame:compileJava` — BUILD SUCCESSFUL ✅
   - `:backends:backend-web:compileJava` — BUILD SUCCESSFUL (no regression) ✅

6. **No similar casts found elsewhere.** Searched the entire `backends/backend-minigame/emu/` tree for `((WebApplication)Gdx.app)` and `WebApplicationConfiguration` — zero matches. This was the only occurrence.

### Note: Observations

- **The `config` variable was never used.** Even before this fix, the `config` line was dead code — `config` was declared but never referenced. The removal is purely additive (removes a runtime crash) with no behavioral change.

---

## Review — 2026-05-01 (Multi-Touch touchMap Fix in MiniGameInput.java)

### What's correct

1. **touchMap correctly maps raw JS touch identifiers to sequential pointer indices.** The WeChat/JS touch API assigns arbitrary identifiers (e.g., 0, 3, 7) that may exceed `MAX_TOUCHES=20` and aren't sequential. The `IntMap<Integer> touchMap` properly decouples these from the libGDX pointer index space (0–19).

2. **All three event types use touchMap correctly:**
   - **TOUCH_DOWN (type==0):** `touchMap.put(real, touchId = getAvailablePointer())` — allocates a fresh sequential index, stores the mapping, then uses `touchId` for all array indexing and the `InputProcessor.touchDown()` callback. ✅
   - **TOUCH_MOVE (type==1):** `int touchId = touchMap.get(real, -1)` — looks up the stored mapping with -1 as default. The `if (touchId >= 0)` guard skips orphaned move events gracefully. Uses `touchId` for all array indexing and `touchDragged()` callback. ✅
   - **TOUCH_UP/CANCEL (type==2):** `Integer touchIdObj = touchMap.get(real)` with `null` check — safely handles stale/unknown identifiers. Then `touchMap.remove(real)` cleans up the mapping. Uses `touchId` for all array indexing and `touchUp()` callback. ✅

3. **Sequential touchId is used consistently for ALL array indexing:** `touched[touchId]`, `touchX[touchId]`, `touchY[touchId]`, `deltaX[touchId]`, `deltaY[touchId]` — no raw `real` identifier ever touches these arrays. ✅

4. **Sequential touchId is passed to ALL InputProcessor callbacks:** `touchDown(x, y, touchId, ...)`, `touchDragged(x, y, touchId)`, `touchUp(x, y, touchId, ...)` — no hardcoded `0` as in the old code. ✅

5. **pressedButtons is correctly scoped to pointer 0:**
   - TOUCH_DOWN: `if (touchId == 0) pressedButtons.put(Input.Buttons.LEFT, true)` — only the primary pointer triggers a mouse button press. ✅
   - TOUCH_UP: `if (touchId == 0)` checks if any other touches remain (`for j in 0..MAX_TOUCHES`), and only removes the button press when all touches are released. ✅
   - This matches libGDX scene2d expectations where `isButtonPressed(LEFT)` reflects primary touch state.

6. **justTouched moved outside the per-touch loop.** `if (type == 0) justTouched = true;` is now before the `for` loop, so it's set once per touchstart event rather than once per finger. This matches the libGDX contract (justTouched is true if *any* touch started this frame). ✅

7. **getAvailablePointer() implementation is correct.** Iterates 0..MAX_TOUCHES, returns the first index not present in `touchMap.values()`. Uses `containsValue(i, false)` — the `false` parameter means `.equals()` comparison (not reference identity), which is correct for autoboxed `Integer` values. ✅

8. **Graceful handling of edge cases:**
   - Move for unknown identifier: `touchMap.get(real, -1)` returns -1, `if (touchId >= 0)` skips it — no crash, no corruption. ✅
   - Up/Cancel for unknown identifier: `touchMap.get(real)` returns null, `if (touchIdObj != null)` skips it. ✅
   - All 20 pointers exhausted: `getAvailablePointer()` returns 0 (overwrites pointer 0). Acceptable — 20 simultaneous touches is effectively impossible on mobile. ✅

9. **Old bugs fixed by this change:**
   - Before: all touchDown/touchDragged/touchUp callbacks used hardcoded pointer `0`, breaking multi-touch (second finger overwrites first finger's state). ✅ Fixed
   - Before: `pressedButtons.remove(LEFT)` ran on every touchUp, even if other fingers were still down. ✅ Fixed (now checks `anyTouched`)
   - Before: raw JS identifiers used directly as array indices — potential `ArrayIndexOutOfBoundsException` if identifier ≥ 20. ✅ Fixed

10. **Build verification passes:**
    - `:backends:backend-minigame:compileJava` — BUILD SUCCESSFUL ✅
    - `:backends:backend-web:compileJava` — BUILD SUCCESSFUL (no regression) ✅

11. **Other files in same commit are unrelated to this fix.** `MiniGameGraphics.java` (recursion fix), `MiniGameBuildConfiguration.java` and `MiniGameBackend.java` (build infrastructure), `Pixmap.java` emu (cast fix) — none touch input logic.

### Note: Observations

- **getAvailablePointer() fallback of 0 is safe but worth documenting.** If all 20 pointers are somehow in use, overwriting pointer 0 could cause unexpected behavior. In practice this never happens on mobile devices (max 5–10 touch points). A logging warning could be added in a future iteration.

- **touchMap is never cleared in reset().** This is correct — `reset()` clears per-frame state (deltas, justTouched), but ongoing touch tracking must persist across frames. The map entries are only removed on TOUCH_UP/CANCEL events.

- **TOUCH_UP removes from touchMap before checking anyTouched.** The order is: `touchMap.remove(real)` → `touched[touchId] = false` → `anyTouched` check. Since the removed pointer's `touched[]` is already false by the time the check runs, the `anyTouched` loop correctly won't count it. ✅

- **No issues found.** The implementation is correct, complete, and handles all edge cases properly.

---

## Review — 2026-05-01 (MiniGamePreferences — Load from Storage at Construction)

### What's correct

1. **`WX.getStorageInfoKeys()` binding is correct.** The `@JSBody(script = "return wx.getStorageInfoSync().keys;")` annotation:
   - Has no `params` (correct — no parameters needed)
   - Returns `String[]` (correct — `wx.getStorageInfoSync().keys` returns a string array)
   - Is placed in the `// === Storage ===` section, grouped with `setStorageSync`/`getStorageSync`/`removeStorageSync` ✅
   - Uses the correct WeChat Mini Game API (`wx.getStorageInfoSync()` is synchronous, matching the pattern of all other storage methods) ✅

2. **Constructor loading logic matches WebPreferences exactly.** Side-by-side comparison:
   - Iterates all storage keys (WX array vs. `storage.getLength()` loop) ✅
   - Decodes hex if `shouldEncode` ✅
   - Filters by `key.startsWith(this.prefix)` ✅
   - Reads value from storage using the *encoded* key (`keyEncoded`) ✅
   - Extracts the user key via `key.substring(prefixLength, key.length() - 1)` — strips prefix + type suffix ✅
   - Calls `toObject()` with decoded value if `shouldEncode`, raw value otherwise ✅
   - Stores into `values` map ✅

3. **`toObject()` helper is identical to WebPreferences.** `diff` produces no output — byte-for-byte identical. Correctly handles all 5 types: `b`→Boolean, `i`→Integer, `l`→Long, `f`→Float, default→String ✅

4. **Hex decode path is correct.** When `shouldEncode=true`, the raw storage key is hex-encoded. The code:
   - Decodes the key to check prefix matching: `new String(HEXCoder.decode(keyEncoded))` ✅
   - Reads the value using the *original encoded key*: `WX.getStorageSync(keyEncoded)` ✅
   - Decodes the value for parsing: `new String(HEXCoder.decode(value))` ✅

5. **Error handling clears values on failure.** The `catch(Exception e)` block calls `values.clear()` and prints the stack trace. This prevents partial/corrupt state from being exposed. ✅

6. **Null safety for key array.** `if(allKeys != null)` guards against `getStorageInfoKeys()` returning null (possible if wx API returns undefined). ✅

7. **`flush()` was NOT modified.** The diff confirms `flush()` is unchanged. It still writes all current values to WX storage correctly. ✅

8. **Build verification passes:**
   - `:backends:backend-minigame:compileJava` — BUILD SUCCESSFUL ✅
   - `:backends:backend-web:compileJava` — BUILD SUCCESSFUL (no regression) ✅

9. **Only the two target files were changed for this feature.** `WX.java` (3 lines added) and `MiniGamePreferences.java` (40 lines changed). Other files in the commit (Pixmap, MiniGameGraphics, MiniGameInput, MiniGameBuildConfiguration, MiniGameBackend) are from the broader Phase 4+5 commit, not this fix. ✅

### Note: Observations

- **Pre-existing issue: `flush()` doesn't remove stale keys.** Unlike `WebPreferences.flush()` which first removes all old keys before writing new ones, `MiniGamePreferences.flush()` only writes current values. If a key was previously stored and then removed from the in-memory map (via `remove()`), it will linger in WX storage. This is a pre-existing issue, NOT introduced by this diff. Should be addressed in a follow-up.

- **Key substring logic is correct but subtle.** `key.substring(prefixLength, key.length() - 1)` strips the type suffix (last char: b/i/l/f/s) to recover the original preference key. This matches `toStorageKey()` which appends a type suffix: `prefix + key + "b"`. The round-trip is: userKey → `prefix + userKey + "s"` → `substring(prefixLen, len-1)` → userKey. ✅

- **No issues found with the implementation.** The loading logic is a faithful port of WebPreferences, adapted for WeChat's array-based key enumeration instead of index-based localStorage access.

---

## Review — 2026-05-01 (Audio Context Pooling Fix in MiniGameAudio.java)

### What's correct

1. **Pool architecture is well-designed.** The three-tier acquire strategy (idle pool → create new → steal oldest) is the correct pattern for managing WeChat's ~10 context limit. FIFO stealing via `active.removeIndex(0)` gives LRU eviction semantics.

2. **SoundId tracking via `IntMap<WXAudioContext>` is correct.** Each `play()` call gets a unique monotonically-increasing `nextId++`, stored as the `int` key. All per-playback methods (`stop(id)`, `pause(id)`, `setVolume(id, ...)`) correctly look up the context from this map with null guards.

3. **`onEnded` callback properly cleans up.** The natural-end callback removes from `activePlaybacks`, untracks from active list, and releases to pool. The cleanup chain is: `onEnded` → `activePlaybacks.remove(soundId)` + `untrackActive(ctx)` + `release(ctx)`.

4. **`dispose()` correctly prevents future plays and stops all active.** `disposed = true` flag is checked at the top of `startPlayback()`, returning -1. Then `stop()` is called to release all active contexts.

5. **Looping sounds work correctly.** `setLoop(looping)` is called before `ctx.play()`, so the WeChat runtime knows to loop from the start. `setLooping(soundId, looping)` can change loop state on an active playback.

6. **MiniGameMusic is completely unchanged.** Only a cosmetic whitespace fix (`if(completionListener` → `if (completionListener`) and an added Javadoc comment. No logic changes.

7. **Build verification passes:**
   - `:backends:backend-minigame:compileJava` — BUILD SUCCESSFUL ✅
   - `:backends:backend-web:compileJava` — BUILD SUCCESSFUL (no regression) ✅

8. **Thread safety is not a concern.** libGDX is single-threaded on the render thread, and TeaVM/JS callbacks (`onEnded`) are dispatched on the same JS event loop, so no concurrent access issues.

### Fixed: Critical Bugs

#### Bug 1 (CRITICAL): Context stealing leaves stale references in previous owner's `activePlaybacks`

**Problem:** When `acquire()` steals the oldest active context via `active.removeIndex(0)`, the previous `MiniGameSound` owner still has a stale `soundId → ctx` entry in its `activePlaybacks` IntMap. The old `onEnded` callback is silently replaced by the new one in `startPlayback()`, so the stale entry is never cleaned up. This causes:
- If the old sound calls `stop()` or `stop(soundId)`, it releases a context now owned by a different sound — **killing the new playback**
- The stale entry is a permanent memory leak in the old sound's map

**Fix:** Added `ObjectMap<WXAudioContext, Runnable> stealCallbacks` to `MiniGameAudio`. Each `startPlayback()` registers a steal callback (`() -> activePlaybacks.remove(soundId)`) via `trackActive(ctx, onSteal)`. When `acquire()` steals a context, it invokes the callback to clean up the old owner's state before returning the context. `untrackActive()` and `release()` also clean up the steal callback.

#### Bug 2 (MEDIUM): No guard against double-release into pool

**Problem:** If `onEnded` fires after a manual `stop()` + `release()` call (edge case: audio ends in the same JS microtask), `release()` would call `pool.add(ctx)` for a context already in the pool, creating a duplicate. This could lead to the same context being acquired twice simultaneously.

**Fix:** Added two defenses in `release()`:
1. `ctx.onEnded(() -> {})` — clears the callback before stopping, preventing stale `onEnded` fires
2. `if (pool.contains(ctx, true)) return;` — guards against double-add to pool

### Note: Observations

- **`nextId` long→int cast is safe.** The counter starts at 1 and increments per `play()`. Even at 60 plays/second, it would take ~1 year to overflow `int` (2^31). The same wrapping is applied consistently in both `startPlayback()` and all lookup methods, so it's self-consistent.

- **`pool.contains()` is O(n) but acceptable.** `Array.contains()` scans the array, but `MAX_POOL_SIZE=10` makes this trivial. The alternative (using `ObjectSet`) would add complexity for no measurable performance gain.

- **Steal scenario is a rare edge case.** Pool stealing only triggers when 10 contexts are simultaneously active (all playing) and an 11th play is requested. Most games play 2–4 concurrent SFX. But action games with rapid-fire sounds can hit this, making the fix important for robustness.

- **Only MiniGameAudio.java was modified for this feature.** The diff also includes changes to other files (Pixmap, MiniGameGraphics, MiniGameInput, MiniGamePreferences, WX.java) from the broader commit, but those are unrelated to the audio pooling fix.
