{
  "id": "d322cbef",
  "title": "BUG: Infinite recursion in MiniGameGraphics getDensity↔getPpiX",
  "tags": [
    "bug",
    "phase3",
    "minigame"
  ],
  "status": "done",
  "created_at": "2026-05-01T02:28:43.636Z"
}

Fixed: Changed getPpiX()/getPpiY() to use getNativeScreenDensity() instead of getDensity(), breaking the infinite recursion cycle. Build passes.
