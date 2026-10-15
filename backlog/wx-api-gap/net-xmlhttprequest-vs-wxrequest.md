# HTTP adapter — cancellation does not abort transport

> **Status**: 📋 Researched
>
> **Severity**: 🟡 Medium (missing contract behavior)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
MiniGameNet creates XHR, and the adapter supplies wx.request-backed XHR when native XHR is absent. cancelHttpRequest only removes/notifies the listener. The wrapper discards RequestTask and abort is empty.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameNet.java` (lines 31–35).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameNet.java` (lines 148–153).
- `backends/backend-minigame/src/main/resources/templates/minigame/adapter.js` (lines 119–196).

**What happens at runtime:**
Cancellation updates Java listener state while a native request may continue. Binary responses, timeout and header parity also need review; no blanket HTTP failure is established.

## WeChat Mini Game API Reference

**WX API that can fix this:**
`wx.request` returns RequestTask, whose abort method cancels the native request.

**Doc path:**
- `references/wx-api-docs/network/request/wx.request.md`.
- `references/wx-api-docs/network/request/RequestTask.abort.md`.

**API signature / key details:**
The current wrapper forces text data and stringifies non-string responses. Native-XHR and polyfilled-XHR paths may differ.

## Proposed Solution

**How to fix:**
Keep a per-request transport handle and implement actual abort. Review required XHR parity, or replace the wrapper path with direct wx.request while preserving HttpResponse semantics.

**Binding changes needed (WX.java):**
Retain RequestTask in the adapter and wire abort, or return a typed task from a direct Java request binding.

**Implementation changes needed:**
Associate HttpRequest with its active transport; remove state and notify exactly once on completion/error/cancel. Ignore late completion after cancellation and map binary/timeout/header behavior explicitly.

## Notes

Edit external templates for adapter changes. This is a cancellation/parity issue, not a claim that HTTP has no native WeChat transport.
