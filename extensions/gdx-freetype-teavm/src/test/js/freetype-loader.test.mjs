import {readFileSync} from 'node:fs';
import {createRequire} from 'node:module';
import {fileURLToPath} from 'node:url';
import vm from 'node:vm';
import assert from 'node:assert/strict';
import test from 'node:test';

const resources = new URL('../../main/resources/', import.meta.url);
const loaderUrl = new URL('freetype-loader.js', resources);

/** Runs the shipped loader as WeChat does: WXWebAssembly.instantiate(path, imports), globals on globalThis. */
function runLoader(instantiate) {
    const context = vm.createContext({console, process, Buffer, setTimeout, clearTimeout, WebAssembly,
        TextDecoder, URL, WXWebAssembly: {instantiate}});
    context.globalThis = context;
    context.require = createRequire(loaderUrl);
    context.__dirname = fileURLToPath(resources);
    context.__filename = fileURLToPath(loaderUrl);
    context.module = {exports: {}};
    context.exports = context.module.exports;
    vm.runInContext(readFileSync(loaderUrl, 'utf8'), context, {filename: 'freetype-loader.js'});
    return context;
}

const realWasm = (path, imports) =>
    WebAssembly.instantiate(readFileSync(new URL(path.replace(/^.*\//, ''), resources)), imports);

test('bridged heap views follow FreeType memory growth', {timeout: 15000}, async () => {
    const context = runLoader(realWasm);
    await context.__freetypeReady;
    const Module = context.Module;
    const before = Module.HEAP8.byteLength;
    // Fonts and glyph caches are malloc'd into this 4 MiB heap; force it to grow.
    const pointer = Module._malloc(6 * 1024 * 1024);
    try {
        assert.notEqual(pointer, 0, 'allocation must succeed by growing the heap');
        assert.ok(Module.HEAP8.byteLength > before, 'HEAP8 must view the grown buffer, not the detached old one');
        assert.equal(Module.HEAPU8.byteLength, Module.HEAP8.byteLength);
        Module.HEAP8[pointer + 6 * 1024 * 1024 - 1] = 42;
        assert.equal(Module.HEAPU8[pointer + 6 * 1024 * 1024 - 1], 42, 'the bridged view writes the live heap');
    } finally {
        Module._free(pointer);
    }
});

test('failed FreeType instantiation rejects readiness instead of resolving it', {timeout: 15000}, async () => {
    const failure = new Error('instantiate failed');
    const context = runLoader(() => Promise.reject(failure));
    await assert.rejects(context.__freetypeReady, error => error === failure);
});
