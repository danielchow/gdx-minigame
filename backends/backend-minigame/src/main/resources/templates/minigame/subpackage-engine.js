// Engine subpackage entry — auto-executed by wx.loadSubpackage({name: 'engine'})
// Requires freetype.js and gdx.wasm.js locally from within the subpackage

console.log('[engine] game.js executing');
try {
    var freetype = require('./freetype-loader.js');
    globalThis.__engineExports = globalThis.__engineExports || {};
    globalThis.__engineExports['freetype'] = freetype;
    console.log('[engine] freetype-loader.js loaded, exports:', typeof freetype);
} catch(e) { console.error('[engine] freetype-loader.js require failed:', e.message); }

try {
    var wasmModule = require('./gdx.wasm.js');
    console.log('[engine] gdx.wasm.js loaded');
} catch(e) { console.error('[engine] gdx.wasm.js require failed:', e.message); }

globalThis.__engineReady = true;
console.log('[engine] game.js done');
