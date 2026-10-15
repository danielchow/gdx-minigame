# Keyboard input — native field configuration and events are unwired

> **Status**: 📋 Researched
>
> **Severity**: 🟡 Medium (missing contract behavior)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
getTextInput now posts canceled() rather than leaving the listener pending. Native input configuration, keyboard type and height observers are still ignored.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameInput.java` (lines 161–182).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/bindings/WX.java` (lines 165–172).

**What happens at runtime:**
Text dialogs cancel on the next frame. Native field calls only toggle visibility, and observers receive no keyboard-height events.

## WeChat Mini Game API Reference

**WX API that can fix this:**
`wx.showKeyboard` and input/confirm/complete/height events.

**Doc path:**
- `references/wx-api-docs/device/keyboard/wx.showKeyboard.md`.
- `references/wx-api-docs/device/keyboard/wx.onKeyboardConfirm.md`.
- `references/wx-api-docs/device/keyboard/wx.onKeyboardComplete.md`.
- `references/wx-api-docs/device/keyboard/wx.onKeyboardHeightChange.md`.

**API signature / key details:**
Use the option name keyboardType, with supported text/number values. Native keyboard UI is not a full LibGDX text-input dialog.

## Proposed Solution

**How to fix:**
Define a session model for the native field, mapping initial text, length, multiline and supported keyboard types. Decide whether getTextInput should remain explicitly canceled or use a keyboard-backed session.

**Binding changes needed (WX.java):**
Add options and missing on/off event bindings with one result-object callback; reuse the existing input event binding.

**Implementation changes needed:**
Route text edits, confirmation and closure to the correct session on the application thread; complete a listener at most once. Register/unregister height observers and prevent stale callbacks after a new session.

## Notes

Fix [null keyboard options](showkeyboard-null-options.md) as part of this work. Successful native-field integration remains unverified.
