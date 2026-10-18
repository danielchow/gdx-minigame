[-SNAPSHOT]
- Optional asset-only subpackages: `MiniGameBackend.setDeferredAssetPackages` keeps logical paths stable while relocating selected payloads under sibling `optional/<name>/` roots. `MiniGameAssetPackages` reports actual per-launch load readiness and deduplicates downloads with retryable failures/timeouts. Internal/classpath reads and the asset loader wait for package readiness; manifests still enumerate every shipped file. Applications own required-startup and optional-download UI. No web/shared backend changes.
- `MiniGameBackend.setDisplayMode()` writes `game.json` `displayMode`. With `"desktop"`, Mac WeChat lets the player stretch the window at a fixed aspect ratio and fires `wx.onWindowResize`; without it, WeChat scales the launch-size canvas and reports no new size. A resize takes the size from the event, since `wx.getWindowInfo()` can lag behind it
- The canvas is sized from `wx.getWindowInfo()` window size when the app starts and follows `wx.onWindowResize`, instead of keeping the size `wx.createCanvas()` caught in `game.js`: desktop WeChat could still be opening its window then and stretched the squashed canvas over the final window. Touch mapping follows the same window size
- `MiniGameShare.setTimelineShare()`: 分享到朋友圈 (Moments) with a synchronously rendered card that is also the post's preview (never WeChat's game screenshot); `wx.onShareTimeline` ignores `{promise}` replies. On iOS the preview is the placeholder's package image, because iOS WeChat ignores runtime files there and takes a 4-7 s screenshot. 转发 and Moments share one `showShareMenu` list in either order
- PC WeChat: `MiniGameBackend.setDesktopOrientation()` writes `game.json` `desktopDeviceOrientation` (defaults to the mobile orientation); minimizing the window pauses the game and its audio like `wx.onHide`, and restoring resumes them, once per hidden period even when WeChat also fires `onHide`/`onShow`
- Extract embedded JS/JSON string templates from MiniGameBackend.java into 8 external template files with `TemplateUtil` for `${var}` placeholder substitution
- Add `.gitattributes` to enforce LF line endings for template resource files
- Filter leaked template resource files from classpath scanning in `copyAssets()`
- Fix WeChat iOS freezing or crashing while loading large textures: the void `memcpy` WASM import returned the whole heap, which WeChat's iOS bridge converts to a number on every call
- Fix asset tasks running twice when `finishLoading()` polling and the queued timer both ran them; task failures (including `Error`s) now reach `AssetManager` through `get()`
- Copy native pixmap pixels into the Java buffer only once it is requested, instead of the whole pixmap after every draw (e.g. the whole font atlas per glyph); fix buffer and native ownership after format conversion and on repeated `dispose()`
- Create the splash WebGL context with the `MiniGameApplicationConfiguration` default attributes, since the game reuses it
- Add `testRuntime` (part of `check`): Node regression tests against the real engine WASM
- Upload only the elements a GL call is given: every SpriteBatch flush uploaded the whole vertex buffer (80 KB for 1000 sprites), and uniform/attribute calls ignored offset, position and count
- Copy pixmap pixels to Java only when requested, not the whole pixmap after each draw of an uploaded pixmap (a 4 MiB font page per new glyph); `Pixmap.setPixels()` now reaches the native pixmap; undecodable images and failed allocations throw `GdxRuntimeException`
- FreeType: the bridged heap views follow heap growth; a failed load rejects `__freetypeReady`; the gamma-1 fast path no longer produces blank glyphs
- Startup failures end in a dialog: failed subpackage downloads retry, and engine/WASM/main failures or a 30 s stall offer a restart, instead of hanging on the splash
- A frame exception shows a restart dialog instead of a silent frozen screen; override `MiniGameApplication.onFatalError(Throwable)` to customize it
- Audio: pooled contexts no longer gain an `onEnded` listener per play; sounds and music pause while hidden or interrupted (calls, alarms) and resume only what was playing
- `wx.onTouchCancel` is delivered as `touchCancelled`, so a system-cancelled touch no longer presses buttons
- Each build empties the release directory (keeping `project.private.config.json`), so stale debug output (`src/`, `.js.map`, `.teavmdbg`), removed assets and old rename-map entries no longer ship; `packOptions.ignore` excludes debug output; a missing `gdx.wasm.js` or WASM data URI fails the build; `rename-map.json` is escaped and always written
- Preferences: `flush()` writes only changed keys; a key's type can change without leaving the old typed key; mismatched getters parse or return the default instead of throwing `ClassCastException`
- Asset loader/downloader listeners are called; `AssetManager` throws "Couldn't load file" for a missing file instead of loading forever
- `MiniGameFileHandle` no longer shadows `FileHandle.type`, which broke `hashCode()`/`equals()`
- `setWindowedMode()` no longer resizes the full-screen canvas (it halved the resolution and drew the game 2x larger when a desktop-style display locked orientation); `supportsDisplayModeChange()` is false
- Density and safe insets come from one cached `wx.getWindowInfo()` snapshot, refreshed on `wx.onWindowResize`, instead of 2-3 synchronous native calls (~112 us each on Android) per read
- Platform detection uses WeChat's lowercase names (`UIUtils.isAndroid`/`isIos` were always false) and reads `wx.getDeviceInfo()` once instead of the deprecated `wx.getSystemInfoSync()`
- Release runtime no longer logs progress and timing (`[PERF]` lines on every texture upload and image decode, startup progress in the JS templates); errors and warnings still report
- Removed dead code: `WebApplication`, `WebApplicationConfiguration`, `MemoryFileStorage`, `ClasspathStorage`, `WebAgentInfo`, and no-op build steps
- Pooled sound contexts use WeChat's WebAudio implementation and keep their loaded source between plays
- `vibrate(ms)` and `vibrate(VibrationType)` map to WeChat's short/long and light/medium/heavy vibrations; `getTextInput` reports `canceled()`; `exit()` calls `wx.exitMiniProgram`; the asset loader's `preload`/`loadScript` listeners are called
- `getDensity()` reports the device pixel ratio, as Android does (was 0.6x), and `getPpiX/Y()` 160x it
- FreeType: the extension no longer puts `backend-web` (and its same-named emu classes) on mini game classpaths; `testRuntime` renders a real glyph through the bundled FreeType WASM
- Add `MiniGameShare`: share cards (`wx.shareAppMessage`), the friends-only 转发 menu with a placeholder card and late replies (`{promise}`), launch/`onShow` query delivery, and native PNG encoding of RGBA pixels through an offscreen canvas

