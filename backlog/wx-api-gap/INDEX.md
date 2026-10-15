# WeChat API-gap backlog

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716.

Source inspection confirms the implementation states below; no runtime reproduction or device validation was performed. Researched means the source and relevant checked-in API references were reviewed; every issue remains open. Severity reflects the stated contract gap or risk, not measured application impact. The move issue is first because its inferred failure path can lose the source.

| Issue | Severity | Status |
| --- | --- | --- |
| [moveTo — quiet close can hide a failed persistent write](moveTo-copy-delete-data-loss.md) | 🔴 High (inferred data-loss risk; fix first) | 📋 Researched; open |
| [Accelerometer — getters return zero](accelerometer-stub.md) | 🟡 Medium (missing contract behavior) | 📋 Researched; open |
| [Audio pan — only volume is applied](audio-setpan-volume-only.md) | 🟡 Medium (missing contract behavior) | 📋 Researched; open |
| [Sound pitch — initial and per-playback values are ignored](audio-setpitch-noop.md) | 🟡 Medium (missing contract behavior) | 📋 Researched; open |
| [Callbacks — audio errors discard their result](callback-interfaces-lack-typed-parameters.md) | 🟡 Medium (missing contract behavior) | 📋 Researched; open |
| [Clipboard reads — Java cache never refreshes](clipboard-getcontents-stub.md) | 🟡 Medium (missing contract behavior) | 📋 Researched; open |
| [Directory filters — FileFilter and FilenameFilter throw](filedb-list-filter-throws.md) | 🟡 Medium (missing contract behavior) | 📋 Researched; open |
| [Compressed textures — upload methods throw](gl-compressed-texture-disabled.md) | 🟡 Medium (missing contract behavior) | 📋 Researched; open |
| [Indexed drawing — Buffer contents are never uploaded](gl-drawelements-client-side-buffer-broken.md) | 🟡 Medium (missing contract behavior) | 📋 Researched; open |
| [GL queries — implementable state queries still throw](gl-query-methods-throw-exceptions.md) | 🟡 Medium (missing contract behavior) | 📋 Researched; open |
| [Readback — only RGBA/UNSIGNED_BYTE is implemented](gl-readpixels-format-restriction.md) | 🟡 Medium (missing contract behavior) | 📋 Researched; open |
| [GL30 request — silent WebGL1 fallback after splash context](gl-webgl2-silent-fallback.md) | 🟡 Medium (missing contract behavior) | 📋 Researched; open |
| [Gyroscope — getters return zero](gyroscope-stub.md) | 🟡 Medium (missing contract behavior) | 📋 Researched; open |
| [isButtonJustPressed — always false](input-buttonjustpressed-stub.md) | 🟡 Medium (missing contract behavior) | 📋 Researched; open |
| [Keyboard input — native field configuration and events are unwired](keyboard-textinput-stub.md) | 🟡 Medium (missing contract behavior) | 📋 Researched; open |
| [Desktop input — native mouse and wheel events are missing](missing-mouse-wheel-events.md) | 🟡 Medium (missing contract behavior) | 📋 Researched; open |
| [HTTP adapter — cancellation does not abort transport](net-xmlhttprequest-vs-wxrequest.md) | 🟡 Medium (missing contract behavior) | 📋 Researched; open |
| [Orientation — angles and rotation matrix are unimplemented](orientation-compass-stub.md) | 🟡 Medium (missing contract behavior) | 📋 Researched; open |
| [FileHandle.read — primary storage can return null](read-npe-on-directory.md) | 🟡 Medium (missing contract behavior) | 📋 Researched; open |
| [Onscreen keyboard — null options and missing events](showkeyboard-null-options.md) | 🟡 Medium (missing contract behavior) | 📋 Researched; open |
| [Application diagnostics — zero heap and version values](app-heap-version-stubs.md) | 🔵 Low (cleanup or optional capability) | 📋 Researched; open |
| [Class filter — registered with no configured exclusions](build-classfilter-unpopulated-dead-code.md) | 🔵 Low (cleanup or optional capability) | 📋 Researched; open |
| [Build configuration — standalone fields are not consumed](build-config-not-wired-to-backend.md) | 🔵 Low (cleanup or optional capability) | 📋 Researched; open |
| [Adapter metrics — deprecated system-info calls](build-deprecated-api-in-adapter-template.md) | 🔵 Low (cleanup or optional capability) | 📋 Researched; open |
| [Build output — incomplete final package validation](build-missing-output-validation.md) | 🔵 Low (cleanup or optional capability) | 📋 Researched; open |
| [Class transformer — broad Reflections roots need review](build-transformer-reflections-scope-too-broad.md) | 🔵 Low (cleanup or optional capability) | 📋 Researched; open |
| [WASM layout — duplicated extracted path](build-wasm-path-hardcoded.md) | 🔵 Low (cleanup or optional capability) | 📋 Researched; open |
| [copyTo — whole-file buffering and extra copies](copyTo-triple-memory-buffer.md) | 🔵 Low (cleanup or optional capability) | 📋 Researched; open |
| [Canvas overlay — browser type used for WX canvas](gl-htmlcanvaselement-cast-fragile.md) | 🔵 Low (cleanup or optional capability) | 📋 Researched; open |
| [Context powerPreference — undocumented behavior](gl-powerpreference-unsupported.md) | 🔵 Low (cleanup or optional capability) | 📋 Researched; open |
| [Integer texture setters — call texParameterf](gl-texparameteri-calls-wrong-method.md) | 🔵 Low (cleanup or optional capability) | 📋 Researched; open |
| [InnerAudioContext — incomplete property and event coverage](inner-audio-context-missing-properties.md) | 🔵 Low (cleanup or optional capability) | 📋 Researched; open |
| [Touch pressure — binary fallback only](input-pressure-hardcoded.md) | 🔵 Low (cleanup or optional capability) | 📋 Researched; open |
| [Local file timestamps — lastModified always zero](lastmodified-always-zero.md) | 🔵 Low (cleanup or optional capability) | 📋 Researched; open |
| [Device APIs — contract gaps and optional extensions](missing-device-sensor-utility-apis.md) | 🔵 Low (cleanup or optional capability) | 📋 Researched; open |
| [Network extensions — file transfers and event sockets are missing](missing-network-apis.md) | 🔵 Low (cleanup or optional capability) | 📋 Researched; open |
| [Writes — entire-file buffering and append copies](write-buffer-entire-file.md) | 🔵 Low (cleanup or optional capability) | 📋 Researched; open |

Use [TEMPLATE.md](TEMPLATE.md) for new issues and retain an evidence date, commit and reproduction status. Durable platform constraints and current fallbacks are in [docs/limitations.md](../../docs/limitations.md); they are not open issues in this directory.
