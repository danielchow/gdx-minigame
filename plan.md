# Implementation Plan — WeChat Mini Game Backend Bug Fixes

## Goal
Fix 12 reported issues in the WeChat Mini Game backend framework, verified against source code and WeChat platform documentation.

---

## Issue Verification Summary

| # | Issue | Severity | Verdict | Evidence |
|---|-------|----------|---------|----------|
| 1 | `config.canvas` never set | CRITICAL | ✅ CONFIRMED | `MiniGameApplication.java` never reads `globalThis.canvas`; `MiniGameApplicationConfiguration.canvas` defaults to `null` |
| 2 | `atob` polyfill uses `Buffer.from()` | CRITICAL | ✅ CONFIRMED | `MiniGameBackend.java` `generateAdapter()` emits `Buffer.from()` — a Node.js API absent in WeChat |
| 3 | WebAssembly patch has inverted condition | HIGH | ✅ CONFIRMED | Condition requires `WebAssembly` AND `WXWebAssembly`, but WeChat may not provide `WebAssembly` |
| 4 | `game.json` declares non-existent subpackages | HIGH | ✅ CONFIRMED | `game-code/` directory is never created; only `scripts/` and `assets/` exist |
| 5 | `MiniGameNet` uses `XMLHttpRequest` | HIGH | ✅ CONFIRMED | WeChat docs (02-TechPrinciple.md) map `XMLHttpRequest → wx.request`; no native XHR in Mini Game runtime |
| 6 | `getAudio()` returns null | HIGH | ✅ CONFIRMED | `MiniGameApplication.java` line: `return null;` despite `Gdx.audio` being set correctly |
| 7 | Preferences don't delete removed keys | MEDIUM | ✅ CONFIRMED | `flush()` only writes current values, never calls `WX.removeStorageSync()` for deleted keys |
| 8 | `isLooping()` always returns false | MEDIUM | ✅ CONFIRMED | Hardcoded `return false;` in `MiniGameMusic`; `WXAudioContext` lacks `getLoop()` getter |
| 9 | Touch coordinates unscaled for pixel ratio | MEDIUM | ✅ CONFIRMED | `MiniGameInput` uses raw `clientX/Y` without scaling by pixel ratio |
| 10 | Unused `Window` import in `MiniGameNet` | MEDIUM | ✅ CONFIRMED | `import org.teavm.jso.browser.Window;` is never used; may pull DOM dependencies |
| 11 | Placeholder appId in `project.config.json` | LOW | ✅ CONFIRMED | Default `appId = ""` produces invalid `"appid": ""` in generated config |
| 12 | Busy-wait loop without timeout in `game.js` | LOW | ✅ CONFIRMED | `while (typeof window.Gdx === 'undefined')` loops forever with no max retries |

**Discarded issues:** None — all 12 issues are confirmed.

---

## Fix Order (by dependency and file grouping)

The fixes are ordered so that prerequisites are addressed first. Fixes are grouped by file to minimize context-switching.

### Group A — MiniGameApplication.java (Issues 1 + 6)

These are in the same file and Issue 1 is the #1 blocker — without it the game cannot start.

---

### Task 1: Set `config.canvas` from `globalThis.canvas` (Issue 1)

- **File:** `src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameApplication.java`
- **Evidence:**
  - `MiniGameApplicationConfiguration.java` line 49: `public Object canvas;` — defaults to `null`
  - `MiniGameGraphics.java` line 44: `this.canvas = (HTMLCanvasElement) config.canvas;` — casts null → NPE
  - `MiniGameBackend.java` `generateAdapter()`: `globalThis.canvas = canvas;` in the `setup()` function — the value IS set in JS, but never read into Java config
- **Changes:**
  Add a native JSBody method to read `globalThis.canvas`, and call it in `init()` before creating graphics:

  ```java
  // Add new native method to MiniGameApplication:
  @JSBody(script = "return globalThis.canvas;")
  private static native JSObject getGlobalCanvas();

  // In init(), BEFORE createGraphics(config), add:
  if (config.canvas == null) {
      config.canvas = getGlobalCanvas();
  }
  ```

  Also add the import: `import org.teavm.jso.JSObject;`

- **Acceptance:** `config.canvas` is non-null when `MiniGameGraphics` constructor runs; WebGL context is obtained successfully.

---