[1.2.1]
- Updated to libgdx 1.13.5
- Fix compile error
- Remove some internal javascript wrappers

[1.2.0]
- Updated to TeaVM 0.12.0
- Add WebAssembly support
- Enhanced compile error logging
- Switched to TeaVM's native array/buffer implementation, eliminating wrappers and custom solutions
- Upgraded to Gradle 8.14
- Skip annotation deprecation

[1.1.0]
- Fix music not looping correctly
- Add disposeUnsafeByteBuffer, newUnsafeByteBuffer and getAllocatedBytesUnsafe
- Fix play music creating new music instance
- Clear mouse delta every frame
- Improve PixmapEmu and Gdx2DPixmapEmu
- Add PixmapEmu(Gdx2DPixmapEmu) constructor
- Add customizable ByteBuffer
- Update to libgdx 1.13.1

[1.0.5]
- Add Config Asset preloadListener
- Update AssetLoader and AssetDownloader
- Add AssetInstance to obtain AssetLoader or AssetDownloader
- Fix drawing to Gdx2DPixmapEmu
- Update Freetype emulation to fix build errors and script loading solution
- Update TeaVM to 0.11.0

[1.0.4]
- Fix music id
- add custom gltf resource path
- Code improvement when using physical pixels
- Fix file handle list returning wrong path
- UsePhysicalPixels is set to false by default
- Fix webgl buffers

