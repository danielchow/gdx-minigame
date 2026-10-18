import {readFileSync} from 'node:fs';
import vm from 'node:vm';
import {createRequire} from 'node:module';
const {create} = createRequire(import.meta.url)('../../main/resources/templates/minigame/asset-packages.js');
import assert from 'node:assert/strict';
import test from 'node:test';

// game.js as MiniGameBackend generated it, with ${mainClassArgs} substituted.
const gameJs = new URL('../../../build/regression-probes/async-completion/minigame/game.js', import.meta.url);

const quiet = new Proxy({}, {get: () => () => {}});
const settle = () => new Promise(resolve => setImmediate(resolve));

/**
 * Runs game.js against a mocked wx. Each subpackage name maps to a list of outcomes, one per
 * wx.loadSubpackage call: 'fail', or a function run as the subpackage script before success.
 */
function startGame(outcomes, {mainError, mainCallbackError, packageIndex = '{}', unsupported = false, console = quiet} = {}) {
    const loads = [];
    const modals = [];
    const restarts = [];
    const exits = [];
    const timers = [];
    const rejectionListeners = [];
    const mainCalls = [];
    const context = vm.createContext({console, Date, Promise, Error,
        setTimeout(callback, delay) {timers.push({callback, delay}); return timers.length;},
        clearTimeout(id) {if (timers[id - 1]) timers[id - 1].cleared = true;}});
    vm.runInContext('var window = globalThis;', context);
    context.require = name => {
        if (name === './asset-packages') return {create};
        if (name.endsWith('.json')) throw new Error('WeChat cannot require JSON');
        return name.includes('adapter') ? {setup() {}} : {start() {}, redraw() {}};
    };
    const defaultScripts = {
        engine: () => {context.window.Gdx = {};},
        game: () => {context.__gameApp = {main(args, callback) {
            mainCalls.push(args);
            if (mainError) throw mainError;
            callback(mainCallbackError);
        }};},
        assets: () => {},
    };
    context.wx = {
        createCanvas: () => ({width: 1, height: 1}),
        getFileSystemManager: () => ({readFileSync(path) {if (path === 'assets/asset-packages.json') {if (packageIndex === null) throw new Error('missing index'); return packageIndex;} throw new Error('missing');}}),
        onUnhandledRejection: listener => rejectionListeners.push(listener),
        offUnhandledRejection() {},
        showModal: options => modals.push(options),
        restartMiniProgram: options => restarts.push(options),
        exitMiniProgram: options => exits.push(options),
        loadSubpackage(options) {
            loads.push(options.name);
            const outcome = (outcomes[options.name] ?? []).shift() ?? defaultScripts[options.name];
            setImmediate(() => {
                if (outcome === 'fail') return options.fail({errMsg: 'loadSubpackage:fail network'});
                if (outcome !== 'hang') {
                    outcome(context);
                    options.success();
                }
            });
            return {onProgressUpdate() {}};
        },
    };
    if (unsupported) delete context.wx.loadSubpackage;
    vm.runInContext(readFileSync(gameJs, 'utf8'), context, {filename: 'game.js'});
    return {context, loads, modals, restarts, exits, timers, mainCalls, rejectionListeners,
        fireTimers() {for (const timer of timers.splice(0)) if (!timer.cleared) timer.callback();}};
}

async function settleAll() {for (let i = 0; i < 10; i++) await settle();}

test('a failed subpackage download asks to retry, and retry loads it again', async () => {
    const game = startGame({assets: ['fail'], engine: ['fail']});
    await settleAll();
    assert.equal(game.mainCalls.length, 0);
    assert.equal(game.modals.length, 1, 'both failures share one dialog');
    assert.match(game.modals[0].content, /加载失败/);
    assert.equal(game.modals[0].confirmText, '重试');
    game.modals[0].complete({confirm: true});
    await settleAll();
    assert.deepEqual(game.loads.filter(n => n === 'assets').length, 2, 'assets is loaded again');
    assert.deepEqual(game.loads.filter(n => n === 'engine').length, 2, 'engine is loaded again');
    assert.equal(game.loads.filter(n => n === 'game').length, 1, 'a loaded subpackage is not reloaded');
    assert.equal(game.mainCalls.length, 1, 'the game starts after a successful retry');
    assert.equal(game.modals.length, 1);
    game.fireTimers();
    assert.equal(game.modals.length, 1, 'no timeout after a successful start');
});

