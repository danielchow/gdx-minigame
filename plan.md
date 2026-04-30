# gdx-minigame: WeChat Mini Game Backend — Implementation Plan

> Status: Reviewed & Actionable
> Source research: `~/Develop/Private/legacy/Solitaire/docs/backlog/minigame/research.md`

---

## Overview

Create a `backend-minigame` module in gdx-minigame that compiles LibGDX games to run on WeChat Mini Game, replacing all browser/DOM dependencies with direct `wx.*` API calls.

**Strategy:** Fork `backend-web` → strip DOM → add `wx.*` bindings → new `MiniGameBackend` for build configuration.

---

## Pre-req: Groundwork

### P0. Fix snapshot versioning
**File:** `buildSrc/src/main/kotlin/LibExt.kt`
**Problem:** `getVersion()` returns `-SNAPSHOT` (no base version) for snapshots.
**Fix:** Read `version` from `gradle.properties` and append `-SNAPSHOT`.

In `getVersion()`, change:
```kotlin
var libVersion = "-SNAPSHOT"
```
to:
```kotlin
var libVersion = "${version}-SNAPSHOT"
```

Also add `version=1.5.4` to `gradle.properties` if not present.

### P1. Update SCM/POM metadata
**File:** `buildSrc/src/main/kotlin/publish.gradle.kts`
**Change:** Update `pom.url`, `pom.scm.connection`, `pom.scm.url` from `xpenatan/gdx-teavm` to new repo URL. Update developer info.

**Verification:** `./gradlew :backends:backend-web:publishToMavenLocal` succeeds.

---

## Phase 1: Module Scaffolding

### 1.1 Create `backend-minigame` module directory

```
backends/backend-minigame/
├── build.gradle.kts
├── src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/
│   ├── bindings/                 ← wx.* @JSBody wrappers
│   ├── config/                   ← MiniGameBackend, build config
│   └── ...                       ← Runtime replacement classes
├── src/main/resources/
│   └── META-INF/
│       ├── teavm.properties      ← Package mapping for emulations
│       └── services/
│           ├── org.teavm.vm.spi.TeaVMPlugin
│           └── org.teavm.vm.spi.ElementFilter
├── emu/                          ← Copied from backend-web (see 1.4)
└── gen/                          ← Copied Emulate.java annotation (see 1.5)
```

### 1.2 `build.gradle.kts`
Same as backend-web but **without** Jetty dependency:

```kotlin
plugins { id("java-library") }
sourceSets["main"].java.setSrcDirs(mutableSetOf("emu", "gen", "src/main/java/"))
dependencies {
    api(project(":backends:backend-shared"))
    implementation("com.badlogicgames.gdx:gdx:${LibExt.gdxVersion}")
    implementation("com.github.xpenatan:jMultiplatform:${LibExt.jMultiplatform}")
}
publishing { /* same pattern as backend-web */ }
```

**Note:** Do NOT depend on `backend-web`. Copy the needed DOM-free classes (WebGL20, etc.) instead. This keeps the modules independent.

### 1.3 Register in `settings.gradle.kts`
```kotlin
include(":backends:backend-minigame")
```

### 1.4 Add to publish targets
**File:** `buildSrc/src/main/kotlin/publish.gradle.kts`
Add `project(":backends:backend-minigame")` to `libProjects` set.

### 1.5 Copy `emu/` from backend-web
The emulations are DOM-independent class replacements. Copy the entire `emu/` directory.

**Two emu files have DOM deps** — must be patched:

| File | DOM Dep | Fix |
|------|---------|-----|
| `emu/com/badlogic/gdx/scenes/scene2d/utils/UIUtils.java` | `WebNavigator.getPlatform()` → `navigator.platform` | Replace with `@JSBody(script = "return wx.getSystemInfoSync().platform;")` |
| `emu/com/badlogic/gdx/utils/async/AsyncResult.java` | `Window.setTimeout(this, 0)` | Replace with `@JSBody(params = {"cb", "ms"}, script = "setTimeout(cb, ms);")` wrapper |

**Also copy these emu files outside `emu/` that are part of the emulation system:**
- `backends/backend-web/emu/com/badlogic/gdx/utils/reflect/FieldGen.java`
- `backends/backend-web/emu/com/badlogic/gdx/assets/EMU_AssetManagerUtils.java`

### 1.6 Copy `Emulate.java` annotation
**Source:** `backends/backend-web/src/main/java/.../gen/Emulate.java`
**Destination:** `backends/backend-minigame/gen/.../gen/Emulate.java` (same package)

Two emu files import this annotation:
- `emu/emulate/java/lang/ThrowableEmu.java`
- `emu/emulate/java/lang/ClassEmu.java`

The annotation package must match: `com.github.xpenatan.gdx.teavm.backends.web.gen.Emulate`

### 1.7 Create `teavm.properties` for emulation package mapping
**File:** `src/main/resources/META-INF/teavm.properties`
```properties
mapPackageHierarchy|emu.java=java
mapPackageHierarchy|emu.com=com
mapPackageHierarchy|emu.org=org
mapPackageHierarchy|emu.net=net
```

### 1.8 Create SPI service files
**File:** `src/main/resources/META-INF/services/org.teavm.vm.spi.TeaVMPlugin`
```
com.github.xpenatan.gdx.teavm.backends.minigame.config.plugins.MiniGamePlugin
```

**File:** `src/main/resources/META-INF/services/org.teavm.vm.spi.ElementFilter`
```
com.github.xpenatan.gdx.teavm.backends.minigame.config.plugins.MiniGameClassFilter
```

Copy and adapt from backend-web's `WebPlugin.java` and `WebClassFilter.java`.

### 1.9 Copy DOM-free classes from backend-web (DO NOT REWRITE)
These classes have zero DOM dependencies and can be copied as-is:

| Source File | Lines | What it does |
|-------------|-------|-------------|
| `WebGL20.java` | 1115 | WebGL 1 wrapper — pure `@JSBody` WebGL calls |
| `WebGL30.java` | 839 | WebGL 2 wrapper — same pattern (won't run on WeChat but harmless) |
| `WebGL20Debug.java` | 896 | Debug wrapper around WebGL20 |
| `WebGL30Debug.java` | 1611 | Debug wrapper around WebGL30 |
| `gl/WebGLRenderingContextExt.java` | ? | WebGL context extension interface |
| `gl/WebGL2RenderingContextExt.java` | ? | WebGL2 context extension interface |
| `gl/WebGLContextAttributesExt.java` | ? | WebGL context attributes |
| `agent/WebAgentInfo.java` | ? | Agent info data class |
| `utils/KeyCodes.java` | ? | Keyboard code mapping |
| `utils/PNG.java` | ? | PNG utility |
| `filesystem/FileData.java` | ? | File data interface |
| `filesystem/HEXCoder.java` | ? | Hex encoding |
| `filesystem/MemoryFileStorage.java` | ? | In-memory file storage |
| `filesystem/FileDB.java` | ? | File database |
| `filesystem/indexeddb/*` | ? | IndexedDB emulations — **SKIP** (not available on WeChat) |

**When copying:** Change package from `com.github.xpenatan.gdx.teavm.backends.web` to `com.github.xpenatan.gdx.teavm.backends.minigame` and update all imports accordingly.

**Verification:** `./gradlew :backends:backend-minigame:compileJava` passes with all copied files.

---

## Phase 2: WeChat API Bindings (`bindings/`)

### 2.1 `WX.java` — Core wx.* bindings
All static `@JSBody` methods. No DOM references.

```java
package com.github.xpenatan.gdx.teavm.backends.minigame.bindings;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.browser.AnimationFrameCallback;

public abstract class WX extends JSObject {
    // Canvas
    @JSBody(script = "return wx.createCanvas();")
    public static native JSObject createCanvas();

    // System info — IMPORTANT: cache results, don't call getSystemInfoSync() repeatedly
    @JSBody(script = "return wx.getSystemInfoSync().pixelRatio;")
    public static native double getPixelRatio();

    @JSBody(script = "return wx.getSystemInfoSync().screenWidth;")
    public static native double getScreenWidth();

    @JSBody(script = "return wx.getSystemInfoSync().screenHeight;")
    public static native double getScreenHeight();

    @JSBody(script = "return wx.getSystemInfoSync().platform;")
    public static native String getPlatform();

    // requestAnimationFrame — bare global, NOT on window
    @JSBody(params = "callback", script = "return requestAnimationFrame(callback);")
    public static native int requestAnimationFrame(AnimationFrameCallback callback);

    @JSBody(params = "id", script = "cancelAnimationFrame(id);")
    public static native void cancelAnimationFrame(int id);

    // Frame rate control
    @JSBody(params = "fps", script = "wx.setPreferredFramesPerSecond(fps);")
    public static native void setPreferredFramesPerSecond(int fps);

    // Touch events
    @JSBody(params = "callback", script = "wx.onTouchStart(callback);")
    public static native void onTouchStart(TouchCallback callback);

    @JSBody(params = "callback", script = "wx.onTouchMove(callback);")
    public static native void onTouchMove(TouchCallback callback);

    @JSBody(params = "callback", script = "wx.onTouchEnd(callback);")
    public static native void onTouchEnd(TouchCallback callback);

    @JSBody(params = "callback", script = "wx.onTouchCancel(callback);")
    public static native void onTouchCancel(TouchCallback callback);

    // Audio
    @JSBody(script = "return wx.createInnerAudioContext();")
    public static native JSObject createInnerAudioContext();

    // Storage
    @JSBody(params = {"key", "value"}, script = "wx.setStorageSync(key, value);")
    public static native void setStorageSync(String key, String value);

    @JSBody(params = "key", script = "return wx.getStorageSync(key);")
    public static native String getStorageSync(String key);

    @JSBody(params = "key", script = "wx.removeStorageSync(key);")
    public static native void removeStorageSync(String key);

    // File system
    @JSBody(script = "return wx.getFileSystemManager();")
    public static native JSObject getFileSystemManager();

    @JSBody(script = "return wx.env.USER_DATA_PATH;")
    public static native String getUserDataPath();

    // HTTP
    @JSBody(params = "options", script = "wx.request(options);")
    public static native void request(JSObject options);

    // Lifecycle
    @JSBody(params = "callback", script = "wx.onShow(callback);")
    public static native void onShow(LifecycleCallback callback);

    @JSBody(params = "callback", script = "wx.onHide(callback);")
    public static native void onHide(Runnable callback);

    // Clipboard
    @JSBody(params = "data", script = "wx.setClipboardData({data: data});")
    public static native void setClipboardData(String data);

    // Keyboard
    @JSBody(params = "options", script = "wx.showKeyboard(options);")
    public static native void showKeyboard(JSObject options);

    @JSBody(script = "wx.hideKeyboard();")
    public static native void hideKeyboard();

    @JSBody(params = "callback", script = "wx.onKeyboardInput(callback);")
    public static native void onKeyboardInput(KeyboardCallback callback);

    // Vibration
    @JSBody(script = "wx.vibrateLong();")
    public static native void vibrateLong();

    @JSBody(script = "wx.vibrateShort({type:'heavy'});")
    public static native void vibrateShort();
}
```

### 2.2 `WXCanvas.java` — Canvas overlay type
```java
package com.github.xpenatan.gdx.teavm.backends.minigame.bindings;

import org.teavm.jso.JSMethod;
import org.teavm.jso.JSProperty;
import org.teavm.jso.JSObject;

public abstract class WXCanvas extends JSObject {
    @JSProperty public abstract int getWidth();
    @JSProperty public abstract void setWidth(int w);
    @JSProperty public abstract int getHeight();
    @JSProperty public abstract void setHeight(int h);
    @JSMethod public abstract JSObject getContext(String type);
}
```

### 2.3 Callback functors (`@JSFunctor` interfaces)
```java
// TouchCallback.java
@JSFunctor
public interface TouchCallback extends JSObject {
    void invoke(JSObject event);
}

// LifecycleCallback.java
@JSFunctor
public interface LifecycleCallback extends JSObject {
    void invoke(JSObject result);
}

// KeyboardCallback.java
@JSFunctor
public interface KeyboardCallback extends JSObject {
    void invoke(JSObject result);
}
```

### 2.4 `WXAudioContext.java` — Audio object wrapper
Wrap the object returned by `wx.createInnerAudioContext()`:

```java
public abstract class WXAudioContext extends JSObject {
    @JSProperty public abstract void setSrc(String src);
    @JSProperty public abstract String getSrc();
    @JSProperty public abstract void setLoop(boolean loop);
    @JSProperty public abstract void setVolume(double volume);
    @JSMethod public abstract void play();
    @JSMethod public abstract void pause();
    @JSMethod public abstract void stop();
    @JSMethod public abstract void onEnded(Runnable callback);
    @JSMethod public abstract void onError(Runnable callback);
}
```

**Verification:** Module compiles with all TeaVM annotations resolved.

---

## Phase 3: Runtime Replacements

These classes replace their `Web*` counterparts. Each removes DOM dependencies and uses `WX.*` or bare `@JSBody` calls.

### 3.1 `MiniGameApplication.java` (replaces `WebApplication.java`)

**Source to fork:** `backends/backend-web/.../WebApplication.java` (572 lines)
**Constructor:** `WebApplication(ApplicationListener, ApplicationListener, WebApplicationConfiguration)`

**DOM dependencies in WebApplication (complete inventory):**

| Line(s) | DOM Call | Replacement |
|---------|---------|-------------|
| Constructor | `WebWindow.get()` → `Window.current()` | Remove `WebWindow` field entirely. Canvas passed via config. |
| Constructor | `WebWindow.addEventListener("pagehide", ...)` | `WX.onHide(runnable)` |
| Constructor | `document.addEventListener("visibilitychange", ...)` + `document.getVisibilityState()` | `WX.onShow(callback)` / `WX.onHide(runnable)` |
| Constructor | `window.addEventListener("resize", ...)` + `window.getClientWidth/Height()` | `WX.onWindowResize()` or poll `WX.getScreenWidth/Height()` |
| Game loop | `window.requestAnimationFrame(this)` via `WebWindow` | `WX.requestAnimationFrame(callback)` |
| `initNativeLibraries` | `assetLoader.loadScript("gdx.wasm.js", ...)` via DOM `<script>` injection | `WX.requireModule("scripts/gdx.wasm.js")` — synchronous `require()` |
| `initAudio` | `assetLoader.loadScript("howler.js", ...)` + `new HowlTeaAudio()` | `new MiniGameAudio()` — no script loading needed |
| `getPreferences` | `Storage.getLocalStorage()` + `new WebPreferences(storage, ...)` | `new MiniGamePreferences(name)` — uses `wx.setStorageSync` |
| `init` | `new AssetDownloadImpl(...)` + `new AssetLoadImpl(baseUrl, ...)` | `new MiniGameAssetLoader(baseUrl, ...)` — uses `wx.getFileSystemManager()` |
| `init` | `WebWebAgent.computeAgentInfo()` | Hardcode or use `WX.getSystemInfoSync()` |
| `init` | `assetLoader.setupFileDrop(graphics.canvas, this)` | Remove — no drag-and-drop on mobile |

**New constructor signature:**
```java
public MiniGameApplication(ApplicationListener appListener, MiniGameApplicationConfiguration config)
```

**Game loop pattern:**
```java
Runnable appLoop = new Runnable() {
    @Override
    public void run() {
        step(curListener);
        if(!stepError) {
            WX.requestAnimationFrame(callback -> this.run());
        }
    }
};
```

**Native library loading (CRITICAL — async gdx.wasm.js):**
`gdx.wasm.js` self-executes: `window.Gdx = await Gdx()`. This is **async** — `require()` returns before `Gdx` is set on `globalThis`.

**Solution:** Add a polling wait or use the adapter's `window` polyfill + `WebAssembly` patch (see Phase 5). The game.js loading flow in Phase 4 handles this by loading subpackages sequentially.

For the Java side, `initNativeLibraries` should:
1. For `gdx.wasm.js`: Call `require()` via `@JSBody`, then poll `globalThis.Gdx` until non-null
2. For `freetype.js`: Call `require()` — this sets `globalThis.Module` synchronously

### 3.2 `MiniGameApplicationConfiguration.java` (replaces `WebApplicationConfiguration`)

**Source to fork:** `backends/backend-web/.../WebApplicationConfiguration.java` (117 lines)

Remove DOM-specific fields, keep GL config:
```java
public class MiniGameApplicationConfiguration {
    // GL config (keep)
    public boolean useGL30 = false;
    public boolean stencil = false;
    public boolean antialiasing = false;
    public boolean alpha = false;
    public boolean premultipliedAlpha = false;
    public boolean preserveDrawingBuffer = false;
    public boolean useDebugGL = false;
    public boolean usePhysicalPixels = false;
    public String powerPreference = "high-performance";

    // Canvas (new — replaces getElementById)
    public Object canvas;  // Set by game.js before Java code runs

    // Storage (keep)
    public String storagePrefix = "app";
    public boolean shouldEncodePreference = false;

    // Remove: canvasID, windowListener, baseUrlProvider, padHorizontal, padVertical, width, height, showDownloadLogs
    // These are browser-specific
}
```

### 3.3 `MiniGameGraphics.java` (replaces `WebGraphics.java` + `WebGLGraphics.java`)

**Sources to fork:**
- `WebGraphics.java` (482 lines) — base graphics class
- `WebGLGraphics.java` (129 lines) — WebGL context creation

**DOM dependencies in WebGLGraphics (complete inventory):**

| Line | DOM Call | Replacement |
|------|---------|-------------|
| 29 | `new WebWindow()` → `Window.current()` | Remove — canvas from config |
| 30 | `window.getDocument()` | Remove — no document |
| 31 | `document.getElementById(config.canvasID)` | Use canvas from config (created by `wx.createCanvas()`) |

**DOM dependencies in WebGraphics (complete inventory):**

| Line | DOM Call | Replacement |
|------|---------|-------------|
| 298 | `Window.current().getDocument().setTitle(title)` | No-op (WeChat has no title bar) |
| 383–478 | `document.addEventListener("fullscreenchange", ...)`, `document.exitFullscreen()`, `document.fullscreenElement` | Remove all fullscreen code — always fullscreen on WeChat |
| Auto-resize | `currentWindow.getClientWidth/Height()` | `WX.getScreenWidth/Height()` |
| Density | `@JSBody(script = "return devicePixelRatio \|\| 1;")` | `@JSBody(script = "return wx.getSystemInfoSync().pixelRatio;")` |
| Screen size | `@JSBody(script = "return screen.width;")` | `@JSBody(script = "return wx.getSystemInfoSync().screenWidth;")` |
| Screen size | `@JSBody(script = "return screen.height;")` | `@JSBody(script = "return wx.getSystemInfoSync().screenHeight;")` |
| Canvas sizing | `canvas.getStyle().setProperty("width", width + "px")` in `setCanvasSize()` | Remove CSS style lines — just set `canvas.setWidth()` / `canvas.setHeight()` directly |
| Cursor | `new WebCursor(...)`, `canvas.getStyle().setProperty("cursor", ...)` | Stub: throw `GdxRuntimeException("Not supported on mobile")` |

**Constructor pattern:**
```java
public MiniGameGraphics(MiniGameApplicationConfiguration config) {
    this.config = config;
    this.canvas = (HTMLCanvasElement)config.canvas; // Set by game.js adapter

    WebGLContextAttributesExt attr = ...;
    // Same WebGL context creation — canvas.getContext("webgl") works on WeChat canvas
    context = (WebGLRenderingContext)canvas.getContext("webgl", attr);
    this.gl20 = new WebGL20((WebGLRenderingContextExt)context);
    // ... rest same as WebGLGraphics but without DOM
}
```

**Important:** `HTMLCanvasElement` is from TeaVM's `org.teavm.jso.dom.html` — this works because WeChat's canvas object has the same interface (width, height, getContext). TeaVM's overlay types are duck-typed.

### 3.4 `MiniGameInput.java` (replaces `WebInput.java`)

**Source to fork:** `backends/backend-web/.../WebInput.java` (1057 lines)

**DOM dependencies (complete inventory):**

| Pattern | DOM Call | Replacement |
|---------|---------|-------------|
| Mouse | `document.addEventListener("mousedown/mousemove/mouseup", ...)` | Synthesize from `WX.onTouchStart/Move/End` |
| Mouse | `canvas.addEventListener("mousedown/mousemove/mouseup", ...)` | Same — use touch events |
| Touch | `canvas.addEventListener("touchstart/touchmove/touchend", ...)` | `WX.onTouchStart/Move/End/Cancel` |
| Keyboard | `document.addEventListener("keydown/keyup", ...)` | `WX.onKeyboardInput` + `WX.showKeyboard` |
| Wheel | `document.addEventListener("wheel", ...)` | Not supported — no-op or skip |
| Cursor | `canvas.style.cursor` | Not applicable on mobile — skip |
| Pointer lock | `canvas.requestPointerLock()` | Not supported — skip |

**Touch event structure** (from WeChat):
```javascript
{ touches: [{identifier, clientX, clientY, pageX, pageY}], changedTouches: [...] }
```

Map `changedTouches[0]` to mouse events for scene2d compatibility.

### 3.5 `MiniGameAudio.java` (replaces `HowlTeaAudio.java`)

**No source to fork** — WebAudio uses Howler.js. Build from scratch.

Implements `com.badlogic.gdx.Audio`:
```java
public class MiniGameAudio implements Audio {
    @Override
    public Sound newSound(FileHandle fileHandle) {
        // Create WXAudioContext via WX.createInnerAudioContext()
        // Set src to file path
        return new MiniGameSound(fileHandle);
    }

    @Override
    public Music newMusic(FileHandle fileHandle) {
        return new MiniGameMusic(fileHandle);
    }

    @Override
    public AudioDevice newAudioDevice(int samplingRate, boolean isMono) {
        throw new GdxRuntimeException("AudioDevice not supported by WeChat backend");
    }

    @Override
    public AudioRecorder newAudioRecorder(int samplingRate, boolean isMono) {
        throw new GdxRuntimeException("AudioRecorder not supported by WeChat backend");
    }
}
```

**Inner classes:**
- `MiniGameSound implements Sound` — wraps `WXAudioContext`, supports `play()`, `stop()`, `setVolume()`, etc.
- `MiniGameMusic implements Music` — wraps `WXAudioContext` with loop/position control

**Audio format:** All Solitaire assets are MP3 (~114KB, 20 files). WeChat supports MP3 natively.

### 3.6 `MiniGameAssetLoader.java` (replaces `AssetLoadImpl.java` + `AssetDownloadImpl.java`)

**This was missing from the original plan.** `AssetLoadImpl` (370 lines) + `AssetDownloadImpl` (191 lines) handle ALL asset downloading and script loading.

**DOM dependencies in AssetDownloadImpl:**

| Line | DOM Call | Replacement |
|------|---------|-------------|
| `loadScript` | `Window.current()`, `document.createElement("script")`, `document.getBody().appendChild(scriptElement)` | `require()` — synchronous on WeChat |
| `loadBinary` | `XMLHttpRequest` with `open("GET", url, async)` | `wx.getFileSystemManager().readFile()` for local files |
| `setOnProgress` | `req.onProgress(...)` | `wx.downloadFile({url, success, fail})` for remote files |

**DOM dependencies in AssetLoadImpl:**

| Line | DOM Call | Replacement |
|------|---------|-------------|
| `setupFileDrop` | `HTMLDocument`, `canvas.getOwnerDocument()`, `document.addEventListener("dragenter/dragover/drop")` | Remove entirely — no drag-and-drop on mobile |
| File reading | `FileReaderWrapper`, `DataTransferWrapper` | Remove — no file drops |

**Implementation strategy:**
- Implement `AssetLoader` interface (from `extensions/asset-loader`)
- `loadScript(path)` → `@JSBody` call to `require(path)`
- `loadAsset(path, Binary)` → `@JSBody` call to `wx.getFileSystemManager().readFile()`
- Skip all drag-and-drop, XHR, and DOM manipulation code

### 3.7 `MiniGameFiles.java` + `MiniGameFileHandle.java` (replaces `WebFiles.java` + `WebFileHandle.java`)

**Sources to fork:**
- `WebFiles.java` (101 lines)
- `WebFileHandle.java` (633 lines)

**DOM dependencies in WebFileHandle:**
- Uses `AssetLoadImpl` for downloading assets → replaced by `MiniGameAssetLoader`
- Uses `IndexedDB` for local storage → replace with `wx.env.USER_DATA_PATH` + `wx.getFileSystemManager()`
- Base URL from `window.location.href` → hardcode `"./"`

**DOM dependencies in WebFiles:**
- `Storage.getLocalStorage()` for some file types → not needed

**File system strategy:**
| FileType | Storage |
|----------|---------|
| `Internal` / `Classpath` | Bundled in package, read via `wx.getFileSystemManager().readFile()` |
| `Local` / `External` | `wx.env.USER_DATA_PATH` |
| `Absolute` | Direct file path |

### 3.8 `MiniGamePreferences.java` (replaces `WebPreferences.java`)

**Source to fork:** `WebPreferences.java` (230 lines)

Replace `Storage.getLocalStorage()` operations with `WX.setStorageSync/getStorageSync`.

**Key difference:** WeChat storage is synchronous, so all operations are direct. No need for async callback patterns.

### 3.9 `MiniGameNet.java` (replaces `WebNet.java`)

**Source to fork:** `WebNet.java` (213 lines)

Replace `XMLHttpRequest` with `WX.request()` via `@JSBody`.

### 3.10 `MiniGameClipboard.java` (replaces `WebClipboard.java`)

**Source to fork:** `WebClipboard.java` (99 lines)

Replace `navigator.clipboard` with `WX.setClipboardData/getClipboardData`.

**IMPORTANT:** `WebClipboard.java` calls `WebPermissions.queryPermission("clipboard-write")` at line 70. WeChat's clipboard API doesn't need permission checks. Remove the entire permission check block and call `WX.setClipboardData()` directly.

### 3.11 `MiniGameWindow.java` (replaces `WebWindow.java`)

**Source to fork:** `backends/backend-web/.../dom/impl/WebWindow.java` (55 lines)

This is a thin wrapper that currently delegates to `org.teavm.jso.browser.Window`. Replace:

| Method | Current | Replacement |
|--------|---------|-------------|
| Constructor | `Window.current()` | Remove — canvas from config |
| `requestAnimationFrame(Runnable)` | `Window.requestAnimationFrame(this)` | `WX.requestAnimationFrame(callback)` |
| `getDocument()` | `window.getDocument()` | Remove — no document |
| `getLocation()` | `window.getLocation()` | Remove — no location |
| `getClientWidth/Height()` | `window.getInnerWidth/Height()` | `WX.getScreenWidth/Height()` |
| `addEventListener(String, EventListener)` | `window.addEventListener(...)` | `WX.onShow/onHide` for lifecycle |

**Verification:** All runtime classes compile: `./gradlew :backends:backend-minigame:compileJava`

---

## Phase 4: Build Backend (`config/`)

### 4.1 `MiniGameBackend.java` (extends `TeaBackend`)

**Source to fork:** `backends/backend-web/.../config/backend/WebBackend.java` (200 lines)

Key differences from `WebBackend`:

| WebBackend | MiniGameBackend |
|-----------|-----------------|
| `targetType = JAVASCRIPT` or `WEBASSEMBLY_GC` | `targetType = JAVASCRIPT` only (WeChat doesn't support WASM GC) |
| Generates `index.html` + `web.xml` via `setupWebapp()` | Generates `game.js` + `game.json` + `project.config.json` via `setupMinigame()` |
| Jetty server for dev | No dev server (use WeChat DevTools) |
| `<script>` tag loading | `require()` in game.js |
| WASM embedded in gdx.wasm.js | Extract .wasm to separate file |
| Copies scripts to `scripts/` dir | Same, plus generates subpackage-aware loading |

**Fields:**
```java
public class MiniGameBackend extends TeaBackend {
    public String appId = "";
    public String orientation = "portrait";
    public String targetFileName = "app";

    @Override
    protected void setup(TeaCompilerData data) {
        targetType = TeaVMTargetType.JAVASCRIPT;
        tool.setTargetFileName(data.outputName + ".js");
        releasePath = new FileHandle(data.output.getAbsolutePath());
        tool.setTargetDirectory(releasePath.file());
        setupMinigame(data);
    }

    @Override
    protected void build(TeaCompilerData data) {
        super.build(data);
        extractWasm(data);  // Extract .wasm from gdx.wasm.js
    }

    private void setupMinigame(TeaCompilerData data) {
        generateGameJson();
        generateProjectConfig();
        generateGameJs(data);
    }
}
```

### 4.2 `MiniGameBuildConfiguration.java`

**NOTE:** `TeaBuildConfiguration` is in `backend-web`, NOT in `backend-shared`. Don't extend it — create a standalone config class:

```java
package com.github.xpenatan.gdx.teavm.backends.minigame.config;

public class MiniGameBuildConfiguration {
    public String appId;
    public String orientation = "portrait";
    public String targetFileName = "app";
    public String mainClassArgs = "";
    // Add fields as needed from TeaBuildConfiguration that are NOT DOM-specific
}
```

### 4.3 `game.js` template (main package entry point)

**CRITICAL: async gdx.wasm.js loading**

`gdx.wasm.js` self-executes with `window.Gdx = await Gdx()`. The `require()` call returns immediately but `Gdx` is set asynchronously. The loading flow MUST await this.

```javascript
// game.js — Main package entry point
const { setup } = require('./adapter/index.js');

// Phase 1: Setup shims and canvas (synchronous)
const canvas = wx.createCanvas();
setup(canvas);

// Phase 2: Draw loading screen
const ctx = canvas.getContext('2d');
ctx.fillStyle = '#1a472a';
ctx.fillRect(0, 0, canvas.width, canvas.height);
ctx.fillStyle = 'white';
ctx.font = '20px sans-serif';
ctx.fillText('Loading...', canvas.width / 2 - 40, canvas.height / 2);

// Phase 3: Load subpackages sequentially
async function startGame() {
    try {
        // Load engine subpackage (freetype.js, gdx.wasm.js)
        await wx.loadSubpackage({ name: 'engine' }).promise;

        // Load freetype.js (sets globalThis.Module synchronously)
        require('./scripts/freetype.js');

        // Load gdx.wasm.js (sets window.Gdx ASYNC via self-execution)
        require('./scripts/gdx.wasm.js');

        // Wait for Gdx to be available (async initialization)
        while (typeof window.Gdx === 'undefined') {
            await new Promise(r => setTimeout(r, 50));
        }

        // Load game code subpackage
        await wx.loadSubpackage({ name: 'game' }).promise;

        // Start the game
        const app = require('./game-code/app.js');
        app.main([]);
    } catch (err) {
        console.error('Failed to load:', err);
    }
}

startGame();
```

### 4.4 `game.json` template
```json
{
    "deviceOrientation": "${orientation}",
    "networkTimeout": {
        "request": 5000,
        "connectSocket": 5000,
        "uploadFile": 5000,
        "downloadFile": 500000
    },
    "subpackages": [
        { "name": "engine", "root": "scripts/" },
        { "name": "game", "root": "game-code/" },
        { "name": "assets", "root": "assets/" }
    ]
}
```

### 4.5 `project.config.json` template
```json
{
    "description": "${description}",
    "setting": {
        "urlCheck": false,
        "es6": true
    },
    "compileType": "game",
    "appid": "${appId}",
    "libVersion": "3.12.1"
}
```

### 4.6 WASM extraction utility
Build step that:
1. Reads `gdx.wasm.js` output
2. Finds the base64-encoded WASM data (variable `O`)
3. Decodes and writes to `scripts/gdx.wasm`
4. Optionally strips the embedded data from `gdx.wasm.js` to save size

**Verification:** `MiniGameBackend` compiles and generates all template files.

---

## Phase 5: Adapter Layer

### 5.1 `adapter/index.js` — Minimal browser polyfills

This provides shims needed by compiled code and third-party scripts:

```javascript
// === window polyfill ===
// Needed by: gdx.wasm.js (window.Gdx = ...), WebWindow equivalent, various compiled code
if (typeof window === 'undefined') {
    var window = {
        requestAnimationFrame: typeof requestAnimationFrame !== 'undefined' ? requestAnimationFrame : undefined,
        cancelAnimationFrame: typeof cancelAnimationFrame !== 'undefined' ? cancelAnimationFrame : undefined,
        devicePixelRatio: wx.getSystemInfoSync().pixelRatio,
        innerWidth: wx.getSystemInfoSync().screenWidth,
        innerHeight: wx.getSystemInfoSync().screenHeight,
        addEventListener: function() {},
        removeEventListener: function() {},
        location: { href: './' }
    };
}

// === WebAssembly patch ===
// Needed by: gdx.wasm.js fallback WASM loading
// WeChat's WXWebAssembly only accepts file paths, not ArrayBuffer
if (typeof WXWebAssembly !== 'undefined') {
    var originalInstantiate = WebAssembly.instantiate;
    WebAssembly.instantiate = function(source, imports) {
        if (source instanceof ArrayBuffer || source instanceof Uint8Array) {
            // Redirect buffer-based instantiation to file-based
            return WXWebAssembly.instantiate('scripts/gdx.wasm', imports);
        }
        return originalInstantiate(source, imports);
    };
}

// === atob polyfill (if not available) ===
// Needed by: freetype.js for decoding inline memory initializer
if (typeof atob === 'undefined') {
    var atob = function(base64) {
        return Buffer.from(base64, 'base64').toString('binary');
    };
}

// === console polyfill (should exist, but defensive) ===
if (typeof console === 'undefined') {
    var console = { log: function(){}, error: function(){}, warn: function(){} };
}

exports.setup = function(canvas) {
    globalThis.canvas = canvas;
};
```

### 5.2 Fix freetype.js Module scoping
Post-processing step in `MiniGameBackend.build()`:

After copying `freetype.js` to output, inject `globalThis.Module = Module;` before the `run()` call:

```java
// In MiniGameBackend
private void fixFreetypeModuleScoping(File freetypeJs) {
    String content = new String(Files.readAllBytes(freetypeJs.toPath()));
    content = content.replace(
        "Module[\"noExitRuntime\"]=true;",
        "Module[\"noExitRuntime\"]=true;if(typeof globalThis!==\"undefined\"){globalThis.Module=Module}"
    );
    Files.write(freetypeJs.toPath(), content.getBytes());
}
```

**Verification:** Adapter loads without errors in WeChat DevTools console.

---

## Phase 6: Build Plugin (`config/plugins/`)

### 6.1 `MiniGamePlugin.java` (adapts `WebPlugin.java`)
```java
@Before(JSOPlugin.class)
public class MiniGamePlugin implements TeaVMPlugin {
    @Override
    public void install(TeaVMHost host) {
        host.add(new MiniGameClassTransformer());
        host.add(new JavaObjectExporterDependency());
    }
}
```

### 6.2 `MiniGameClassTransformer.java` (adapts `WebClassTransformer.java`)
Copy `WebClassTransformer` — it uses reflection to scan `emu/` for `@Emulate` annotations. The emulation mechanism is platform-independent.

**IMPORTANT:** Update the Reflections scan package from `com.github.xpenatan.gdx.teavm.backends.web` to `com.github.xpenatan.gdx.teavm.backends.minigame` so it finds the copied emu/ files in the new location.

### 6.3 `MiniGameClassFilter.java` (adapts `WebClassFilter.java`)
Copy `WebClassFilter` — same filtering logic applies.

### 6.4 `JavaObjectExporterDependency.java`
Copy directly from backend-web — no DOM dependencies.

**Verification:** `./gradlew :backends:backend-minigame:compileJava` passes.

---

## Phase 7: Integration & Testing

### 7.1 Create example minigame build project

```
examples/basic/teavm-minigame/
├── build.gradle.kts
└── src/main/java/
    ├── BuildTeaVMMinigameDemo.java
    └── MiniGameLauncher.java
```

**`BuildTeaVMMinigameDemo.java`:**
```java
import com.github.xpenatan.gdx.teavm.backends.minigame.config.backend.MiniGameBackend;
import com.github.xpenatan.gdx.teavm.backends.shared.config.AssetFileHandle;
import com.github.xpenatan.gdx.teavm.backends.shared.config.compiler.TeaCompiler;
import org.teavm.vm.TeaVMOptimizationLevel;
import java.io.File;

public class BuildTeaVMMinigameDemo {
    public static void main(String[] args) {
        AssetFileHandle assetsPath = new AssetFileHandle("../assets");
        new TeaCompiler(new MiniGameBackend()
                .setAppId("test-appid")
                .setOrientation("portrait"))
                .addAssets(assetsPath)
                .setOptimizationLevel(TeaVMOptimizationLevel.ADVANCED)
                .setMainClass(MiniGameLauncher.class.getName())
                .setObfuscated(true)
                .build(new File("build/dist"));
    }
}
```

### 7.2 Build → test in WeChat DevTools
1. Run `BuildTeaVMMinigameDemo.main()` from IDE
2. Open output `dist/` folder in WeChat DevTools
3. Verify: canvas renders, game loop runs, touch input works

### 7.3 Test matrix

| Feature | Test | Status |
|---------|------|--------|
| Canvas creation | Screen shows colored rect | ☐ |
| Game loop | requestAnimationFrame runs at 60fps | ☐ |
| WebGL context | `getContext("webgl")` returns valid context | ☐ |
| Touch input | Tap/click events register | ☐ |
| Audio playback | Sound effects play (MP3) | ☐ |
| Asset loading | Textures load from filesystem | ☐ |
| Preferences | Save/load persists via wx.setStorageSync | ☐ |
| Lifecycle | wx.onShow/onHide pauses/resumes | ☐ |
| WASM loading | gdx.wasm loads via WXWebAssembly | ☐ |
| FreeType | Font rendering works (globalThis.Module) | ☐ |

---

## Phase 8: Size Optimization & Packaging

### 8.1 Measure actual release build size
Build with `ADVANCED` optimization + obfuscation. Get actual byte counts for:
- `app.js`
- `freetype.js`
- `gdx.wasm.js` + `gdx.wasm`

This determines subpackaging strategy.

### 8.2 If app.js ≤ 2MB → Single subpackage (easy)
```
dist/
├── game.js, game.json, project.config.json  (main ~50KB)
├── adapter/    (adapter polyfills ~5KB)
├── scripts/    (sub "engine" ~1.3MB: freetype.js, gdx.wasm.js, gdx.wasm)
├── game-code/  (sub "game" ~≤2MB: app.js)
└── assets/     (sub "assets" ~6.5MB+)
```

### 8.3 If app.js > 2MB → Size reduction strategy
Priority order (from research doc §10):

| # | Optimization | Est. Savings | Effort |
|---|-------------|-------------|--------|
| 1 | Uncomment `reflectionListener` in TeaBuilder | ~1.1MB | 1 line |
| 2 | Strip unused game variants (keep Klondike only) | ~600KB | Gradle excludes |
| 3 | Remove FreeType.js (use bitmap fonts) | ~1.0MB | Replace font system |
| 4 | Remove unused deps (gdx.ai, ashley, ktx.ashley) | ~150KB | build.gradle |
| 5 | Strip platform services (Billing, Leaderboard) | ~200-400KB | Source excludes |

---

## Dependency Graph

```
P0, P1 (Groundwork)
  │
  ▼
Phase 1 (Scaffolding: dirs, build.gradle, settings, emu/, gen/, teavm.properties, SPI)
  │
  ├── Phase 2 (WX bindings) ── depends on: Phase 1 compiles
  │       │
  │       ├── Phase 3 (Runtime replacements) ── depends on: Phase 2
  │       │
  │       └── Phase 4 (Build backend) ── depends on: Phase 1
  │               │
  │               └── Phase 5 (Adapter) ── depends on: Phase 4
  │
  ├── Phase 6 (Build plugin) ── depends on: Phase 1 (copies from backend-web)
  │
  └── Phase 7 (Integration) ── depends on: Phases 3 + 4 + 5 + 6
          │
          └── Phase 8 (Optimization) ── depends on: Phase 7
```

**Critical path:** P0 → P1 → **Phase 1** → **Phase 2** → **Phase 3** (3.1 Application + 3.3 Graphics + 3.6 AssetLoader) → **Phase 4** → **Phase 5** → **Phase 7**

---

## File Inventory: What to Create, Copy, or Skip

### Create from scratch (new code):
| File | Purpose |
|------|---------|
| `bindings/WX.java` | wx.* @JSBody wrappers |
| `bindings/WXCanvas.java` | Canvas overlay type |
| `bindings/TouchCallback.java` | @JSFunctor |
| `bindings/LifecycleCallback.java` | @JSFunctor |
| `bindings/KeyboardCallback.java` | @JSFunctor |
| `bindings/WXAudioContext.java` | Audio overlay type |
| `MiniGameApplication.java` | Main application class |
| `MiniGameApplicationConfiguration.java` | Config (no DOM fields) |
| `MiniGameAudio.java` | Audio implementation |
| `MiniGameAssetLoader.java` | Asset loading via wx API |
| `MiniGamePreferences.java` | Storage via wx API |
| `config/backend/MiniGameBackend.java` | Build backend |
| `config/MiniGameBuildConfiguration.java` | Build config |
| `adapter/index.js` | Browser polyfills |

### Fork and modify from backend-web:
| Source | Destination | Key changes |
|--------|-------------|-------------|
| `WebGraphics.java` | `MiniGameGraphics.java` | Remove fullscreen, setTitle |
| `WebGLGraphics.java` | (merged into MiniGameGraphics) | Remove getElementById, use config.canvas |
| `WebInput.java` | `MiniGameInput.java` | Replace all event listeners with wx.onTouch*/onKeyboard |
| `WebFiles.java` | `MiniGameFiles.java` | Remove localStorage ref |
| `WebFileHandle.java` | `MiniGameFileHandle.java` | Replace XHR with wx FS |
| `WebNet.java` | `MiniGameNet.java` | Replace XMLHttpRequest with wx.request |
| `WebClipboard.java` | `MiniGameClipboard.java` | Replace navigator.clipboard with wx clipboard |
| `WebWindow.java` | `MiniGameWindow.java` | Replace Window.current with WX bindings |
| `WebPlugin.java` | `MiniGamePlugin.java` | Same logic, different package |
| `WebClassTransformer.java` | `MiniGameClassTransformer.java` | Same logic |
| `WebClassFilter.java` | `MiniGameClassFilter.java` | Same logic |
| `WebPreferences.java` | `MiniGamePreferences.java` | Replace `Storage.getLocalStorage()` with `WX.setStorageSync/getStorageSync` |

### Copy as-is from backend-web (no changes except package rename):
| File | Why |
|------|-----|
| `WebGL20.java` | Pure WebGL 1, no DOM |
| `WebGL30.java` | Pure WebGL 2, no DOM (harmless) |
| `WebGL20Debug.java` | Debug wrapper, no DOM |
| `WebGL30Debug.java` | Debug wrapper, no DOM |
| `gl/*.java` | WebGL context interfaces |
| `gen/Emulate.java` | Annotation for emulation system |
| `emu/*` (all files) | Class emulations (patch 2 DOM refs) |
| `utils/KeyCodes.java` | Key code constants |
| `utils/PNG.java` | PNG utility |
| `filesystem/FileData.java` | File data interface |
| `filesystem/HEXCoder.java` | Hex encoding |
| `filesystem/MemoryFileStorage.java` | In-memory storage |
| `filesystem/FileDB.java` | File database |
| `agent/WebAgentInfo.java` | Agent info data class |
| `JavaObjectExporterDependency.java` | TeaVM plugin dep |
| `dom/typedarray/TypedArrays.java` | Pure TeaVM typed array ↔ Java NIO bridge — NO DOM deps despite being in dom/ package. Required by WebGL20 (26+ refs) and WebGL30 (25+ refs) |
| `filesystem/types/ClasspathStorage.java` | Empty class extending MemoryFileStorage, used by WebFiles |
| `filesystem/types/InternalStorage.java` | Empty class extending MemoryFileStorage, used by WebFiles |
| `WebApplicationLogger.java` | Pure System.out/err logging, no DOM |

### Skip entirely (DOM-dependent, not needed):
| File | Why skip |
|------|---------|
| `dom/impl/WebWindow.java` | Replaced by MiniGameWindow |
| `dom/*.java` (all DOM wrappers except `typedarray/TypedArrays.java`) | Browser DOM overlays |
| `assetloader/AssetDownloadImpl.java` | Uses XHR + script injection |
| `assetloader/AssetLoadImpl.java` | Uses DOM for file drop + AssetDownloadImpl |
| `webaudio/howler/*` | Howler.js audio system |
| `utils/WebNavigator.java` | Uses `navigator.*` |
| `utils/WebBaseUrlProvider.java` | Uses `window.location.href` |
| `utils/WebDefaultBaseUrlProvider.java` | Uses `window.location` |
| `utils/Timer.java` | Uses `Window.setTimeout` |
| `config/TeaBuildConfiguration.java` | Web-specific build config |
| `config/DefaultWebApp.java` | Generates index.html |
| `config/BaseWebApp.java` | WebApp interface |
| `config/TeaBuilder.java` | Web build helper |
| `config/backend/JettyServer.java` | Dev server |
| `filesystem/indexeddb/*` | IndexedDB not available on WeChat |
| `filesystem/types/LocalDBStorage.java` | Uses IndexedDB |
| `WebPreloadApplicationListener.java` | Web-specific preload |
| `WebAssetPreloadListener.java` | Web-specific preload |
| `WebCursor.java` | CSS cursor manipulation |
| `WebAudio.java` | Empty interface, only used by skipped `HowlTeaAudio` |
| `WebPermissions.java` | Uses `navigator.permissions` API |
| `WebWindowListener.java` | File drop interface, not needed on mobile |
| `WebTool.java` | Unused utility (not imported by any file) |
| `agent/WebWebAgent.java` | Uses `navigator.userAgent` — replaced by `WX.getPlatform()` |
| `dom/audio/Audio.java` | Uses `HTMLAudioElement`, not imported by any other file |

---

## Estimated Effort

| Phase | Description | New Files | Forked Files | Copied Files | Est. Hours |
|-------|-------------|-----------|-------------|-------------|------------|
| Pre-req | Version fix, SCM update | 0 | 2 | 0 | 0.5h |
| Phase 1 | Module scaffolding | 4 | 0 | ~30 | 2h |
| Phase 2 | WX bindings | 6 | 0 | 0 | 2h |
| Phase 3 | Runtime replacements | 2 | 9 | 0 | 10h |
| Phase 4 | Build backend | 2 | 1 | 0 | 3h |
| Phase 5 | Adapter layer | 1 | 0 | 0 | 2h |
| Phase 6 | Build plugin | 0 | 3 | 1 | 1h |
| Phase 7 | Integration + testing | 2 | 0 | 0 | 4h |
| Phase 8 | Size optimization | 0 | 0 | 0 | 2–8h |
| **Total** | | **17** | **15** | **~31** | **26–35h** |

---

## Open Questions

| # | Question | Resolution needed by | Default if unresolved |
|---|----------|---------------------|----------------------|
| 1 | What is the new SCM/repo URL for POM metadata? | Before publishing | Use placeholder, update later |
| 2 | Actual release build size — does app.js fit in 2MB? | Phase 8 | Plan for >2MB, optimize down |
| 3 | Should the adapter be generated by build or shipped as static files? | Phase 5 | Static files (simpler) |
| 4 | Does the Solitaire game use `Music` (streaming) or only `Sound` (short)? | Phase 3.5 | Implement both — simple wrappers |
