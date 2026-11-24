import {readFileSync} from 'node:fs';
import vm from 'node:vm';
import {createRequire} from 'node:module';
const {create} = createRequire(import.meta.url)('../../main/resources/templates/minigame/asset-packages.js');
import assert from 'node:assert/strict';
import test from 'node:test';

const template = readFileSync(new URL('../../main/resources/templates/minigame/game-inline.js', import.meta.url), 'utf8')
    .replace('${targetFileName}', 'app')
    .replace('${mainClassArgs}', '');

const quiet = new Proxy({}, {get: () => () => {}});
const settle = () => new Promise(resolve => setImmediate(resolve));
async function settleAll() {for (let i = 0; i < 10; i++) await settle();}

function startInline({assetsOutcome, gdx = true, freetypeReject = false, appError, mainCallbackError,
    packageIndex = '{}', unsupported = false, console = quiet} = {}) {
    const events = [];
    const loads = [];
    const modals = [];
    const restarts = [];
    const exits = [];
    const timers = [];
    const mainCalls = [];
    const screen = {
        start() {events.push('start');},
        setProgress(name, pct) {events.push(['progress', name, pct]);},
        redraw() {events.push('redraw');},
        end() {events.push('end');},
    };
    const context = vm.createContext({console, Date, Promise, Error,
        setTimeout(callback, delay) {timers.push({callback, delay}); return timers.length;},
        clearTimeout(id) {if (timers[id - 1]) timers[id - 1].cleared = true;}});
    vm.runInContext('var window = globalThis;', context);
    context.require = name => {
        if (name === './asset-packages') return {create};
        if (name === './adapter.js') return {setup() {}};
        if (name === './first-screen') return screen;
        if (name === './engine/freetype-loader.js') {
            events.push('req:freetype');
            if (freetypeReject) context.__freetypeReady = {then: (res, rej) => rej(new Error('ft fail'))};
            return {};
        }
        if (name === './engine/gdx.wasm.js') {
            events.push('req:gdx');
            if (gdx) setImmediate(() => {context.window.Gdx = {};});
            return {};
        }
        if (name === './app.js') {
            events.push('req:app');
            if (appError) throw appError;
            return {main(args, cb) {mainCalls.push(args); cb(mainCallbackError);}};
        }
        throw new Error('Unexpected require: ' + name);
    };
    const outcomes = [...(assetsOutcome ?? ['ok'])];
    context.wx = {
        createCanvas: () => ({width: 1, height: 1}),
        getFileSystemManager: () => ({readFileSync(path) {
            if (path === 'assets/asset-packages.json') return packageIndex;
            throw new Error('missing');
        }}),
        onUnhandledRejection() {},
        offUnhandledRejection() {},
        showModal: options => modals.push(options),
        restartMiniProgram: options => restarts.push(options),
        exitMiniProgram: options => exits.push(options),
        loadSubpackage(options) {
            loads.push(options.name);
            events.push('load:' + options.name);
            const outcome = outcomes.shift() ?? 'ok';
            let onProgress;
            setImmediate(() => {
                if (onProgress) onProgress({progress: 50});
                if (outcome === 'fail') options.fail({errMsg: 'network'});
                else if (outcome !== 'hang') options.success();
            });
            return {onProgressUpdate(fn) {onProgress = fn;}};
        },
    };
    if (unsupported) delete context.wx.loadSubpackage;
    vm.runInContext(template, context, {filename: 'game-inline.js'});
    return {context, events, loads, modals, restarts, exits, timers, mainCalls,
        fireTimers() {for (const timer of timers.splice(0)) if (!timer.cleared) timer.callback();}};
}

test('inline entry starts splash, overlaps assets with WASM/app, reports progress and disposes splash', async () => {
    const game = startInline();
    await settleAll();
    assert.deepEqual(game.loads, ['assets']);
    assert.equal(game.mainCalls.length, 1);
    assert.equal(game.modals.length, 0);
    assert.equal(game.events[0], 'start');
    assert.ok(game.events.indexOf('load:assets') < game.events.indexOf('req:app'));
    assert.deepEqual(game.events.slice(-2), ['redraw', 'end']);
    assert.ok(game.events.some(e => Array.isArray(e) && e[0] === 'progress' && e[1] === 'assets' && e[2] === 100));
});

test('inline entry retries failed assets subpackage and releases app gate only after recovery', async () => {
    const game = startInline({assetsOutcome: ['fail', 'ok']});
    let assetsGateResolved = false;
    game.context.__miniGameAssetsReady.then(() => { assetsGateResolved = true; });
    await settleAll();
    assert.equal(assetsGateResolved, false);
    assert.ok(!game.events.includes('end'));
    assert.equal(game.modals.length, 1);
    game.modals[0].complete({confirm: true});
    await settleAll();
    assert.deepEqual(game.loads, ['assets', 'assets']);
    assert.equal(game.mainCalls.length, 1);
    assert.equal(assetsGateResolved, true);
    assert.deepEqual(game.events.slice(-2), ['redraw', 'end']);
});

test('inline entry fails startup on freetype rejection or main error', async () => {
    const ft = startInline({freetypeReject: true});
    await settleAll();
    assert.equal(ft.mainCalls.length, 0);
    assert.equal(ft.modals.length, 1);
    ft.modals[0].complete({confirm: true});
    assert.equal(ft.restarts.length, 1);

    const mainErr = startInline({mainCallbackError: new Error('boom')});
    await settleAll();
    assert.equal(mainErr.modals.length, 1);
});