### Task 2: Fix `getAudio()` to return the `MiniGameAudio` instance (Issue 6)

- **File:** `src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameApplication.java`
- **Evidence:**
  - Line ~155 in `init()`: `Gdx.audio = new MiniGameAudio();` — correctly sets the singleton
  - Line ~170: `public Audio getAudio() { return null; }` — returns null instead of the instance
- **Changes:**
  1. Add a field: `private MiniGameAudio audio;`
  2. In `init()`, change `Gdx.audio = new MiniGameAudio();` to:
     ```java
     this.audio = new MiniGameAudio();
     Gdx.audio = audio;
     ```
  3. Change `getAudio()`:
     ```java
     @Override
     public Audio getAudio() {
         return audio;
     }
     ```

- **Acceptance:** `Gdx.app.getAudio()` returns the same `MiniGameAudio` instance as `Gdx.audio`.

---

### Group B — MiniGameBackend.java (Issues 2, 3, 4, 11, 12)

All fixes are in the template generation methods of `MiniGameBackend.java`.

---

### Task 3: Fix `atob` polyfill to not use `Buffer.from()` (Issue 2)

- **File:** `src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/config/backend/MiniGameBackend.java`
- **Method:** `generateAdapter()`
- **Evidence:**
  - The adapter template contains: `return Buffer.from(base64, 'base64').toString('binary');`
  - `Buffer` is a Node.js API; WeChat Mini Game runtime does not provide it
- **Changes:**
  Replace the `atob` polyfill block with a pure-JS implementation:

  ```java
  "// === atob polyfill ===\n" +
  "if (typeof atob === 'undefined') {\n" +
  "    var atob = function(base64) {\n" +
  "        var chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/';\n" +
  "        var lookup = {};\n" +
  "        for (var i = 0; i < chars.length; i++) lookup[chars[i]] = i;\n" +
  "        var output = '';\n" +
  "        var buffer = 0, bufferLen = 0;\n" +
  "        for (var j = 0; j < base64.length; j++) {\n" +
  "            var ch = base64.charAt(j);\n" +
  "            if (ch === '=') break;\n" +
  "            buffer = (buffer << 6) | lookup[ch];\n" +
  "            bufferLen += 6;\n" +
  "            if (bufferLen >= 8) {\n" +
  "                bufferLen -= 8;\n" +
  "                output += String.fromCharCode((buffer >> bufferLen) & 0xFF);\n" +
  "            }\n" +
  "        }\n" +
  "        return output;\n" +
  "    };\n" +
  "    globalThis.atob = atob;\n" +
  "}\n\n"
  ```

- **Acceptance:** The generated `adapter/index.js` contains no reference to `Buffer`. Calling `atob("SGVsbG8=")` returns `"Hello"`.

---

### Task 4: Fix WebAssembly patch condition (Issue 3)

