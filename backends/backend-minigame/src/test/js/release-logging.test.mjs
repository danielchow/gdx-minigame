import {readdirSync, readFileSync} from 'node:fs';
import assert from 'node:assert/strict';
import test from 'node:test';

// Release builds of the regression probes, and the files MiniGameBackend generates from templates.
const probes = new URL('../../../build/regression-probes/', import.meta.url);
const release = name => new URL(`${name}/minigame/`, probes);
const generated = ['game.js', 'adapter.js', 'first-screen.js', 'subpackages/engine/game.js',
    'subpackages/game/game.js', 'assets/game.js'];

test('compiled game code carries no [PERF] logging', () => {
    const names = readdirSync(probes);
    assert(names.includes('runtime-robustness'), 'the probes are built');
    for (const name of names) {
        const app = readFileSync(new URL('subpackages/game/app.js', release(name)), 'utf8');
        assert(!app.includes('[PERF]'), `${name}/subpackages/game/app.js logs [PERF] lines`);
    }
});

test('generated scripts log only errors and warnings', () => {
    for (const file of generated) {
        const script = readFileSync(new URL(file, release('async-completion')), 'utf8');
        assert(!script.includes('[PERF]'), `${file} logs [PERF] lines`);
        assert.doesNotMatch(script, /console\.(log|info|debug)\b/, `${file} logs progress`);
    }
});
