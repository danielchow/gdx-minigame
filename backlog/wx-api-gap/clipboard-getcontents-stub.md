# Clipboard reads — Java cache never refreshes

> **Status**: 📋 Researched
>
> **Severity**: 🟡 Medium (missing contract behavior)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
getContents and hasContents read only the Java-side string; setContents updates that string and calls wx.setClipboardData.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameClipboard.java` (lines 12–28).

**What happens at runtime:**
Clipboard changes made outside the game are not reflected in reads.

## WeChat Mini Game API Reference

**WX API that can fix this:**
`wx.getClipboardData`.

**Doc path:**
- `references/wx-api-docs/device/clipboard/wx.getClipboardData.md`.

**API signature / key details:**
The asynchronous success callback receives one object whose data property is the string.

## Proposed Solution

**How to fix:**
Provide an explicit async refresh/read operation and define getContents as the last cached value. Refresh on a deliberate paste action or another documented trigger; a synchronous call cannot promise a fresh native value.

**Binding changes needed (WX.java):**
Use a result-object callback and read res.data, or wrap success as `function(result) { callback(result.data); }` for a string callback. Include failure handling; do not pass a string callback directly to success.

**Implementation changes needed:**
Update the cache only on accepted results, prevent an older read from overwriting a newer setContents, and define hasContents as cache state. Document when refresh completes.

## Notes

Automatic polling or resume reads may have platform UI effects and need validation. Do not block the JS event loop waiting for the callback.
