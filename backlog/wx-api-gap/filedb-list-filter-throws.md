# Directory filters — FileFilter and FilenameFilter throw

> **Status**: 📋 Researched
>
> **Severity**: 🟡 Medium (missing contract behavior)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
Both java.io filter overloads explicitly throw GdxRuntimeException; suffix filtering already applies the requested suffix.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/filesystem/FileDB.java` (lines 83–101).

**What happens at runtime:**
Calling either filter overload fails. Unfiltered and suffix listings remain implemented.

## WeChat Mini Game API Reference

**WX API that can fix this:**
No additional WX API is needed.

**Doc path:**
Not applicable; this is a build/backend issue.

**API signature / key details:**
The implementation cites TeaVM java.io.File support as the constraint; the web reference has the same unsupported overloads.

## Proposed Solution

**How to fix:**
First establish whether TeaVM can safely supply the java.io.File values required by these interfaces. If compatible, evaluate each caller predicate for every candidate and return only accepted entries. Otherwise retain an explicit unsupported result and offer a separate FileHandle predicate extension.

**Binding changes needed (WX.java):**
None.

**Implementation changes needed:**
Do not replace either overload with list(file), which silently ignores the filter. A new extension must have a distinct API; it does not make the existing overloads compatible.

## Notes

Filter exceptions should propagate. Verify parent/name semantics for FilenameFilter and accepted/rejected candidates before claiming support.
