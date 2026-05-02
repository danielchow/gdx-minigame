# gdx-minigame — Agent Guide

## Project Overview

**gdx-minigame** is a fork of [gdx-teavm](https://github.com/xpenatan/gdx-teavm) that adds a WeChat Mini Game backend. It compiles LibGDX games to JavaScript/WASM via TeaVM and wraps WeChat Mini Game APIs (canvas, audio, storage, networking, touch, lifecycle) into LibGDX's standard interfaces.

- **Group ID:** `com.zourgames.gdx`
- **Status:** The `backend-minigame` module is mature and working, with ongoing improvements merged regularly.

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
| `backends/backend-minigame/` | WeChat Mini Game backend module |
| `backends/backend-web/` | Original web backend (fork source for minigame) |

### backend-minigame Key Packages

| Package | Contents |
|---------|----------|
| `bindings/` | WX API wrappers (`WX.java`, `WXAudioContext.java`, `WXCanvas.java`), callbacks (touch, audio, keyboard, lifecycle, onHide) |
| `config/backend/` | `MiniGameBackend.java` — generates all WeChat output files (`game.js`, `game.json`, `adapter/index.js`, `project.config.json`) |
| `config/plugins/` | TeaVM plugins (class filter, class transformer, JavaObject exporter) |
| `filesystem/` | File storage (`FileDB.java`, `MemoryFileStorage.java`, `HEXCoder.java`), storage types (classpath, internal) |
| `gl/` | WebGL extension interfaces |
| `dom/` | DOM emulation (TeaVM navigator) |
| `utils/` | `KeyCodes.java`, `Timer.java`, `PNG.java` |
| `agent/` | `WebAgentInfo.java` |

### Reference Documentation

| Path | Contents |
|------|----------|
| `references/api-index.md` | Index of all 904 WeChat API docs across 22 categories |
| `references/wechat-minigame/` | 19 WeChat technical guides (QuickStart, TechPrinciple, adapters, FAQ) |
| `references/wx-api-docs/` | Full WeChat API docs organized by category (ad, ai, base, device, file, media, network, render, storage, ui, etc.) |

### Build Config

| File | Purpose |
|------|---------|
| `buildSrc/src/main/kotlin/LibExt.kt` | Group ID, versions, build constants |
| `settings.gradle.kts` | Module registration |

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

## Debugging WeChat DevTools Errors

Use the `fix-wechat` prompt template (`.pi/prompts/fix-wechat.md`):

1. Copy console output from WeChat DevTools
2. Feed it into the fix-wechat prompt
3. It runs a **scout → plan → review → work** pipeline that:
   - Identifies each distinct issue
   - Scouts root causes in backend source and generated output
   - Plans concrete fixes
   - Reviews and implements fixes with worker→reviewer loops
   - Builds and publishes the backend, rebuilds Solitaire

---

### Additional Resources

- `~/Develop/Private/legacy/Solitaire/docs/backlog/minigame/research.md` — Original design decision research (WeChat API details, TeaVM internals, risk eliminations)
