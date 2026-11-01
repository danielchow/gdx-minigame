import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import {createRequire} from 'node:module';
import {fileURLToPath} from 'node:url';
import vm from 'node:vm';
import test from 'node:test';
import {typedArrays} from './engine.mjs';

const adapter = readFileSync(new URL('../../main/resources/templates/minigame/adapter.js', import.meta.url), 'utf8');
const packageUrl = new URL('../../../build/regression-probes/pixmap-buffer/minigame/', import.meta.url);
const emptyWasm = new Uint8Array([0, 97, 115, 109, 1, 0, 0, 0]);
const dummy = new Uint8Array([0]);

function runtime({mode = 'standard', binary = emptyWasm, readError, wxError, adapterSource = adapter} = {}) {
    const calls = [];
    const standard = mode === 'none' || mode === 'wx-only' ? undefined : Object.create(WebAssembly);
    const wxWasm = mode === 'alias' ? standard : mode.startsWith('wx') ? {
        instantiate(path, imports) {
            calls.push({kind: 'wx', path, imports});
            return wxError ? Promise.reject(wxError) : WebAssembly.instantiate(binary, imports);
        }
    } : undefined;
    const context = vm.createContext({console, exports: {}, window: {}, setTimeout, clearTimeout, ...typedArrays,
        WebAssembly: standard, WXWebAssembly: wxWasm,
        wx: {getSystemInfoSync: () => ({pixelRatio: 1, screenWidth: 390, screenHeight: 844}),
            getFileSystemManager: () => ({readFile(options) {
                calls.push({kind: 'read', path: options.filePath, encoding: options.encoding});
                if (readError) options.fail({errMsg: readError});
                else options.success({data: binary.buffer.slice(binary.byteOffset, binary.byteOffset + binary.byteLength)});
            }})}});
    vm.runInContext(adapterSource, context, {filename: 'adapter.js'});
    return {context, calls};
}

for (const mode of ['wx', 'wx-only', 'standard', 'alias']) {
    test(mode + ': resolves the dummy through the extracted package, not its zero byte', async () => {
        const {context, calls} = runtime({mode});
        const imports = {};
        const result = await context.WebAssembly.instantiate(dummy, imports);
        assert.ok(result.instance instanceof WebAssembly.Instance);
        assert.equal(calls.length, 1);
        assert.equal(calls[0].path, 'subpackages/engine/gdx.wasm');
        assert.equal(calls[0].kind, mode.startsWith('wx') ? 'wx' : 'read');
        if (mode.startsWith('wx')) assert.equal(calls[0].imports, imports);
        else assert.equal(calls[0].encoding, undefined, 'read binary, never UTF-8');
    });
}

test('recognizes the dummy ArrayBuffer and an offset typed-array view', async () => {
    for (const source of [dummy.buffer, new Uint8Array([7, 0, 9]).subarray(1, 2)]) {
        const {context, calls} = runtime();
        await context.WebAssembly.instantiate(source, {});
        assert.equal(calls.length, 1);
    }
});

test('passes other bytes and compiled modules to the native API with its overload intact', async () => {
    const {context, calls} = runtime({mode: 'wx'});
    const result = await context.WebAssembly.instantiate(emptyWasm, {});
    assert.ok(result.instance instanceof WebAssembly.Instance);
    const instance = await context.WebAssembly.instantiate(result.module, {});
    assert.ok(instance instanceof WebAssembly.Instance);
    await assert.rejects(context.WebAssembly.instantiate(new Uint8Array([1]), {}));
    assert.equal(calls.length, 0);
});

test('package read errors reject with the path, without trying the dummy', async () => {
    const {context} = runtime({readError: 'permission denied'});
    await assert.rejects(context.WebAssembly.instantiate(dummy, {}), /subpackages\/engine\/gdx\.wasm: permission denied/);
});

test('malformed package bytes and WX errors remain failures, not silent fallbacks', async () => {
    const {context} = runtime({binary: new Uint8Array([7])});
    await assert.rejects(context.WebAssembly.instantiate(dummy, {}), WebAssembly.CompileError);
    const failure = new Error('WX compile failed');
    const wx = runtime({mode: 'wx', wxError: failure});
    await assert.rejects(wx.context.WebAssembly.instantiate(dummy, {}), error => error === failure);
    assert.equal(wx.calls.length, 1);
    assert.equal(wx.calls[0].kind, 'wx');
});

test('no WASM runtime gives an explicit rejected promise', async () => {
    const {context} = runtime({mode: 'none'});
    await assert.rejects(context.WebAssembly.instantiate(dummy, {}), /No WASM support/);
});

for (const mode of ['wx', 'standard']) {
    test(mode + ': the real packaged Gdx engine initializes through its generated adapter', {timeout: 15000}, async () => {
        const engineUrl = new URL('subpackages/engine/gdx.wasm.js', packageUrl);
        const binary = readFileSync(new URL('subpackages/engine/gdx.wasm', packageUrl));
        const {context, calls} = runtime({mode, binary,
            adapterSource: readFileSync(new URL('adapter.js', packageUrl), 'utf8')});
        let ready;
        const initialized = new Promise(resolve => { ready = resolve; });
        Object.defineProperty(context.window, 'Gdx', {configurable: true, set: ready});
        Object.assign(context, {module: {exports: {}}, require: createRequire(engineUrl), process, Buffer,
            __dirname: fileURLToPath(new URL('.', engineUrl)), __filename: fileURLToPath(engineUrl)});
        vm.runInContext(readFileSync(engineUrl, 'utf8'), context, {filename: 'gdx.wasm.js'});
        const gdx = await initialized;
        const ptr = gdx._malloc(8);
        assert.ok(ptr > 0);
        gdx._free(ptr);
        assert.equal(calls.length, 1);
        assert.equal(calls[0].kind, mode === 'wx' ? 'wx' : 'read');
    });
}
