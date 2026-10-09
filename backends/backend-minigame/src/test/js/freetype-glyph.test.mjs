import {readFileSync} from 'node:fs';
import {createRequire} from 'node:module';
import {fileURLToPath} from 'node:url';
import vm from 'node:vm';
import assert from 'node:assert/strict';
import test from 'node:test';
import {loadEngine, typedArrays} from './engine.mjs';

const packageUrl = new URL('../../../build/regression-probes/freetype-glyph/minigame/', import.meta.url);
const engineDir = new URL('subpackages/engine/', packageUrl);
const font = new URL('../../../../../examples/freetype/desktop/assets/data/lsans.ttf', import.meta.url);

/** Runs the packaged freetype-loader.js as WeChat does and returns its bridged Module. */
async function loadFreeType() {
    const loaderUrl = new URL('freetype-loader.js', engineDir);
    const context = vm.createContext({console, process, Buffer, setTimeout, clearTimeout, WebAssembly, TextDecoder, URL,
        WXWebAssembly: {instantiate: (path, imports) =>
            WebAssembly.instantiate(readFileSync(new URL(path.replace(/^.*\//, ''), engineDir)), imports)}});
    context.globalThis = context;
    context.require = createRequire(loaderUrl);
    context.__dirname = fileURLToPath(engineDir);
    context.module = {exports: {}};
    context.exports = context.module.exports;
    vm.runInContext(readFileSync(loaderUrl, 'utf8'), context, {filename: 'freetype-loader.js'});
    await context.__freetypeReady;
    return context.Module;
}

test('the FreeType gamma-1 fast path draws the glyph into the native pixmap', {timeout: 20000}, async () => {
    const {gdx} = await loadEngine(new URL('gdx.wasm.js', engineDir), readFileSync(new URL('gdx.wasm', engineDir)));
    const Module = await loadFreeType();
    const probe = {};
    const app = {exports: {}, console, Gdx: gdx, window: {Gdx: gdx}, Module, __glyphProbe: probe,
        __fontBytes: new Int8Array(readFileSync(font)), setTimeout, clearTimeout, ...typedArrays};
    app.globalThis = app;
    vm.runInNewContext(readFileSync(new URL('subpackages/game/app.js', packageUrl), 'utf8'), app);
    await new Promise((resolve, reject) => app.exports.main([], error => error ? reject(error) : resolve()));
    assert.equal(probe.complete, 1);
    assert.equal(probe.pixelModeGray, 1, 'glyph renders as 8-bit gray');
    assert.equal(probe.rowBytesIsWidth, 1, 'the bitmap qualifies for the fast path');
    assert.ok(probe.width > 0 && probe.height > 0);
    assert.ok(probe.covered > probe.width, `native pixmap has glyph coverage (covered=${probe.covered})`);
});