[1.0.3]
- Don't download assets if preload assets is true
- add PowerPreference option
- update teavm to 0.10.1
- update upload attempt
- Small file handle fix
- Add option to overwrite saved file
- Improve download logs and progress method

[1.0.2]
- Small file storage improvement
- Use file type for checking if asset is loaded
- Improve arraybuffer performance
- Improve texture opengl method
- Fix sync loading (FreeType)

[1.0.1]
- Fix an issue when creating database and trying to load assets.
- Quick fix to filter out assets.txt file and to delete it before adding new files

[1.0.0]
- Update teavm to 0.10.0
- Pixmap now use Gdx2DPixmap
- New Gdx.files.internal, classpath and local implementation. Local files uses IndexedDB.  
- Call dispose when browser closes
- Improve clipboard text copy/paste.
- Change default sound/music api to howler.js
- add shouldEncodePreference config
- add localStoragePrefix config
- AssetManager can now download assets

[1.0.0-b9]
- add TeaClassFilter printAllowedClasses() and printExcludedClasses()
- add TeaReflectionSupplier.printReflectionClasses()
- Change exclude class comparison to regex
- Downgrade teavm dev 0.10.X version to 0.9.2 release version
- Remove tool/generator from maven/snapshot.

[1.0.0-b8]
- Fix MMB code
- Fix catch key
- Support IntBuffer in GL20
- Update teavm to 0.10.0-dev-1
- Update libgdx to 1.12.1
- Remove obfuscate from build config. Use TeaVMTool.
- Add useDefaultHtmlIndex and logoPath config option.

[1.0.0-b7]
- Remove jParser
- Remove/Move Bullet module to a standalone repository
- Remove/Move Box2d module to a standalone repository
- Remove hardcoded box2d, bullet and imgui load method.
- Update teavm to version 0.9.0-dev-12
- Update GL20 from GWT gdx 1.12.0
- Update Sound from GWT gdx 1.12.0
- Add GL30 from GWT gdx 1.12.0
- Add resources automatically when module contains META-INF/gdx-teavm.properties
- Fix freetype shadow error
- Improve rendering performance
- Add mouse locking

[1.0.0-b6]
- Update libgdx to version 1.12.0
- Update jParser to version b3
- Update teavm to version 0.9.0-dev-7

[1.0.0-b5]
- Option in App config to show download logs. Default is set to false.
- Implement setApplicationLogger method.
- Rename bullet desktop lib from platform to desktop.
- Add file drag & drop using TeaWindowListener in App config.
- Add WIP Gdx2DPixmap. Can toggle in App config.
- Update teavm to 0.9.0-dev-4
- Add userAgent system property
- Add Storage prefix #94

[1.0.0-b4]
- Fix an issue with TeaClassloader that was giving annotation error when building artemis games
- Bugfix: Asset Loading should not re-Download 403 Errors
- Bugfix for density calculation.
- Bugfix: blank line on top of HTML body on mobile.
- Bugfix: Asset Loading & Error "ERR_HTTP2_SERVER_REFUSED_STREAM

[1.0.0-b3]
- Add gdx-bullet c++ module so desktop and web use the same c++ code/version
- Update teaVM to 0.8.0-dev-1 and project Java version to 11
- Add missing buffer in glBufferData
- Add web gl debug
- Bugfix for File Listing: file listing now also includes binary, image and audio files
- Build Configuration: Asset Filter & Title for HTML File can now be Configured
- Load imgui javascript file if exist (Temp)
- Add Bullet missing methods and improvements
- Remove backend-web
- Deprecate some settings from TeaBuildConfiguration (obfuscate flag)
- Add webapp loading
- Add TeaNet HTTP Requests & Opening Links
- Fixing the base URL for asset loading. Having a '?' with parameters won't work.
- Option to not show the libGDX logo during loading

[1.0.0-b2]
- Fix sound/music assets path
- Add TeaBuildConfiguration.classesToSkip option
- Remove MathUtilsEmu

[1.0.0-b1]
- First beta release