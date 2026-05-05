// Game code subpackage entry — auto-executed by wx.loadSubpackage({name: 'game'})

console.log('[game-code] game.js executing');
try {
    var app = require('./${targetFileName}.js');
    globalThis.__gameApp = app;
    console.log('[game-code] ${targetFileName}.js loaded, main:', typeof app.main);
} catch(e) { console.error('[game-code] ${targetFileName}.js require failed:', e.message); }

globalThis.__gameCodeReady = true;
console.log('[game-code] game.js done');
