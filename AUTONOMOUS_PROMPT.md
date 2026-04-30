# Autonomous Build: gdx-minigame WeChat Mini Game Backend

## Context

We are building a **WeChat Mini Game backend** (`backend-minigame`) for the gdx-minigame project (a fork of gdx-teavm). The project is at `/Users/daniel/workspace/gdx-minigame/`. We've already:

- Changed the group ID to `com.zourgames.gdx`
- Published all 5 existing modules to local Maven
- Completed exhaustive research (confirmed: TeaVM runtime is DOM-free, WebGL 1 only, MP3 audio, no VAOs, UMD works with WeChat require())

## Your Task

Execute the implementation plan at **`/Users/daniel/workspace/gdx-minigame/plan.md`** autonomously, phase by phase. The plan has been reviewed twice and is fully actionable — every file is accounted for across 4 tables (create, fork, copy, skip).

## Execution Rules

1. **Read the full plan first** before writing any code. It's ~1100 lines with complete file inventories, DOM dependency tables, and code templates.

2. **Background research:** Read `~/Develop/Private/legacy/Solitaire/docs/backlog/minigame/research.md` if you need context on why specific decisions were made (WeChat API details, TeaVM internals, risk eliminations).

3. **Execute in phase order:** P0 → P1 → Phase 1 → Phase 2 → Phase 6 → Phase 3 → Phase 4 → Phase 5 → Phase 7. Each phase has a **"Verification"** step — run it before moving on.

4. **Fork, don't invent.** The plan specifies exact source files to fork from `backends/backend-web/`. Read the original file first, understand its structure, then create the minigame version replacing only the DOM-dependent parts.

5. **Package rename rule:** When copying/forking files, change package from `com.github.xpenatan.gdx.teavm.backends.web` to `com.github.xpenatan.gdx.teavm.backends.minigame`. Update all imports accordingly.

6. **Commit after each phase** with a descriptive message like `feat(minigame): Phase 1 - module scaffolding` or `feat(minigame): Phase 3.1 - MiniGameApplication`.

7. **If a phase fails verification**, debug and fix before proceeding. Don't skip ahead with compilation errors.

8. **Stop after Phase 5** (adapter layer). Phases 7-8 (integration testing, size optimization) require WeChat DevTools and actual game projects — leave those for later sessions.

## Key Files You'll Need

| File | Purpose |
|------|---------|
| `plan.md` | The implementation plan (START HERE) |
| `buildSrc/src/main/kotlin/LibExt.kt` | Build config (group ID, versions) |
| `buildSrc/src/main/kotlin/publish.gradle.kts` | Publishing config |
| `settings.gradle.kts` | Module registration |
| `backends/backend-web/` | Source to fork from |
| `backends/backend-shared/` | Shared compile-time infrastructure |
| `research.md` | Background research document |

## What Success Looks Like

After completion, running `./gradlew :backends:backend-minigame:compileJava` should pass, and the module should contain:
- ~17 new files (WX bindings, runtime classes, build backend)
- ~15 forked files (adapted from backend-web with DOM removed)
- ~31 copied files (WebGL, emulations, utilities)
- `teavm.properties` and SPI service files for the emulation system
- A publishable Maven artifact `com.zourgames.gdx:backend-minigame`