- **File:** `src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/config/backend/MiniGameBackend.java`
- **Method:** `generateAdapter()`
- **Evidence:**
  - Current condition: `typeof WXWebAssembly !== 'undefined' && typeof WebAssembly !== 'undefined'`
  - WeChat provides `WXWebAssembly` but may NOT provide `WebAssembly` globally (per 02-TechPrinciple.md: the adapter layer's job is to bridge this gap)
  - The intent is: when `WXWebAssembly` IS available, patch/alias `WebAssembly` to use it
- **Changes:**
  Replace the WASM patch block:

  ```java
  "// === WebAssembly patch ===\n" +
  "if (typeof WXWebAssembly !== 'undefined') {\n" +
  "    if (typeof WebAssembly === 'undefined') {\n" +
  "        globalThis.WebAssembly = {};\n" +
  "    }\n" +
  "    var originalInstantiate = WebAssembly.instantiate;\n" +
  "    WebAssembly.instantiate = function(source, imports) {\n" +
  "        if (source instanceof ArrayBuffer || source instanceof Uint8Array) {\n" +
  "            return WXWebAssembly.instantiate('scripts/gdx.wasm', imports);\n" +
  "        }\n" +
  "        return originalInstantiate ? originalInstantiate(source, imports) : Promise.reject(new Error('No WASM support'));\n" +
  "    };\n" +
  "}\n\n"
  ```

- **Acceptance:** When only `WXWebAssembly` exists (no `WebAssembly`), the patch still runs and creates the necessary shim.

---

### Task 5: Remove non-existent `game-code/` subpackage from `game.json` (Issue 4)

- **File:** `src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/config/backend/MiniGameBackend.java`
- **Method:** `generateGameJson()`
- **Evidence:**
  - `generateGameJson()` declares 3 subpackages: `scripts/`, `game-code/`, `assets/`
  - `copyAssets()` only creates `scripts/` and `assets/` — no `game-code/` directory is ever created
  - The main compiled JS file (`app.js`) is written to the root output directory, not `game-code/`
  - `generateGameJs()` tries to `wx.loadSubpackage({ name: 'game' })` which will fail
- **Changes:**
  1. Remove the `game-code/` subpackage entry from `game.json`
  2. Update `generateGameJs()` to not try to load the non-existent "game" subpackage
  3. Load the compiled JS directly from root (it already does `require('./app.js')`)

  ```java
  private void generateGameJson() {
      String json = "{\n" +
          "    \"deviceOrientation\": \"" + orientation + "\",\n" +
          "    \"networkTimeout\": {\n" +
          "        \"request\": 5000,\n" +
          "        \"connectSocket\": 5000,\n" +
          "        \"uploadFile\": 5000,\n" +
          "        \"downloadFile\": 500000\n" +
          "    },\n" +
          "    \"subpackages\": [\n" +
          "        { \"name\": \"engine\", \"root\": \"scripts/\" },\n" +
          "        { \"name\": \"assets\", \"root\": \"assets/\" }\n" +
          "    ]\n" +
          "}\n";

      FileHandle gameJson = releasePath.child("game.json");
      gameJson.writeString(json, false);
  }
  ```

  In `generateGameJs()`, remove these lines:
  ```
  // Load game code
  if (typeof wx.loadSubpackage === 'function') {
      await wx.loadSubpackage({ name: 'game' }).promise;
  }
  ```

- **Acceptance:** `game.json` only declares subpackages whose directories actually exist. `game.js` no longer attempts to load a non-existent subpackage.

---

### Task 6: Add timeout to busy-wait loop in `game.js` (Issue 12)

- **File:** `src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/config/backend/MiniGameBackend.java`
- **Method:** `generateGameJs()`
- **Evidence:**
  - The generated `game.js` contains:
    ```javascript
    while (typeof window.Gdx === 'undefined') {
        await new Promise(r => setTimeout(r, 50));
    }
    ```
  - If WASM initialization fails silently, this loops forever with no user-visible error
- **Changes:**
  Replace the infinite while loop with a bounded retry:

  ```javascript
  // Wait for Gdx to be available (with timeout)
  var gdxWaitStart = Date.now();
  while (typeof window.Gdx === 'undefined') {
      if (Date.now() - gdxWaitStart > 30000) {
          throw new Error('Timed out waiting for Gdx to initialize (30s)');
      }
      await new Promise(r => setTimeout(r, 50));
  }
  ```

- **Acceptance:** If WASM fails to load, the game shows an error within 30 seconds instead of hanging silently.

---

### Task 7: Add warning for empty appId (Issue 11)

- **File:** `src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/config/backend/MiniGameBackend.java`
- **Method:** `generateProjectConfigJson()` or `setup()`
- **Evidence:**
  - `MiniGameBuildConfiguration.java` line 8: `public String appId = "";`
  - `MiniGameBackend.java` line 13: `public String appId = "";`
  - Empty appId produces `"appid": ""` in `project.config.json`, which will cause WeChat DevTools to fail in production
- **Changes:**
  Add a warning in `setupMinigame()`:

  ```java
  private void setupMinigame(TeaCompilerData data) {
      if (appId == null || appId.isEmpty()) {
          System.out.println("[MiniGameBackend] WARNING: appId is empty. " +
              "Set appId via MiniGameBackend.setAppId() for production builds.");
      }
      generateGameJson();
      generateProjectConfigJson();
      generateGameJs(data);
      generateAdapter();
  }
  ```

- **Acceptance:** Console warning appears during build when appId is empty. Production users are alerted.

---

### Group C — MiniGameNet.java (Issues 5 + 10)

---

### Task 8: Replace `XMLHttpRequest` with `wx.request()` (Issue 5)

- **File:** `src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameNet.java`
- **Evidence:**
  - Line 1: `import org.teavm.jso.browser.Window;` (unused, see Issue 10)
  - Line 49: `org.teavm.jso.ajax.XMLHttpRequest request = org.teavm.jso.ajax.XMLHttpRequest.create();`
  - WeChat docs (02-TechPrinciple.md, adapter table): `XMLHttpRequest → wx.request`
  - `WX.java` already has the binding: `@JSBody(params = "options", script = "wx.request(options);")`
- **Changes:**
  Rewrite `sendHttpRequest()` to use `wx.request()` via TeaVM JSBody calls. The approach:
  1. Build the request options object using `JSObject` and TeaVM JSBody helpers
  2. Call `WX.request(options)` 
  3. Handle success/failure callbacks

  High-level structure (replacing the entire `sendHttpRequest` method):
  ```java
  @Override
  public void sendHttpRequest(final HttpRequest httpRequest, HttpResponseListener httpResponseListener) {
      httpResponseListeners.put(httpRequest, httpResponseListener);

      String method = httpRequest.getMethod();
      String url = httpRequest.getUrl();
      Map<String, String> headers = httpRequest.getHeaders();

      String content = httpRequest.getContent();
      if (content == null) {
          InputStream input = httpRequest.getContentStream();
          if (input != null) {
              try {
                  content = StreamUtils.copyStreamToString(input);
              } catch (IOException e) {
                  throw new GdxRuntimeException("Error reading string from stream.", e);
              }
          }
      }

      boolean doingOutput = (content != null) && (method.equalsIgnoreCase(HttpMethods.POST) ||
              method.equalsIgnoreCase(HttpMethods.PUT));

      String finalUrl = url;
      String finalContent = content;
      if ((content != null) && (!"".equals(content)) && (!doingOutput)) {
          finalUrl = url + "?" + content;
          finalContent = null;
      }

      String finalUrl2 = finalUrl;
      String finalContent2 = doingOutput ? finalContent : null;
      Map<String, String> finalHeaders = headers;

      // Build and send wx.request via native JS
      sendWxRequest(method, finalUrl2, finalContent2, finalHeaders,
          (status, responseText, headerString) -> {
              HttpResponseListener listener = httpResponseListeners.remove(httpRequest);
              if (listener != null) {
                  // Parse headers and create HttpResponse
                  // ... (build response object as before)
                  listener.handleHttpResponse(buildResponse(status, responseText, headerString));
              }
          },
          (error) -> {
              HttpResponseListener listener = httpResponseListeners.remove(httpRequest);
              if (listener != null) {
                  listener.failed(new GdxRuntimeException("Request failed: " + error));
              }
          }
      );
  }
  ```

  Add native helper methods:
  ```java
  @JSBody(params = {"method", "url", "data", "headers", "onSuccess", "onFail"},
      script = "wx.request({" +
      "  url: url, method: method, data: data, header: headers," +
      "  success: function(res) { onSuccess(res.statusCode, res.data, '');" +
      "    // Note: response headers available via res.header object" +
      "  }," +
      "  fail: function(err) { onFail(err.errMsg || 'Unknown error'); }" +
      "});")
  private static native void sendWxRequest(String method, String url, String data,
      Map<String, String> headers, SuccessCallback onSuccess, ErrorCallback onFail);
  ```

  **Note:** This is the most complex fix. A simpler intermediate approach is to create a thin wrapper that maps `wx.request` to an XMLHttpRequest-like interface in the adapter, but the cleaner fix is to use `wx.request` directly from Java via JSBody.

  **Simpler alternative:** Add an XHR polyfill to `adapter/index.js` that wraps `wx.request`:
  ```javascript
  // In adapter/index.js, add XMLHttpRequest polyfill using wx.request
  if (typeof XMLHttpRequest === 'undefined') {
      globalThis.XMLHttpRequest = function() { /* wx.request wrapper */ };
  }
  ```
  This approach avoids rewriting `MiniGameNet.java` entirely and lets TeaVM's existing XMLHttpRequest JSO work.

  **Recommended approach:** Use the adapter polyfill (simpler, less risk).

- **Acceptance:** HTTP requests succeed in WeChat Mini Game runtime using `wx.request()`.

---

### Task 9: Remove unused `Window` import from `MiniGameNet` (Issue 10)

- **File:** `src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameNet.java`
- **Evidence:**
  - Line 1: `import org.teavm.jso.browser.Window;` — not referenced anywhere in the file
  - TeaVM may pull in DOM-related JavaScript when it sees `org.teavm.jso.browser.Window` imported, increasing output size
- **Changes:**
  Remove the line: `import org.teavm.jso.browser.Window;`

- **Acceptance:** The import is gone; `MiniGameNet.java` compiles without it; output bundle is no larger.

---

### Group D — MiniGamePreferences.java (Issue 7)

---

### Task 10: Flush should remove deleted keys from WX storage (Issue 7)

- **File:** `src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGamePreferences.java`
- **Evidence:**
  - `flush()` (line ~88) iterates `values` map and writes each key — but never removes keys that were deleted via `remove()`
  - `remove()` (line ~172) only removes from the in-memory `values` map
  - `WX.java` already has `removeStorageSync()`: `@JSBody(params = "key", script = "wx.removeStorageSync(key);")`
- **Changes:**
  Track keys that have been removed, and clean them up during `flush()`. Add a `removedKeys` set:

  ```java
  // Add field:
  private final com.badlogic.gdx.utils.Array<String> removedKeys = new com.badlogic.gdx.utils.Array<>();

  // Modify remove():
  @Override
  public void remove(String key) {
      values.remove(key);
      removedKeys.add(key);
  }

  // Modify clear():
  @Override
  public void clear() {
      for (String key : values.keys()) {
          removedKeys.add(key);
      }
      values.clear();
  }

  // Modify flush() — add cleanup BEFORE writing new values:
  @Override
  public void flush() {
      try {
          // Remove deleted keys from WX storage
          for (int i = 0; i < removedKeys.size; i++) {
              String key = removedKeys.get(i);
              // We don't know the old type suffix, so try all possible storage keys
              String[] suffixes = {"b", "i", "l", "f", "s"};
              for (String suffix : suffixes) {
                  String storageKey = prefix + key + suffix;
                  if (shouldEncode) {
                      storageKey = HEXCoder.encode(storageKey.getBytes());
                  }
                  WX.removeStorageSync(storageKey);
              }
          }
          removedKeys.clear();

          // Write all values to WX storage (existing code)
          for (String key : values.keys()) {
              // ... existing write logic ...
          }
      } catch (Exception e) {
          throw new GdxRuntimeException("Couldn't flush preferences", e);
      }
  }
  ```

- **Acceptance:** After `prefs.remove("key"); prefs.flush();`, the key no longer exists in WX storage. On next app launch, the key is not re-loaded.

---

### Group E — WXAudioContext.java + MiniGameAudio.java (Issue 8)

---

### Task 11: Add `getLoop()` getter to `WXAudioContext` and use it in `isLooping()` (Issue 8)

- **File 1:** `src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/bindings/WXAudioContext.java`
- **File 2:** `src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameAudio.java`
- **Evidence:**
  - `WXAudioContext.java` has `@JSProperty void setLoop(boolean loop);` but no `boolean getLoop()` getter
  - WeChat `InnerAudioContext.loop` is a read-write boolean property (documented in WeChat API)
  - `MiniGameAudio.java` inner class `MiniGameMusic` line: `public boolean isLooping() { return false; // No getter available }` — the comment is wrong; the getter just isn't declared
- **Changes:**
  In `WXAudioContext.java`, add the getter:
  ```java
  @JSProperty
  boolean getLoop();
  ```

  In `MiniGameAudio.java`, fix `isLooping()`:
  ```java
  @Override
  public boolean isLooping() {
      return audioContext.getLoop();
  }
  ```

- **Acceptance:** `music.isLooping()` returns `true` after `music.setLooping(true)`, `false` after `music.setLooping(false)`.

---

### Group F — MiniGameInput.java (Issue 9)

---

### Task 12: Scale touch coordinates by pixel ratio (Issue 9)

- **File:** `src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameInput.java`
- **Evidence:**
  - `handleTouchEvent()` line: `int x = (int) touch.getClientX();` and `int y = (int) touch.getClientY();`
  - WeChat docs (02-TechPrinciple.md) state: "将触摸坐标转换为 Canvas 相对坐标，考虑设备像素比" with formula: `x = (touch.pageX - rect.left) * (canvas.width / rect.width)`
  - `MiniGameGraphics` provides `getNativeScreenDensity()` via `WX.getPixelRatio()`
  - If the canvas physical dimensions differ from CSS dimensions (e.g., for sharp rendering on high-DPI devices), raw touch coordinates will be wrong
- **Changes:**
  Scale touch coordinates by the ratio of canvas dimensions to screen dimensions:

  ```java
  private void handleTouchEvent(JSObject event, int type) {
      currentEventTimeStamp = (int)(TimeUtils.nanoTime() / 1000000);
      JSArrayReader<Touch> changedTouches = getChangedTouches(event);
      if (changedTouches == null) return;

      // Scale factor: canvas physical pixels / CSS pixels
      float scaleX = (float) canvas.getWidth() / (float) WX.getScreenWidth();
      float scaleY = (float) canvas.getHeight() / (float) WX.getScreenHeight();

      if (type == 0) justTouched = true;

      for (int i = 0; i < changedTouches.getLength(); i++) {
          Touch touch = changedTouches.get(i);
          int real = touch.getIdentifier();
          int x = Math.round((float) touch.getClientX() * scaleX);
          int y = Math.round((float) touch.getClientY() * scaleY);

          // ... rest of the handler unchanged ...
      }
  }
  ```

  Add import: `import com.github.xpenatan.gdx.teavm.backends.minigame.bindings.WX;` (already imported? Check.)

  **Note:** `WX.getScreenWidth()` returns `double`. The canvas created by `wx.createCanvas()` has default dimensions matching CSS pixels, so in the default case `scaleX == scaleY == 1.0` and coordinates are unchanged. This fix handles the case where the canvas is later resized for high-DPI rendering.

- **Acceptance:** Touch coordinates correctly map to canvas coordinates regardless of pixel ratio scaling.

---

## Files to Modify

| File | Issues | Tasks |
|------|--------|-------|
| `src/main/java/.../MiniGameApplication.java` | 1, 6 | 1, 2 |
| `src/main/java/.../config/backend/MiniGameBackend.java` | 2, 3, 4, 11, 12 | 3, 4, 5, 6, 7 |
| `src/main/java/.../MiniGameNet.java` | 5, 10 | 8, 9 |
| `src/main/java/.../MiniGamePreferences.java` | 7 | 10 |
| `src/main/java/.../bindings/WXAudioContext.java` | 8 | 11 |
| `src/main/java/.../MiniGameAudio.java` | 8 | 11 |
| `src/main/java/.../MiniGameInput.java` | 9 | 12 |

## New Files

None required. All fixes modify existing files.

## Dependencies

```
Task 1 (canvas) ───── must be first; blocks all rendering
Task 2 (getAudio) ──── independent, but same file as Task 1
Task 3 (atob) ──────── independent
Task 4 (WASM) ──────── independent but same file as Task 3
Task 5 (subpackages) ─ independent but same file as Task 3
Task 6 (timeout) ────── independent but same file as Task 3
Task 7 (appId) ──────── independent but same file as Task 3
Task 8 (wx.request) ── independent; most complex fix
Task 9 (Window import) ─ independent but same file as Task 8; do together
Task 10 (prefs flush) ─ independent
Task 11 (isLooping) ─── independent; WXAudioContext change must precede MiniGameAudio change
Task 12 (touch coords) ─ independent
```

Recommended execution order: **1 → 2 → 3 → 4 → 5 → 6 → 7 → 8 → 9 → 10 → 11 → 12**

(Proceeding file-by-file through the groups above.)

## Risks

1. **Task 8 (wx.request rewrite)** is the riskiest change. The recommended approach is adding an `XMLHttpRequest` polyfill to the adapter that wraps `wx.request()`, rather than rewriting all of `MiniGameNet.java`. This preserves the existing TeaVM XMLHttpRequest JSO binding and minimizes code changes.
2. **Task 12 (touch scaling)** is safe when `scaleX == scaleY == 1.0` (default case), but should be tested on high-DPI devices with the canvas resized to physical pixels.
3. **Task 10 (preferences flush)** tries all 5 type suffixes when removing keys. This is slightly inefficient but correct — `wx.removeStorageSync` on a non-existent key is a no-op.
4. **Task 4 (WASM patch)** creates an empty `WebAssembly` object if it doesn't exist. If other code tries to use `WebAssembly.compile` or other methods before `instantiate` is called, it will fail. This is unlikely since the only WASM usage in this framework goes through `instantiate`.
