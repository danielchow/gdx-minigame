// Game code subpackage entry — auto-executed by wx.loadSubpackage({name: 'game'})

try {
    var app = require('./${targetFileName}.js');
    globalThis.__gameApp = app;
} catch(e) {
    globalThis.__gameAppError = e;
    console.error('[game-code] ${targetFileName}.js require failed:', e.message);
}

globalThis.__gameCodeReady = true;
