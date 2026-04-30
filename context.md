# Code Context

## Files Retrieved
1. `~/Develop/GitHub/game/teavm/core/src/main/java/org/teavm/vm/TeaVMTarget.java` (lines 1-72) - Core target interface, defines `emit()` with single output
2. `~/Develop/GitHub/game/teavm/core/src/main/java/org/teavm/vm/TeaVMTargetController.java` (lines 1-50) - Controller interface, no split-related methods
3. `~/Develop/GitHub/game/teavm/core/src/main/java/org/teavm/backend/javascript/JavaScriptTarget.java` (full file ~827 lines) - JS backend implementation
4. `~/Develop/GitHub/game/teavm/core/src/main/java/org/teavm/backend/javascript/JSModuleType.java` (lines 1-27) - Module type enum: COMMON_JS, UMD, NONE, ES2015
5. `~/Develop/GitHub/game/teavm/core/src/main/java/org/teavm/vm/TeaVMBuilder.java` (lines 1-93) - Builder API, no split configuration
6. `~/Develop/GitHub/game/teavm/core/src/main/java/org/teavm/vm/TeaVM.java` (lines 120-470) - Main VM class, single `build(target, outputName)` call
7. `~/Develop/GitHub/game/teavm/core/src/main/java/org/teavm/vm/BuildTarget.java` (lines 1-28) - Simple `createResource(fileName)` interface
8. `~/Develop/GitHub/game/teavm/tools/core/src/main/java/org/teavm/tooling/TeaVMTool.java` (lines 1-50, grep for split/chunk) - No split-related configuration
9. Git commit `e9e291b22` on branch `origin/js-code-splitting` - Historical code splitting implementation (removed from master)

## Key Code

### TeaVMTarget.emit() — Single Output Signature
```java
// TeaVMTarget.java:59
void emit(ListableClassHolderSource classes, BuildTarget buildTarget, String outputName) throws IOException;
```

### JavaScriptTarget.emit() — Produces Exactly One File
```java
// JavaScriptTarget.java:349-357
@Override
public void emit(ListableClassHolderSource classes, BuildTarget target, String outputName) {
    try (OutputStream output = target.createResource(outputName);
            Writer writer = new OutputStreamWriter(output, StandardCharsets.UTF_8)) {
        emit(classes, writer, target);
    } catch (IOException e) {
        throw new RenderingException(e);
    }
}
```

### JSModuleType — Module Output Formats (Not Code Splitting)
```java
// JSModuleType.java
public enum JSModuleType {
    COMMON_JS,
    UMD,
    NONE,
    ES2015
}
```

### BuildTarget — Simple Single-Resource Interface
```java
// BuildTarget.java
public interface BuildTarget {
    OutputStream createResource(String fileName) throws IOException;
}
```

### Historical Code Splitting API (Removed, from commit e9e291b22)
```java
// These classes existed on the js-code-splitting branch but were NEVER merged to master:

// interop/CodeFragment.java — User-facing annotation point
public interface CodeFragment {
    void run();
}

// interop/CodeLoader.java — Split point declaration
public final class CodeLoader {
    public static void split(CodeFragment codeFragment) {
        codeFragment.run();
    }
}

// backend/javascript/splitting/RegionAnalyzer.java — Core splitting analysis
// Analyzed call graph to partition code into regions/parts
// Methods: analyze(entryPoint), hasSplitting(), getParts()

// backend/javascript/rendering/RenderingFilter.java — Filter interface for splitting
// Defined isExternal/isShared for methods, classes, fields, strings
// Used to determine what goes in each chunk

// backend/javascript/RegionFilter.java — Region-based filter implementation
// backend/javascript/splitting/CodeFragmentTransformer.java — Transformed IR for splitting
// backend/javascript/splitting/AsyncLauncher.java — Async chunk loading
```

## Architecture

### Current State (master branch)
- TeaVM compiles to a **single monolithic output file** per compilation.
- The `TeaVMTarget.emit()` API takes exactly one `outputName` and produces one file.
- The `JavaScriptTarget` writes everything to a single Writer stream — runtime, string pool, all class metadata, and all method bodies in one file.
- Module types (`UMD`, `COMMON_JS`, `ES2015`, `NONE`) only control the **wrapper format** around the monolithic output, not chunk splitting.
- The `JSModuleType.ES2015` mode produces `import`/`export` statements but the output is still a single file.
- TeaVM can `importModule()` external JS modules, but this is for consuming external libraries, not splitting TeaVM output.

### Historical Code Splitting (js-code-splitting branch)
- In June 2022, commit `e9e291b22` ("Implement code splitting") added code splitting infrastructure on the `js-code-splitting` branch.
- The API used `CodeLoader.split(CodeFragment)` as a declarative split point.
- `RegionAnalyzer` performed call-graph analysis to partition code into regions.
- `RenderingFilter` determined what went into each chunk vs. was shared.
- This branch has **one commit ahead of its base** and has **never been merged to master**.
- All splitting files (`splitting/`, `RegionFilter.java`, `RenderingFilter.java`, `CodeFragment.java`, `CodeLoader.java`) do **not exist on master**.
- The branch appears abandoned — it's based on a very old commit from ~2022.

### Related Features Available on Master
1. **Incremental compilation**: `TeaVMTool.setIncremental(true)` with `IncrementalDirectoryBuildTarget` for faster rebuilds
2. **Module output**: `JSModuleType.ES2015` / `COMMON_JS` for module-system-compatible single-file output
3. **Multiple compilation units**: You can run TeaVM multiple times with different entry points to produce separate JS files, but they would each be independent (no shared runtime deduplication)

## Start Here
1. **`core/src/main/java/org/teavm/backend/javascript/JavaScriptTarget.java`** — The JS backend that produces output. Confirms single-file output via `emit()` at line 349.
2. **`core/src/main/java/org/teavm/vm/TeaVMTarget.java`** — The target interface showing `emit()` takes a single `outputName`.
3. Git branch `origin/js-code-splitting` — The only code splitting implementation ever attempted, never merged.

---

## Summary Answer

**TeaVM does NOT support code splitting natively on the current master branch.**

- There is no API like `withSplitTarget`, `splitTarget`, `codeSplitting`, or any configuration for outputting multiple JS chunks.
- The `TeaVMTarget.emit()` interface is fundamentally single-file: `emit(classes, buildTarget, outputName)`.
- `JavaScriptTarget` writes everything to one output stream.
- `JSModuleType` only controls the wrapper format (UMD, CommonJS, ES2015, IIFE), not chunking.

**Historical attempt**: A `js-code-splitting` branch exists (commit `e9e291b22`, June 2022) with `CodeLoader.split()` API, `RegionAnalyzer`, and `RenderingFilter`, but it was never merged and appears abandoned. All those files are absent from master.

**Workarounds**:
1. Run TeaVM multiple times with different entry points → multiple independent JS files (but duplicated runtime).
2. Use `JSModuleType.ES2015` output and manually lazy-load the single bundle via dynamic `import()`.
3. Post-process the single output file with a JS bundler (e.g., webpack/rollup) that can split it.
4. Use the incremental compilation feature (`setIncremental(true)`) for faster rebuilds during development.
