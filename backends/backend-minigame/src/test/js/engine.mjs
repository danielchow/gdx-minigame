import {readFileSync} from 'node:fs';
import {createRequire} from 'node:module';
import {fileURLToPath} from 'node:url';
import vm from 'node:vm';

export const typedArrays = {Int8Array, Uint8Array, Uint8ClampedArray, Int16Array, Uint16Array,
    Int32Array, Uint32Array, Float32Array, Float64Array, BigInt64Array, BigUint64Array, ArrayBuffer, DataView};

/**
 * Runs an unmodified gdx.wasm.js and resolves with its window.Gdx module and WASM imports.
 * Packaged builds replace the embedded binary with a dummy; pass the extracted gdx.wasm bytes.
 */
export async function loadEngine(engineUrl, wasmBytes) {
    const wasm = Object.create(WebAssembly);
    let imports;
    wasm.instantiate = (embedded, engineImports) => {
        imports = engineImports;
        return WebAssembly.instantiate(wasmBytes ?? embedded, engineImports);
    };
    let ready;
    const initialized = new Promise(resolve => {ready = resolve;});
    const window = Object.defineProperty({}, 'Gdx', {set: ready});
    const context = {module: {exports: {}}, require: createRequire(engineUrl), process, console, Buffer,
        __dirname: fileURLToPath(new URL('.', engineUrl)), __filename: fileURLToPath(engineUrl),
        WebAssembly: wasm, window, setTimeout, clearTimeout, ...typedArrays};
    context.exports = context.module.exports;
    vm.runInNewContext(readFileSync(engineUrl, 'utf8'), context, {filename: 'gdx.wasm.js'});
    return {gdx: await initialized, imports};
}
