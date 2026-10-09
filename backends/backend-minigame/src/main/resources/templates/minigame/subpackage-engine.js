// Engine subpackage entry — auto-executed by wx.loadSubpackage({name: 'engine'})
// Requires freetype.js and gdx.wasm.js locally from within the subpackage

try {
    var freetype = require('./freetype-loader.js');
    globalThis.__engineExports = globalThis.__engineExports || {};
    globalThis.__engineExports['freetype'] = freetype;
} catch(e) { console.error('[engine] freetype-loader.js require failed:', e.message); }

try {
    require('./gdx.wasm.js');
} catch(e) {
    globalThis.__engineError = e;
    console.error('[engine] gdx.wasm.js require failed:', e.message);
}

globalThis.__engineReady = true;
