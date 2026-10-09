import {readFileSync} from 'node:fs';
import vm from 'node:vm';
import assert from 'node:assert/strict';
import test from 'node:test';
import {typedArrays} from './engine.mjs';

const app = new URL('../../../build/regression-probes/gl-upload-range/minigame/subpackages/game/app.js', import.meta.url);
const range = (from, to, step = 1) => Array.from({length: to - from}, (_, i) => (from + i) * step).join(',');

test('GL uploads only the elements they are given', () => {
    // Records the typed-array argument of each data call; everything else is a harmless no-op.
    const calls = {};
    const dataArg = {bufferData: 1, bufferSubData: 2, vertexAttrib3fv: 1, uniform4fv: 1, uniform2iv: 1,
        uniform1iv: 1, uniformMatrix4fv: 2, uniformMatrix3fv: 2};
    const gl = new Proxy({}, {
        get(target, name) {
            if (typeof name !== 'string') return undefined;
            if (name === 'createBuffer' || name === 'createProgram' || name === 'getUniformLocation') return () => ({});
            return (...args) => {
                if (name in dataArg) {
                    // Copy at call time: the probe reuses the same Java buffers afterwards.
                    (calls[name] ??= []).push(Array.from(args[dataArg[name]]));
                }
                return 0;
            };
        }
    });
    const probe = {results: {}, complete: false};
    const context = vm.createContext({exports: {}, console, __glMock: gl, __glCalls: calls, __glProbe: probe,
        setTimeout, clearTimeout, ...typedArrays});
    vm.runInContext(readFileSync(app, 'utf8'), context, {timeout: 5000});
    vm.runInContext('exports.main([], error => { if (error) throw error; })', context, {timeout: 5000});
    assert.equal(probe.complete, true, 'probe must finish');

    const r = probe.results;
    assert.equal(r['vbo-bufferData'], range(1, 41), 'VBO uploads the 40 floats set, not its 20000-float capacity');
    assert.equal(r['bufferSubData'], range(8, 12, 10), 'bufferSubData uploads [position, limit)');
    assert.equal(r['uniformMatrix4fv-buffer'], range(4, 20, 10), 'mat4 uploads 16 floats from position');
    assert.equal(r['uniform4fv-array'], range(4, 12), 'uniform4fv uploads count * 4 floats from offset');
    assert.equal(r['uniformMatrix3fv-array'], range(6, 15), 'mat3 uploads 9 floats from offset');
    assert.equal(r['uniform2iv-buffer'], '6,9', 'uniform2iv uploads count * 2 ints from position');
    assert.equal(r['uniform1iv-array'], '8,7,6', 'uniform1iv uploads count ints from offset');
    assert.equal(r['vertexAttrib3fv'], '200,210,220', 'vertexAttrib3fv uploads 3 floats from position');
});
