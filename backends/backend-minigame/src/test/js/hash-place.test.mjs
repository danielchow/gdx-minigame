import {readdirSync, readFileSync} from 'node:fs';
import vm from 'node:vm';
import assert from 'node:assert/strict';
import test from 'node:test';
import {typedArrays} from './engine.mjs';

// FibonacciHashTransformer rewrites libGDX's place() so a hash lookup allocates no BigInt. The compiled
// collections must still place keys, and so iterate, exactly as the JVM does.
const probes = new URL('../../../build/regression-probes/', import.meta.url);
const appOf = name => new URL(`${name}/minigame/subpackages/game/app.js`, probes);

// What the JVM produces for HashPlaceProbe.order(); FibonacciHashTest asserts the same literal there.
const expectedOrder = readFileSync(new URL('../java/com/github/xpenatan/gdx/teavm/backends/minigame/regression/HashPlaceProbe.java', import.meta.url), 'utf8')
    .match(/EXPECTED_ORDER =([^;]*);/)[1].match(/"[^"]*"/g).map(literal => JSON.parse(literal)).join('');

const MULTIPLIER = 0x9E3779B97F4A7C15n;
const toInt = value => Number(BigInt.asIntN(32, value));
// libGDX: (int)(hash * 0x9E3779B97F4A7C15L >>> shift), hash sign-extended from int.
const referencePlace = (hash, shift) => toInt(BigInt.asUintN(64, BigInt(hash) * MULTIPLIER) >> BigInt(shift & 63));
// LongMap: (int)((key ^ key >>> 32) * 0x9E3779B97F4A7C15L >>> shift).
const referencePlaceLong = (key, shift) => {
    const folded = BigInt.asIntN(64, key ^ (BigInt.asUintN(64, key) >> 32n));
    return toInt(BigInt.asUintN(64, folded * MULTIPLIER) >> BigInt(shift & 63));
};

export function startProbe() {
    const context = vm.createContext({exports: {}, console, performance, setTimeout, clearTimeout, ...typedArrays});
    vm.runInContext(readFileSync(appOf('hash-place'), 'utf8'), context, {timeout: 5000});
    let mainError;
    context.exports.main([], error => {mainError = error;});
    assert.equal(mainError, undefined);
    return (command, arg = 0) => context.__hashPlaceProbe(command, arg);
}

test('every compiled collection places keys as the JVM does, for every shift the probe reaches', () => {
    const run = startProbe();
    const lines = run('slots').trim().split('\n');
    const shifts = new Set();
    const classes = new Set();
    for (const line of lines) {
        const [name, shiftText, pairs] = line.split('|');
        const shift = Number(shiftText);
        shifts.add(shift);
        classes.add(name);
        for (const pair of pairs.split(',')) {
            const [keyText, slotText] = pair.split('=');
            const expected = name === 'LongMap' ? referencePlaceLong(BigInt(keyText), shift) : referencePlace(Number(keyText), shift);
            assert.equal(Number(slotText), expected, `${name} shift ${shift} key ${keyText}`);
        }
    }
    assert.deepEqual([...shifts].sort((a, b) => a - b), [44, 47, 53, 58, 63]);
    assert.equal(classes.size, 11, 'all libGDX hash collections');
});

test('iteration order is the JVM order', () => {
    assert.equal(startProbe()('order'), expectedOrder);
});

test('no compiled probe hashes with BigInt any more', () => {
    const names = readdirSync(probes);
    assert(names.includes('hash-place'));
    for (const name of names) {
        const app = readFileSync(appOf(name), 'utf8');
        // TeaVM 0.13 builds the long constant from its two words; later versions emit a BigInt literal.
        assert.doesNotMatch(app, /\(\s*2135587861\s*,\s*2654435769\s*\)/, `${name}: place() still multiplies a BigInt`);
        assert.doesNotMatch(app, /7046029254386353131n/, `${name}: place() still multiplies a BigInt`);
    }
    // The int replacement is there: Math.imul (aliased) by the multiplier's low word.
    assert.match(readFileSync(appOf('hash-place'), 'utf8'), /\(\w+,\s*2135587861\)/);
});
