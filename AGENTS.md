# gdx-minigame — Agent Guide

## Project Overview

**gdx-minigame** is a fork of [gdx-teavm](https://github.com/xpenatan/gdx-teavm) that adds a WeChat Mini Game backend. It compiles LibGDX games to JavaScript/WASM via TeaVM and wraps WeChat Mini Game APIs (canvas, audio, storage, networking, touch, lifecycle) into LibGDX's standard interfaces.

- **Group ID:** `com.zourgames.gdx`
- **Status:** The `backend-minigame` module is mature and working, with ongoing improvements merged regularly.

## Architecture

```
LibGDX Game (Java/Kotlin)
       │
       ▼
  TeaVM Compiler ─── backend-shared/ (compiler infra, asset handling)
       │                        ▲
       │                   reference only
       ▼
  backend-minigame/ ──────────┘
       │
       ├── config/backend/MiniGameBackend.java  ──►  Generates WeChat output files
       │       (loads templates via TemplateUtil, writes to build output)
       │
       ├── config/backend/TemplateUtil.java     ──►  ${var} placeholder substitution
       │
       ├── resources/templates/minigame/*.js     ──►  External template files
       │
       ├── bindings/WX.java, WXCanvas.java, WXAudioContext.java  ──►  WeChat API wrappers
       │
       └── MiniGame*.java (Audio, Input, Graphics, Files, Net, ...)  ──►  LibGDX interfaces
       │
       ▼
  WeChat Mini Game Runtime (canvas, wx.* APIs)
```

**Key insight:** `backend-minigame` implements LibGDX backend interfaces (Audio, Input, Graphics, Files, Net, Preferences, etc.) by calling into `bindings/WX*.java` which wraps WeChat's `wx.*` JavaScript APIs via TeaVM's JS interop. The `config/backend/MiniGameBackend.java` handles build-time code generation, producing the WeChat-specific output structure instead of a standard HTML page.

**Template system:** Generated JS/JSON files (`adapter.js`, `game.js`, `game.json`, etc.) are **external template files** under `src/main/resources/templates/minigame/`, loaded at build time by `TemplateUtil` with `${var}` placeholder substitution. **Never embed JS/JSON as Java string concatenation** — always use templates. See [Template System](#template-system) below.

## Project Structure

### Top-Level Files

| File | Purpose |
|------|---------|
| `AGENTS.md` | This guide — what agents need to know to work on the project |
| `CHANGES.md` | Changelog for all releases |
| `README.md` | Project readme (from upstream gdx-teavm) |
| `.pi/prompts/fix-wechat.md` | Debug pipeline prompt for WeChat DevTools errors |

### Source Modules

| Path | Purpose |
|------|---------|
| `backends/backend-minigame/` | WeChat Mini Game backend module — **this is where all development happens** |
| `backends/backend-web/` | Original web backend — **read-only reference, do not modify** |
| `backends/backend-shared/` | Shared compiler infrastructure — **read-only reference, do not modify** |

> ⚠️ **Rule:** Never modify `backend-web/` or `backend-shared/`. They are upstream code used as reference implementations. When adding features to `backend-minigame/`, study the equivalent class in `backend-web/` first to understand the pattern, then adapt it for the WeChat API.

### backend-minigame Key Packages

| Package | Contents |
|---------|----------|
| `bindings/` | WX API wrappers (`WX.java`, `WXAudioContext.java`, `WXCanvas.java`), ad wrappers (`WXAdUtils.java`, `WXCustomAd.java`, `WXInterstitialAd.java`, `WXRewardedVideoAd.java`), callbacks (touch, audio, keyboard, lifecycle, onHide, ad events) |
| `config/backend/` | `MiniGameBackend.java` — generates all WeChat output files via `TemplateUtil`; `TemplateUtil.java` — `${var}` placeholder loader |
| `config/` | `MiniGameBuildConfiguration.java` — user-facing config class (appId, description, orientation, etc.). Add new config fields here; `MiniGameBackend` consumes them. |
| `config/plugins/` | TeaVM plugins (class filter, class transformer, JavaObject exporter) |
| `filesystem/` | File storage (`FileDB.java`, `MemoryFileStorage.java`, `PersistentFileStorage.java`, `FileData.java`, `HEXCoder.java`), storage types (classpath, internal) |
| `gl/` | WebGL extension interfaces |
| `dom/` | DOM emulation (TeaVM navigator) |
| `utils/` | `KeyCodes.java`, `Timer.java`, `PNG.java` |
| `agent/` | `WebAgentInfo.java` |

### Reference Documentation

| Path | Contents |
|------|---------|
| `references/api-index.md` | Index of all 904 WeChat API docs across 22 categories |
| `references/wechat-minigame/` | 19 WeChat technical guides (QuickStart, TechPrinciple, adapters, FAQ) |
| `references/wx-api-docs/` | Full WeChat minigame API docs organized by category |
| `references/miniprogram-api-docs/` | WeChat miniprogram API docs (overlaps with minigame; useful for APIs shared between miniprogram and minigame) |

**Most relevant WX API categories for this project:**
- `render/` — canvas, frame, image, font (used by `MiniGameGraphics`, `WXCanvas`)
- `media/audio/` — audio playback (used by `MiniGameAudio`, `WXAudioContext`)
- `storage/` — key-value storage (used by `MiniGamePreferences`, `FileDB`)
- `base/` — lifecycle, system info, touch events (used by `WX.java`, callbacks)
- `network/` — HTTP requests, WebSocket (used by `MiniGameNet`)
- `file/` — file system access (used by `MiniGameFiles`, `MiniGameFileHandle`)

> 💡 When implementing a new feature, always check **both** the web backend (`backend-web/`) for the LibGDX interface pattern **and** `references/wx-api-docs/` for the WeChat API that provides the equivalent capability.

### Other Modules

The repo also contains `extensions/` (asset-loader, freetype, controllers) and `examples/` (basic, freetype, gdx-tests) from upstream. These are registered in `settings.gradle.kts` but are **not part of the minigame backend** — do not modify them for minigame work.

### Top-Level Agent Artifacts

`context.md`, `plan.md`, and `progress.md` are transient agent planning files — **do not treat them as authoritative project documentation**.

### Build Config

| File | Purpose |
|------|---------|
| `buildSrc/src/main/kotlin/LibExt.kt` | Group ID, versions, build constants |
| `settings.gradle.kts` | Module registration |

---

## Common Agent Tasks

### Adding a new WeChat API binding

1. **Find the equivalent** in `backends/backend-web/` — study how the web backend implements the LibGDX interface
2. **Read the WeChat API doc** in `references/wx-api-docs/<category>/` to understand what `wx.*` calls are available
3. **Add the binding** in `bindings/` — create a Java wrapper using TeaVM's `@JSBody` / JS interop to call the `wx.*` API
4. **Wire it up** in the corresponding `MiniGame*.java` class (e.g., `MiniGameAudio.java` for audio APIs)
5. **Build and test** using the Build-Publish-Test cycle below

### Fixing a runtime error from WeChat DevTools

Use the `fix-wechat` prompt template (`.pi/prompts/fix-wechat.md`), or manually:

1. Copy the console error from WeChat DevTools
2. Identify whether it's a **compile-time** issue (TeaVM class/method problem) or **runtime** issue (WX API mismatch, missing adapter shim)
3. For runtime issues: check `bindings/WX*.java` (JS interop layer) and `config/backend/MiniGameBackend.java` (generated adapter code)
4. For compile-time issues: check `config/plugins/` (class filter, transformer)
5. Fix, rebuild, and re-test

### Updating generated output files (adapter, game.js, etc.)

All generated JS/JSON files are **external templates** loaded from `src/main/resources/templates/minigame/`.

**To change the content of a generated file** (adapter.js, game.js, game.json, etc.):
1. Edit the template file in `src/main/resources/templates/minigame/`
2. If the template needs a new `${var}` placeholder, add it to the template AND update the `TemplateUtil.resolve()` call in `MiniGameBackend.java`
3. Rebuild and test via the Build-Publish-Test cycle

**⚠️ NEVER embed JS/JSON as Java string concatenation in MiniGameBackend.java.** All generated text files must be external templates.

See [Template System](#template-system) for the full guide.

---

## Template System

`MiniGameBackend.java` generates 8 output files at build time. Instead of embedding JS/JSON as Java string concatenation, each output is a **template resource file** under `src/main/resources/templates/minigame/` with `${var}` placeholders substituted at build time by `TemplateUtil`.

### How it works

```
templates/minigame/game.json          Template with ${orientation}
         │
         ▼  TemplateUtil.resolve("game.json", Map.of("orientation", orientation))
MiniGameBackend.generateGameJson()    Reads template, substitutes, writes output
         │
         ▼
build/dist/minigame/game.json        Final output with placeholder replaced
```

### Template files and their placeholders

| Template file | Output path | Method | Placeholders |
|---|---|---|---|
| `adapter.js` | `adapter.js` | `generateAdapter()` | none |
| `first-screen.js` | `first-screen.js` | `generateFirstScreen()` | none |
| `game.js` | `game.js` | `generateGameJs()` | `${mainClassArgs}` |
| `game.json` | `game.json` | `generateGameJson()` | `${orientation}` |
| `project.config.json` | `project.config.json` | `generateProjectConfigJson()` | `${description}`, `${appId}` |
| `subpackage-engine.js` | `subpackages/engine/game.js` | `generateSubpackageEntryFiles()` | none |
| `subpackage-game.js` | `subpackages/game/game.js` | `generateSubpackageEntryFiles()` | `${targetFileName}` ×3 |
| `subpackage-assets.js` | `assets/game.js` | `generateSubpackageEntryFiles()` | none |

### Adding a new generated file

1. Create the template file in `src/main/resources/templates/minigame/`
2. Add `${var}` placeholders where dynamic values are needed
3. Add a `generateXxx()` method in `MiniGameBackend.java`:
   ```java
   private void generateXxx() {
       String content = TemplateUtil.resolve("my-template.js",
           Map.of("myVar", myField));
       releasePath.child("output.js").writeString(content, false);
   }
   ```
4. Call the method from `setupMinigame()` or `copyAssets()` as appropriate
5. Rebuild and test

### Template rules

1. **LF only** — no CRLF. Enforced via `.gitattributes` (`* text=auto eol=lf` — global LF policy)
2. **End with exactly one newline** — no trailing blank lines
3. **No `${` in JS content** — current JS uses string concatenation (`+`), not ES6 template literals. If `${` is needed in JS output, add escaping support to `TemplateUtil`
4. **Never modify inline** — always edit the template file, never put content back into Java string concatenation

### Why templates?

- **Readability**: 200-line JS files are readable as `.js` files, not as `"..." +` chains
- **Editability**: IDE syntax highlighting, linting, and formatting work on `.js`/`.json` files
- **Reviewability**: diffs show meaningful changes to the JS/JSON, not Java string escaping noise
- **Safety**: `TemplateUtil` throws `RuntimeException` with a clear message if a template is missing

### Non-template build steps

`MiniGameBackend.java` also performs post-processing that isn't template-based. Be aware of these when modifying build output:

| Method | Purpose |
|--------|---------|
| `fixFreetypeModuleScoping()` | Wraps freetype JS in an IIFE to avoid module scope conflicts |
| `extractWasmFromJs()` | Extracts embedded WASM binary from TeaVM's combined JS output into a separate `.wasm` file |
| `renameBlockedExtensions()` | Appends `.txt` to file extensions that WeChat blocks (e.g. `.wasm` → `.wasm.txt`) and generates `rename-map.json` for runtime lookup |

### Classpath leak prevention

Template resources live in `src/main/resources/templates/minigame/`. The shared `TeaVMResourceProperties` classpath scanner finds them and copies them to the output along with game assets. `MiniGameBackend.copyAssets()` prevents this:

1. `scripts.removeIf(path -> path.contains("templates/minigame/"))` — removes templates from the scripts list
2. Deletes any `assets/templates/` directory that was copied
3. Filters `preload.txt` entries that reference template paths

When adding new templates, verify they don't leak into the output by checking the build output:
```bash
find minigame/build/dist/minigame -path "*/templates/*" -type f
# Should return nothing
```

---

### Understanding how a LibGDX interface maps to WeChat

| LibGDX Interface | MiniGame Class | WX API Category |
|-----------------|---------------|-----------------|
| `Audio` / `Music` | `MiniGameAudio` | `media/audio/` |
| `Graphics` | `MiniGameGraphics` | `render/canvas/` |
| `Input` | `MiniGameInput` | `base/` (touch events) |
| `Files` / `FileHandle` | `MiniGameFiles` / `MiniGameFileHandle` | `file/` |
| `Net` | `MiniGameNet` | `network/` |
| `Preferences` | `MiniGamePreferences` | `storage/` |
| `Application` | `MiniGameApplication` | `base/` (lifecycle) |
| `Clipboard` | `MiniGameClipboard` | — |

---

## Testing

- **No automated test suite** for `backend-minigame` itself — the only test harness is the WeChat DevTools running the Solitaire test game
- `backend-web` has one unit test (`MemoryFileStorageTest.java`) that may serve as a pattern
- Always validate changes via the full Build-Publish-Test cycle below

---

## Test Game: Solitaire

The **Solitaire** card game (`~/Develop/Private/legacy/Solitaire`) is the primary integration test. Its `:minigame` Gradle subproject consumes `com.zourgames.gdx:backend-minigame` from Maven Local.

### Solitaire Key Files

| Path | Purpose |
|------|---------|
| `minigame/build.gradle.kts` | Dependencies, build config |
| `minigame/src/.../MiniGameBuilder.java` | TeaVM build entry point |
| `minigame/src/.../MiniGameLauncher.kt` | Kotlin launcher |
| `minigame/adapter/` | Empty — adapter is generated into `build/dist/` by the backend at build time |
| `minigame/build/dist/minigame/` | Generated WeChat output (import into WeChat DevTools for testing) |

---

## Build-Publish-Test Cycle

After making changes to the backend, test end-to-end:

```bash
# 1. Build and publish backend to Maven Local
cd /Users/daniel/workspace/gdx-minigame
./gradlew :backends:backend-minigame:publishToMavenLocal

# 2. Rebuild Solitaire minigame variant
cd /Users/daniel/Develop/Private/legacy/Solitaire
rm -rf minigame/build/dist
./gradlew :minigame:buildRelease

# 3. Test in WeChat DevTools
# Open WeChat DevTools, import the minigame project at:
#   ~/Develop/Private/legacy/Solitaire/minigame/build/dist/minigame
# Check the console for errors.
```

---

### Additional Resources

- `~/Develop/Private/legacy/Solitaire/docs/backlog/minigame/research.md` — Original design decision research (WeChat API details, TeaVM internals, risk eliminations)
