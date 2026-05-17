(function() {
    var createModule = require('./freetype.js');
    var readyResolve;
    globalThis.__freetypeReady = new Promise(function(resolve) { readyResolve = resolve; });
    var moduleConfig = {
        instantiateWasm: function(imports, successCallback) {
            if (typeof WXWebAssembly !== 'undefined') {
                WXWebAssembly.instantiate('subpackages/engine/freetype.wasm', imports).then(function(result) {
                    // WXWebAssembly.instantiate may return {instance,module} or just the instance
                    var inst = result.instance || result;
                    var mod = result.module || undefined;
                    successCallback(inst, mod);
                }, function(err) {
                    console.error('[freetype] WXWebAssembly.instantiate failed:', err);
                    // Do not call successCallback(null, null) — Emscripten's internal
                    // receiveInstance(null) crashes on null.exports, causing an unhandled
                    // rejection that hangs createModule() forever.
                    // Instead, resolve readiness directly so the game can proceed.
                    readyResolve();
                });
                return {};
            }
            return undefined;
        }
    };
    createModule(moduleConfig).then(function(ftModule) {
        var g = globalThis;
        g.Module = g.Module || {};
        var keys = Object.keys(ftModule);
        for (var i = 0; i < keys.length; i++) {
            var key = keys[i];
            if (key.startsWith('_c_') || key === '_malloc' || key === '_free' ||
                key === 'writeArrayToMemory' || key === 'HEAP8' || key === 'HEAPU8' ||
                key === 'ccall' || key === 'cwrap' || key === 'setValue' || key === 'getValue') {
                g.Module[key] = ftModule[key];
            }
        }
        console.log('[freetype] WASM module loaded and bridged to globalThis.Module');
        readyResolve();
    }).catch(function(err) {
        console.error('[freetype] Failed to load:', err);
        readyResolve();
    });
})();
