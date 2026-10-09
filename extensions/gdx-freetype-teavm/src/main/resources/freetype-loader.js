(function() {
    var createModule = require('./freetype.js');
    var readyResolve, readyReject;
    // Rejects when FreeType cannot load, so startup can report the failure instead of failing later.
    globalThis.__freetypeReady = new Promise(function(resolve, reject) { readyResolve = resolve; readyReject = reject; });
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
                    // rejection that hangs createModule() forever. Fail readiness directly.
                    readyReject(err);
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
                key === 'writeArrayToMemory' ||
                key === 'ccall' || key === 'cwrap' || key === 'setValue' || key === 'getValue') {
                g.Module[key] = ftModule[key];
            }
        }
        // The heap grows (4 to 16 MiB), and growth replaces ftModule.HEAP8/HEAPU8 with views of the new
        // buffer. A copied view would stay on the old, detached buffer, so read through to the current one.
        ['HEAP8', 'HEAPU8'].forEach(function(key) {
            Object.defineProperty(g.Module, key, {
                configurable: true, enumerable: true,
                get: function() { return ftModule[key]; }
            });
        });
        console.log('[freetype] WASM module loaded and bridged to globalThis.Module');
        readyResolve();
    }).catch(function(err) {
        console.error('[freetype] Failed to load:', err);
        readyReject(err);
    });
})();