test('a Gdx module that never arrives ends in a restart dialog', async () => {
    const game = startGame({engine: [() => {}]});
    await settleAll();
    assert.equal(game.modals.length, 0);
    assert(game.timers.some(t => !t.cleared), 'a startup watchdog is armed');
    game.fireTimers();
    assert.equal(game.modals.length, 1);
    assert.equal(game.restarts.length, 0);
    game.modals[0].complete({confirm: true});
    assert.equal(game.restarts.length, 1, 'retrying engine initialization restarts the mini game');
});

test('a never-completing download also ends in a dialog', async () => {
    const game = startGame({game: ['hang']});
    await settleAll();
    game.fireTimers();
    assert.equal(game.modals.length, 1);
});

for (const [name, options] of [['throws', {mainError: new Error('boom')}],
    ['reports an error', {mainCallbackError: new Error('boom')}]]) {
    test(`a main() that ${name} ends in a restart dialog`, async () => {
        const game = startGame({}, options);
        await settleAll();
        assert.equal(game.mainCalls.length, 1);
        assert.equal(game.modals.length, 1);
        game.modals[0].complete({confirm: true});
        assert.equal(game.restarts.length, 1);
    });
}

test('a missing game module ends in a restart dialog', async () => {
    const game = startGame({game: [() => {}]});
    await settleAll();
    assert.equal(game.modals.length, 1);
    game.modals[0].complete({confirm: true});
    assert.equal(game.restarts.length, 1);
});

test('a rejected FreeType initialization ends in a restart dialog', async () => {
    const game = startGame({engine: [context => {
        // A thenable rejects only once awaited, so Node does not report it as unhandled first.
        context.__freetypeReady = {then: (resolve, reject) => reject(new Error('freetype wasm failed'))};
        context.window.Gdx = {};
    }]});
    await settleAll();
    assert.equal(game.mainCalls.length, 0);
    assert.equal(game.modals.length, 1);
    game.modals[0].complete({confirm: true});
    assert.equal(game.restarts.length, 1);
});

test('a WASM instantiation rejection during startup ends in a restart dialog', async () => {
    const game = startGame({engine: [() => {}]});
    await settleAll();
    assert.equal(game.rejectionListeners.length, 1, 'startup listens for unhandled rejections');
    game.rejectionListeners[0]({reason: new Error('WebAssembly.instantiate failed')});
    assert.equal(game.modals.length, 1);
    game.modals[0].complete({confirm: true});
    assert.equal(game.restarts.length, 1);
});

test('a successful startup logs nothing', async () => {
    const calls = [];
    const console = new Proxy({}, {get: (target, method) => (...args) => calls.push([method, ...args])});
    const game = startGame({}, {console});
    await settleAll();
    assert.equal(game.mainCalls.length, 1);
    assert.deepEqual(calls, []);
});


test('startup reads the optional index through the filesystem, never require(JSON)', async () => {
    const game = startGame({}, {packageIndex: '{"theme/regal/cards.atlas":"regal"}'});
    await settleAll();
    assert.equal(game.mainCalls.length, 1);
    assert.equal(game.context.__miniGameAssetPackages.isMissing('theme/regal/cards.atlas'), true);
    assert.equal(game.context.__miniGameAssetPackages.isMissing('default.png'), false);
});
for (const packageIndex of [null, '{broken']) {
    test('a missing or invalid optional index fails startup rather than assuming common files: ' + packageIndex, async () => {
        const game = startGame({}, {packageIndex});
        await settleAll();
        assert.equal(game.mainCalls.length, 0);
        assert.equal(game.modals.length, 1);
    });
}
test('a host without subpackage support asks to update and exits, with no retry loop', async () => {
    const game = startGame({}, {unsupported: true});
    await settleAll();
    assert.equal(game.loads.length, 0);
    assert.equal(game.mainCalls.length, 0);
    assert.equal(game.modals.length, 1);
    assert.equal(game.modals[0].confirmText, '退出');
    game.modals[0].complete({confirm: true});
    assert.equal(game.exits.length, 1);
    assert.equal(game.restarts.length, 0);
    game.fireTimers();
    assert.equal(game.modals.length, 1);
});
