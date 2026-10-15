# Onscreen keyboard — null options and missing events

> **Status**: 📋 Researched
>
> **Severity**: 🟡 Medium (missing contract behavior)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
Showing the keyboard passes null to WX. Keyboard type is ignored; the input binding is present but confirm, complete and height events are not wired.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameInput.java` (lines 173–182).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/bindings/WX.java` (lines 165–172).

**What happens at runtime:**
The call lacks the documented required options. Whether a particular client rejects it or applies defaults is unverified; no failure was reproduced.

## WeChat Mini Game API Reference

**WX API that can fix this:**
`wx.showKeyboard`, `wx.onKeyboardInput`, `wx.onKeyboardConfirm`, `wx.onKeyboardComplete`, `wx.onKeyboardHeightChange` and corresponding removal APIs.

**Doc path:**
- `references/wx-api-docs/device/keyboard/wx.showKeyboard.md`.
- `references/wx-api-docs/device/keyboard/wx.onKeyboardConfirm.md`.
- `references/wx-api-docs/device/keyboard/wx.onKeyboardComplete.md`.
- `references/wx-api-docs/device/keyboard/wx.onKeyboardHeightChange.md`.

**API signature / key details:**
Provide defaultValue, maxLength, multiple, confirmHold, confirmType and keyboardType. The documented keyboardType values are text/number; numeric keyboard support is client-version dependent.

## Proposed Solution

**How to fix:**
Pass an explicit options object with defaults and failure handling, then map supported types and native input configuration. Coordinate event handling with [keyboard input support](keyboard-textinput-stub.md).

**Binding changes needed (WX.java):**
Add option construction and missing event/on-off bindings. Input/confirm/complete receive result objects with text; the height event supplies height.

**Implementation changes needed:**
Wire options and keyboard events in MiniGameInput; define when confirmation commits, when closing cancels, and how height is delivered to the observer.

## Notes

getTextInput already schedules canceled(); this keyboard path is separate. Unsupported keyboard types need a documented fallback.
