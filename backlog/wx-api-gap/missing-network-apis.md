# Network extensions — file transfers and event sockets are missing

> **Status**: 📋 Researched
>
> **Severity**: 🔵 Low (cleanup or optional capability)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
WX exposes a raw request wrapper but no download/upload/WebSocket/TCP/UDP implementation was found. LibGDX streaming socket methods throw; the asset downloader now fails explicitly instead of hanging.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/bindings/WX.java` (lines 117–120).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameNet.java` (lines 162–173).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameAssetDownloader.java` (lines 15–22).

**What happens at runtime:**
Applications cannot use those native transfer/socket capabilities through this backend. HTTP transport is already implemented separately.

## WeChat Mini Game API Reference

**WX API that can fix this:**
`wx.downloadFile`, `wx.uploadFile`, `wx.connectSocket`, `wx.createTCPSocket` and `wx.createUDPSocket`.

**Doc path:**
- `references/wx-api-docs/network/download/wx.downloadFile.md`.
- `references/wx-api-docs/network/upload/wx.uploadFile.md`.
- `references/wx-api-docs/network/websocket/wx.connectSocket.md`.
- `references/wx-api-docs/network/tcp/wx.createTCPSocket.md`.
- `references/wx-api-docs/network/udp/wx.createUDPSocket.md`.

**API signature / key details:**
These APIs expose asynchronous task/event models. Standard LibGDX Net has no direct WebSocket method; its socket contract is different from the WX event model.

## Proposed Solution

**How to fix:**
Define explicit backend extension APIs for native transfers and event sockets. Separately assess whether any LibGDX streaming contract can be adapted faithfully.

**Binding changes needed (WX.java):**
Add typed task/socket overlays with result callbacks, progress, error, abort/close and listener removal as needed.

**Implementation changes needed:**
Implement remote asset downloading with clear success/failure ownership and path persistence. Do not treat a connectSocket binding alone as complete Gdx.net support.

## Notes

Follow domain/TLS/platform restrictions from the specific API docs. Existing downloader fail-fast behavior is not a pending callback hang; see [HTTP cancellation](net-xmlhttprequest-vs-wxrequest.md).
