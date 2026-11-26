import {readFileSync} from 'node:fs';
import vm from 'node:vm';
import assert from 'node:assert/strict';
import test from 'node:test';
import {typedArrays} from './engine.mjs';

const app = new URL('../../../build/regression-probes/runtime-robustness/minigame/subpackages/game/app.js', import.meta.url);

/**
 * InnerAudioContext that, like WeChat's, adds a listener on every on*() call. Assigning src
 * makes WeChat load the audio again, so the mock counts assignments.
 */
class MockAudioContext {
    constructor(options) {
        this.options = options;
        this.srcAssignments = 0;
        this.ended = [];
        this.errors = [];
        this.state = 'idle';
        this.destroyed = false;
    }
    get src() {return this.currentSrc;}
    set src(value) {this.currentSrc = value; this.srcAssignments++;}
    onEnded(listener) {this.ended.push(listener);}
    offEnded(listener) {this.ended = listener ? this.ended.filter(l => l !== listener) : [];}
    onError(listener) {this.errors.push(listener);}
    offError(listener) {this.errors = listener ? this.errors.filter(l => l !== listener) : [];}
    play() {this.state = 'playing';}
    pause() {if (this.state === 'playing') this.state = 'paused';}
    stop() {this.state = 'stopped';}
    seek() {}
    destroy() {this.destroyed = true;}
    end() {
        this.state = 'stopped';
        for (const listener of [...this.ended]) listener();
    }
}

function noopProxy(values = {}) {
    return new Proxy(values, {get: (target, key) => key in target ? target[key] : () => null});
}

/**
 * `canvas` is the size wx.createCanvas() returned in game.js; `window` overrides wx.getWindowInfo()
 * at startup.
 */
function startProbe(platform = 'devtools', console = noopProxy(), {canvas: canvasSize, window = {}, globals = {}, wxOverrides = {}} = {}) {
    const frames = [];
    const listeners = {};
    const contexts = [];
    const modals = [];
    const restarts = [];
    const vibrations = [];
    const exits = [];
    const report = {events: []};
    const httpRequests = [];
    class XMLHttpRequest {
        static DONE = 4;
        constructor() {this.readyState = 0; httpRequests.push(this);}
        open() {}
        send() {}
        setRequestHeader() {}
        getAllResponseHeaders() {return '';}
        complete() {this.status = 200; this.responseText = '{}'; this.readyState = 4; this.onreadystatechange();}
    }
    const on = name => listener => (listeners[name] ??= []).push(listener);
    const gl = noopProxy({getParameter: () => 'WebGL 1.0 (probe)', getExtension: () => null,
        getSupportedExtensions: () => []});
    const windowInfo = {pixelRatio: 1, screenWidth: 100, screenHeight: 200, windowWidth: 100, windowHeight: 200,
        safeArea: null, calls: 0, ...window};
    const wx = noopProxy({
        getWindowInfo: () => {windowInfo.calls++; return {...windowInfo};},
        onWindowResize: on('windowResize'),
        getDeviceInfo: () => ({platform}),
        getSystemInfoSync: () => ({platform, pixelRatio: 1}),
        getFileSystemManager: () => noopProxy({readFileSync() {throw new Error('missing');}}),
        env: {USER_DATA_PATH: 'wxfile://usr'},
        getStorageSync: () => '',
        getStorageInfoSync: () => ({keys: []}),
        createInnerAudioContext(options) {
            const context = new MockAudioContext(options);
            contexts.push(context);
            return context;
        },
        onShow: on('show'), onHide: on('hide'),
        onWindowStateChange: on('windowState'),
        onAudioInterruptionBegin: on('interruptionBegin'), onAudioInterruptionEnd: on('interruptionEnd'),
        onTouchStart: on('touchstart'), onTouchMove: on('touchmove'),
        onTouchEnd: on('touchend'), onTouchCancel: on('touchcancel'),
        onKeyDown: on('keydown'), onKeyUp: on('keyup'), onKeyboardInput: on('keyboardinput'),
        onKeyboardHeightChange: on('keyboardHeightChange'),
        onKeyboardComplete: on('keyboardComplete'),
        showModal: options => modals.push(options),
        restartMiniProgram: options => restarts.push(options),
        exitMiniProgram: options => exits.push(options),
        vibrateShort: options => vibrations.push(['short', options?.type]),
        vibrateLong: () => vibrations.push(['long']),
        ...wxOverrides,
    });
    const canvas = {width: 100, height: 200, ...canvasSize, getContext: () => gl};
    const context = vm.createContext({exports: {}, console, performance, wx, canvas, XMLHttpRequest, __robustnessProbe: report,
        requestAnimationFrame(callback) {frames.push(callback); return frames.length;},
        cancelAnimationFrame() {}, setTimeout, clearTimeout, ...typedArrays, ...globals});
    vm.runInContext(readFileSync(app, 'utf8'), context, {timeout: 5000});
    let mainError;
    context.exports.main([], error => {mainError = error;});
    assert.equal(mainError, undefined);

    const fire = (name, event) => {for (const listener of listeners[name] ?? []) listener(event);};
    return {
        report, contexts, modals, restarts, vibrations, exits, listeners, fire, windowInfo, httpRequests,
        command: (name, arg = 0) => report.command(name, arg),
        /** Runs every frame queued so far; returns how many ran. */
        runFrames() {
            const queued = frames.splice(0);
            for (const frame of queued) frame(0);
            return queued.length;
        },
        pendingFrames: () => frames.length,
        touch: (name, identifier, clientX = 10, clientY = 20) =>
            fire(name, {changedTouches: [{identifier, clientX, clientY}]}),
        /** Values recorded for an event, in order. */
        values: name => report.events.filter(e => e.startsWith(name + ':')).map(e => Number(e.slice(name.length + 1))),
    };
}

const soundContexts = probe => probe.contexts.filter(c => c.src === 'assets/effect.mp3');
const eventCount = (probe, name) => probe.report.events.filter(e => e === name + ':0').length;
const musicContext = (probe, i) => probe.contexts.find(c => c.src === `assets/music${i}.mp3`);

test('pooled sound contexts keep one listener each over many plays, and pool and steal still work', () => {
    const probe = startProbe();
    probe.runFrames();
    for (let i = 0; i < 50; i++) {
        probe.command('playSound');
        const playing = soundContexts(probe).filter(c => c.state === 'playing');
        assert.equal(playing.length, 1);
        playing[0].end();
    }
    assert.equal(soundContexts(probe).length, 1, 'sequential plays reuse one pooled context');
    for (const context of soundContexts(probe)) {
        assert.equal(context.ended.length, 1, 'one onEnded listener per context');
        assert.equal(context.errors.length, 1, 'one onError listener per context');
    }

    const ids = [];
    for (let i = 0; i < 11; i++) ids.push(probe.command('playSound'));
    const sounds = soundContexts(probe);
    assert.equal(sounds.length, 10, 'the pool is capped at ten contexts');
    assert.equal(sounds.filter(c => c.state === 'playing').length, 10, 'the eleventh play steals one');
    // The first playback lost its context; stopping it must not stop the stealing playback.
    probe.command('stopSound', ids[0]);
    assert.equal(sounds.filter(c => c.state === 'playing').length, 10);
    // An ended context goes back to the pool and is reused before a new one is created.
    const first = sounds.find(c => c.state === 'playing');
    first.end();
    probe.command('playSound');
    assert.equal(soundContexts(probe).length, 10);
    assert.equal(first.state, 'playing');
    probe.command('stopAllSounds');
    assert.equal(soundContexts(probe).filter(c => c.state === 'playing').length, 0);
    for (const context of soundContexts(probe)) {
        assert.equal(context.ended.length, 1);
        assert.equal(context.errors.length, 1);
    }
});

for (const [suspend, unsuspend] of [['hide', 'show'], ['interruptionBegin', 'interruptionEnd']]) {
    test(`${suspend}/${unsuspend} pause and resume exactly what was playing`, () => {
        const probe = startProbe();
        probe.runFrames();
        probe.command('loopSound');
        const pausedId = probe.command('loopSound');
        probe.command('pauseSound', pausedId);
        probe.command('playMusic', 0);
        probe.command('playMusic', 1);
        probe.command('pauseMusic', 1);
        const [playing, paused] = soundContexts(probe);
        assert.deepEqual([playing.state, paused.state], ['playing', 'paused']);

        probe.fire(suspend);
        assert.equal(playing.state, 'paused', 'a playing sound is paused');
        assert.equal(musicContext(probe, 0).state, 'paused', 'playing music is paused');
        assert.equal(probe.command('musicPlaying', 0), 1, 'the game still sees its music as playing');

        probe.fire(unsuspend, {});
        assert.equal(playing.state, 'playing');
        assert.equal(paused.state, 'paused', 'a sound the game paused stays paused');
        assert.equal(musicContext(probe, 0).state, 'playing');
        assert.equal(musicContext(probe, 1).state, 'paused', 'music the game paused stays paused');
        assert.equal(musicContext(probe, 2).state, 'idle', 'music never played is not started');
    });
}

test('overlapping hide and interruption resume once both are over, or on show', () => {
    const probe = startProbe();
    probe.runFrames();
    probe.command('playMusic', 0);
    probe.fire('interruptionBegin');
    probe.fire('hide');
    probe.fire('interruptionEnd');
    assert.equal(musicContext(probe, 0).state, 'paused', 'still hidden');
    probe.fire('show', {});
    assert.equal(musicContext(probe, 0).state, 'playing');

    // A missed interruption end must not silence a visible game for good.
    probe.fire('hide');
    probe.fire('interruptionBegin');
    probe.fire('show', {});
    assert.equal(musicContext(probe, 0).state, 'playing');
    // Music started while suspended waits for the end of the suspension.
    probe.command('pauseMusic', 0);
    probe.fire('interruptionBegin');
    probe.command('playMusic', 0);
    assert.equal(musicContext(probe, 0).state, 'paused');
    probe.fire('interruptionEnd');
    assert.equal(musicContext(probe, 0).state, 'playing');
});

test('a minimized PC window pauses and resumes once, even when WeChat also reports hide and show', () => {
    const probe = startProbe('windows');
    probe.runFrames();
    probe.command('playMusic', 0);
    probe.fire('windowState', {state: 'minimize'});
    assert.equal(musicContext(probe, 0).state, 'paused', 'minimizing does not fire onHide');
    probe.fire('hide');
    assert.equal(eventCount(probe, 'pause'), 1, 'a later onHide does not pause again');
    probe.fire('windowState', {state: 'normalize'});
    probe.fire('show', {});
    assert.equal(eventCount(probe, 'resume'), 1);
    assert.equal(musicContext(probe, 0).state, 'playing');

    probe.fire('windowState', {state: 'maximize'});
    assert.equal(eventCount(probe, 'resume'), 1, 'maximizing a visible window resumes nothing');
    probe.fire('hide');
    probe.fire('windowState', {state: 'maximize'});
    assert.deepEqual([eventCount(probe, 'pause'), eventCount(probe, 'resume')], [2, 2]);
});

test('a cancelled touch reaches touchCancelled, not touchUp, and clears the pointer', () => {
    const probe = startProbe();
    probe.runFrames();
    probe.touch('touchstart', 7);
    assert.equal(probe.command('isTouched'), 1);
    probe.touch('touchcancel', 7);
    assert.equal(probe.command('isTouched'), 0);
    probe.touch('touchstart', 8);
    probe.touch('touchend', 8);
    const touches = probe.report.events.filter(e => e.startsWith('touch'));
    assert.deepEqual(touches, ['touchDown:0', 'touchCancelled:0', 'touchDown:0', 'touchUp:0']);
});

test('a throwing render reports once, stops the loop and offers a restart', () => {
    const probe = startProbe();
    for (let i = 0; i < 3; i++) assert.equal(probe.runFrames(), 1);
    probe.command('throwOnRender');
    probe.runFrames();
    assert.equal(probe.pendingFrames(), 0, 'no frame runs after a fatal error');
    const count = name => probe.report.events.filter(e => e === name + ':0').length;
    assert.equal(count('onError'), 1);
    assert.equal(count('onFatalError'), 1);
    assert.equal(probe.modals.length, 1, 'the default fatal handler shows a dialog');
    assert.equal(probe.restarts.length, 0);
    probe.modals[0].complete({confirm: true});
    assert.equal(probe.restarts.length, 1, 'confirming the dialog restarts the mini game');
});

test('the full-screen canvas cannot be resized through setWindowedMode', () => {
    const probe = startProbe();
    probe.runFrames();
    const width = probe.command('graphicsWidth');
    assert.equal(probe.command('supportsDisplayModeChange'), 0);
    assert.equal(probe.command('setWindowedMode', Math.floor(width / 2)), 0, 'setWindowedMode reports no change');
    probe.runFrames();
    assert.equal(probe.command('graphicsWidth'), width, 'the canvas keeps its size');
});

test('the canvas fills the window at launch even when wx.createCanvas() caught a squashed one', () => {
    // Desktop WeChat can still be opening its window when game.js creates the canvas. WeChat then
    // stretched that canvas over the final window, squashing the whole game (solitaire#9).
    const probe = startProbe('mac', noopProxy(),
        {canvas: {width: 800, height: 120}, window: {pixelRatio: 2, windowWidth: 800, windowHeight: 450}});
    probe.runFrames();
    assert.deepEqual([probe.command('graphicsWidth'), probe.command('graphicsHeight')], [1600, 900]);
    assert.deepEqual([probe.values('resizeWidth'), probe.values('resizeHeight')], [[1600], [900]]);
    probe.touch('touchstart', 1, 400, 225);
    assert.deepEqual([probe.values('pointerX'), probe.values('pointerY')], [[800], [450]],
        'touches map from logical window pixels to canvas pixels');
});

test('a window resize resizes the canvas, the app and touch mapping', () => {
    const probe = startProbe('mac', noopProxy(), {window: {pixelRatio: 2}});
    probe.runFrames();
    assert.deepEqual([probe.command('graphicsWidth'), probe.command('graphicsHeight')], [200, 400]);
    Object.assign(probe.windowInfo, {windowWidth: 300, windowHeight: 150});
    probe.fire('windowResize', {windowWidth: 300, windowHeight: 150});
    probe.runFrames();
    assert.deepEqual([probe.values('resizeWidth'), probe.values('resizeHeight')], [[200, 600], [400, 300]]);
    probe.touch('touchstart', 1, 150, 75);
    assert.deepEqual([probe.values('pointerX'), probe.values('pointerY')], [[300], [150]]);
    probe.fire('windowResize', {windowWidth: 300, windowHeight: 150});
    probe.runFrames();
    assert.deepEqual(probe.values('resizeWidth'), [200, 600], 'an unchanged size does not resize again');
});

test('a resize event\'s size wins over a getWindowInfo() that still reports the old window', () => {
    const probe = startProbe('mac', noopProxy(), {window: {pixelRatio: 2}});
    probe.runFrames();
    probe.fire('windowResize', {windowWidth: 400, windowHeight: 225});
    probe.runFrames();
    assert.deepEqual([probe.command('graphicsWidth'), probe.command('graphicsHeight')], [800, 450]);
    probe.touch('touchstart', 1, 400, 225);
    assert.deepEqual([probe.values('pointerX'), probe.values('pointerY')], [[800], [450]]);
});

test('density and safe insets are cached and refreshed on window resize', () => {
    const probe = startProbe();
    probe.runFrames();
    const calls = probe.windowInfo.calls;
    // Layout code reads these per widget; each read must not be a synchronous wx.getWindowInfo().
    probe.command('readWindowMetrics', 500);
    assert.equal(probe.windowInfo.calls, calls, 'repeated reads make no native calls');
    assert.equal(probe.command('safeInset', 0), 0, 'no safe area means no insets');

    Object.assign(probe.windowInfo, {pixelRatio: 3, safeArea: {top: 44, left: 0, bottom: 166, right: 100}});
    probe.fire('windowResize', {});
    assert.deepEqual([0, 1, 2, 3].map(side => probe.command('safeInset', side)), [132, 0, 102, 0],
        'insets in physical pixels: top 44*3, left 0, bottom (200-166)*3, right 0');
    assert.equal(probe.command('densityTimes100'), 300);
});

test('platform flags follow WeChat\'s lowercase platform names', () => {
    const flags = probe => ({android: probe.command('uiutils', 0), ios: probe.command('uiutils', 1),
        osX: probe.command('uiutils', 2)});
    const android = startProbe('android');
    assert.deepEqual(flags(android), {android: 1, ios: 0, osX: 0});
    const ios = startProbe('ios');
    assert.deepEqual(flags(ios), {android: 0, ios: 1, osX: 1});
});

test('startup, frames, sounds and texture uploads log no progress or timing', () => {
    const calls = [];
    const probe = startProbe('devtools', new Proxy({}, {get: (target, method) => (...args) => calls.push(method)}));
    for (let i = 0; i < 3; i++) probe.runFrames();
    probe.command('playSound');
    probe.command('texImage2D');
    probe.runFrames();
    // TeaVM prints System.out as console.info; warnings and errors stay allowed.
    assert.deepEqual(calls.filter(method => ['log', 'info', 'debug'].includes(method)), []);
});

test('sound contexts use the WebAudio implementation and music keeps the default', () => {
    const probe = startProbe();
    probe.command('playSound');
    probe.command('playMusic', 0);
    assert.equal(soundContexts(probe)[0].options?.useWebAudioImplement, true);
    assert.equal(musicContext(probe, 0).options?.useWebAudioImplement, undefined);
});

test('a reused sound context is not given its source again', () => {
    const probe = startProbe();
    for (let i = 0; i < 5; i++) {
        probe.command('playSound');
        soundContexts(probe)[0].end();
    }
    const [context] = soundContexts(probe);
    assert.equal(soundContexts(probe).length, 1);
    assert.equal(context.srcAssignments, 1, 'replaying the same sound does not reload it');
    assert.equal(context.state, 'stopped');

    probe.command('playOtherSound');
    assert.equal(context.src, 'assets/other.mp3', 'another sound still switches the source');
    assert.equal(context.state, 'playing');
    context.end();
    probe.command('playSound');
    assert.equal(context.src, 'assets/effect.mp3');
    assert.equal(context.srcAssignments, 3);

    // After a load error the source is set again, so the next play retries the load.
    for (const listener of [...context.errors]) listener({errMsg: 'decode failed'});
    assert.equal(soundContexts(probe).filter(c => c.state === 'playing').length, 0);
    probe.command('playSound');
    assert.equal(context.srcAssignments, 4);
    assert.equal(context.state, 'playing');
});

test('vibrate picks the short or long vibration by duration, and maps vibration types', () => {
    const probe = startProbe();
    for (const ms of [1, 50, 199, 200, 400, 1000]) probe.command('vibrate', ms);
    for (const type of [0, 1, 2]) probe.command('vibrateType', type);
    assert.deepEqual(probe.vibrations, [
        ['short', 'medium'], ['short', 'medium'], ['short', 'medium'], ['long'], ['long'], ['long'],
        ['short', 'light'], ['short', 'medium'], ['short', 'heavy'],
    ]);
});

test('getTextInput cancels, since WeChat has no text input dialog', () => {
    const probe = startProbe();
    probe.runFrames();
    probe.command('textInput', 0);
    probe.command('textInput', 1);
    probe.runFrames();
    assert.equal(eventCount(probe, 'textCanceled'), 2);
    assert.equal(eventCount(probe, 'textInput'), 0);
});

test('exit leaves the mini game', () => {
    const probe = startProbe();
    probe.command('exit');
    assert.equal(probe.exits.length, 1);
});

test('density matches the pixel ratio, as on Android, and ppi follows it', () => {
    const probe = startProbe();
    probe.windowInfo.pixelRatio = 3;
    probe.fire('windowResize', {});
    assert.equal(probe.command('graphicsDensityTimes100'), 300);
    assert.equal(probe.command('ppiX'), 480);
    assert.equal(probe.command('ppiY'), 480);
});


test('on-demand requests coalesce, render next RAF, and stop scheduling when idle', () => {
    const p = startProbe();
    assert.equal(p.command('isContinuous'), 1, 'continuous is the compatibility default');
    p.runFrames();
    assert.equal(p.pendingFrames(), 1);
    p.command('continuous', 0);
    const before = eventCount(p, 'render');
    p.runFrames(); // the already-queued continuous callback is allowed to drain without drawing
    assert.equal(eventCount(p, 'render'), before);
    assert.equal(p.pendingFrames(), 0);
    for (let i = 0; i < 20; i++) p.command('requestFrame');
    assert.equal(eventCount(p, 'render'), before, 'request never renders synchronously');
    assert.equal(p.pendingFrames(), 1);
    p.runFrames();
    assert.equal(eventCount(p, 'render'), before + 1);
    assert.equal(p.pendingFrames(), 0);
    p.command('requestInRender');
    p.runFrames();
    assert.equal(p.pendingFrames(), 1, 'request during render survives consumption');
    p.runFrames();
    assert.equal(p.pendingFrames(), 0);
    p.command('continuous', 1);
    p.command('requestFrame');
    p.runFrames();
    assert.equal(p.pendingFrames(), 1, 'restoring continuous starts only one chain');
});

test('input, runnable, nested runnable, resize and JS timer wake a dormant loop', async () => {
    const p = startProbe();
    p.runFrames(); p.command('continuous', 0); p.runFrames();
    for (const event of ['touchstart', 'touchmove', 'touchend', 'touchcancel']) {
        p.touch(event, 1);
        assert.equal(p.pendingFrames(), 1, event);
        p.runFrames();
        assert.equal(p.pendingFrames(), 0);
    }
    for (const event of ['keydown', 'keyup', 'keyboardinput']) {
        p.fire(event, {});
        assert.equal(p.pendingFrames(), 1, event);
        p.runFrames();
    }
    p.command('postRunnable', 1);
    assert.equal(eventCount(p, 'runnable'), 0);
    p.runFrames();
    assert.equal(eventCount(p, 'runnable'), 1);
    assert.equal(eventCount(p, 'nestedRunnable'), 0);
    assert.equal(p.pendingFrames(), 1);
    p.runFrames();
    assert.equal(eventCount(p, 'nestedRunnable'), 1);
    assert.equal(p.pendingFrames(), 0);
    p.fire('windowResize', {windowWidth: 300, windowHeight: 400});
    assert.equal(p.pendingFrames(), 1);
    p.runFrames();
    assert.equal(p.values('resizeWidth').at(-1), 300);
    await new Promise(resolve => setTimeout(() => {p.command('requestFrame'); resolve();}, 1));
    assert.equal(p.pendingFrames(), 1, 'setTimeout may safely request the next RAF');
    p.runFrames();
    assert.equal(p.pendingFrames(), 0);
});

test('hide suppresses queued callbacks and runnables, show resumes exactly one chain', () => {
    const p = startProbe();
    p.runFrames();
    p.fire('hide');
    const before = eventCount(p, 'render');
    p.command('postRunnable');
    p.runFrames(); // mock intentionally delivers even a cancelled RAF
    assert.equal(eventCount(p, 'render'), before);
    assert.equal(eventCount(p, 'runnable'), 0);
    assert.equal(p.pendingFrames(), 0);
    p.fire('show', {}); p.fire('show', {});
    assert.equal(p.pendingFrames(), 1);
    p.runFrames();
    assert.equal(eventCount(p, 'runnable'), 1);
    assert.equal(eventCount(p, 'render'), before + 1);
    assert.equal(p.pendingFrames(), 1);
});


test('async asset completion wakes an idle loop without polling', async () => {
    const p = startProbe();
    p.runFrames(); p.command('continuous', 0); p.runFrames();
    p.command('asyncCompletion');
    assert.equal(p.pendingFrames(), 0);
    await new Promise(resolve => setTimeout(resolve, 10));
    assert.equal(eventCount(p, 'async'), 1);
    assert.equal(p.pendingFrames(), 1);
    p.runFrames();
    assert.equal(p.pendingFrames(), 0);
});


test('HTTP completion wakes a dormant loop and hide/show rejects stale RAF delivery', () => {
    const p = startProbe();
    p.runFrames(); p.command('continuous', 0); p.runFrames();
    p.command('httpRequest');
    assert.equal(p.pendingFrames(), 0);
    p.httpRequests[0].complete();
    assert.deepEqual(p.values('http'), [200]);
    assert.equal(p.pendingFrames(), 1);
    p.fire('hide'); p.fire('show', {});
    const before = eventCount(p, 'render');
    p.runFrames(); // delivers both old cancelled and new callbacks
    assert.equal(eventCount(p, 'render'), before + 1);
    assert.equal(p.pendingFrames(), 0);
});


test('runnables posted before first RAF survive initialization and run after create', () => {
    const p = startProbe();
    p.command('continuous', 0);
    p.command('postRunnable', 1);
    assert.equal(p.pendingFrames(), 1);
    assert.equal(eventCount(p, 'runnable'), 0);
    p.runFrames();
    assert.equal(eventCount(p, 'create'), 1);
    assert.equal(eventCount(p, 'runnable'), 1);
    assert(p.report.events.indexOf('create:0') < p.report.events.indexOf('runnable:0'));
    assert.equal(eventCount(p, 'nestedRunnable'), 0);
    assert.equal(p.pendingFrames(), 1);
    p.runFrames();
    assert.equal(eventCount(p, 'runnable'), 1);
    assert.equal(eventCount(p, 'nestedRunnable'), 1);
    assert.equal(p.pendingFrames(), 0);
});


test('music completion and error wake a dormant loop', () => {
    const p = startProbe();
    p.runFrames(); p.command('continuous', 0); p.runFrames();
    p.command('musicCompletion', 0);
    p.command('playMusic', 0);
    musicContext(p, 0).end();
    assert.equal(eventCount(p, 'musicComplete'), 1);
    assert.equal(p.command('musicPlaying', 0), 0);
    assert.equal(p.pendingFrames(), 1);
    p.runFrames();
    p.command('playMusic', 0);
    musicContext(p, 0).errors[0]();
    assert.equal(p.command('musicPlaying', 0), 0);
    assert.equal(p.pendingFrames(), 1);
    p.runFrames();
    assert.equal(p.pendingFrames(), 0);
});

test('assets-ready gate defers first frame until resolved and ignores premature hide/show', async () => {
    let releaseAssets;
    const gate = new Promise(resolve => { releaseAssets = resolve; });
    const p = startProbe('devtools', noopProxy(), {globals: {__miniGameAssetsReady: gate}});
    assert.equal(p.pendingFrames(), 0, 'must not schedule a frame before assets mount');
    p.fire('hide');
    p.fire('show', {});
    assert.equal(p.pendingFrames(), 0, 'show before assets mount must not start the loop');
    releaseAssets();
    await gate;
    await Promise.resolve();
    assert.equal(p.pendingFrames(), 1, 'frame schedules once assets gate resolves');
    p.runFrames();
    assert.equal(eventCount(p, 'create'), 1);
});

test('failure inside completeAssetInit shows the fatal restart modal instead of hanging', async () => {
    let releaseAssets;
    const gate = new Promise(resolve => { releaseAssets = resolve; });
    const p = startProbe('devtools', noopProxy(), {
        globals: {__miniGameAssetsReady: gate},
        wxOverrides: {getFileSystemManager() { throw new Error('fs broken'); }},
    });
    assert.equal(p.modals.length, 0);
    releaseAssets();
    await gate;
    await Promise.resolve();
    await Promise.resolve();
    assert.equal(p.modals.length, 1, 'completeAssetInit failure must trigger fatal modal');
    assert.equal(p.pendingFrames(), 0);
});

function createFakeClock(startMs = 10_000, {noopClear = false} = {}) {
    let now = startMs;
    let nextId = 0;
    const timers = new Map();
    const scheduled = [];
    class FakeDate extends Date {
        static now() { return now; }
    }
    return {
        globals: {
            Date: FakeDate,
            setTimeout(fn, delay = 0) {
                const id = nextId++; // intentionally starts at 0 to test falsy numeric timer IDs
                const entry = {id, fn, delay, dueAt: now + delay};
                timers.set(id, entry);
                scheduled.push(entry);
                return id;
            },
            clearTimeout(id) {
                if (!noopClear) timers.delete(id);
            },
        },
        now: () => now,
        setNow(value) { now = value; },
        advance(ms) { now += ms; },
        pendingCount: () => timers.size,
        scheduled,
        flushDue() {
            let ran = 0;
            while (true) {
                const due = [...timers.values()].filter(t => t.dueAt <= now).sort((a, b) => a.dueAt - b.dueAt);
                if (due.length === 0) break;
                const next = due[0];
                timers.delete(next.id);
                next.fn();
                ran++;
            }
            return ran;
        },
        runAll() {
            let ran = 0;
            while (timers.size > 0) {
                const next = [...timers.values()].sort((a, b) => a.dueAt - b.dueAt)[0];
                timers.delete(next.id);
                next.fn();
                ran++;
            }
            return ran;
        },
    };
}

test('mobile keyboard height-only window resizes are ignored while active and settling, but rotations and desktop still resize', () => {
    const clock = createFakeClock(10_000);
    const android = startProbe('android', noopProxy(), {
        canvas: {width: 1080, height: 2400},
        window: {pixelRatio: 3, screenWidth: 360, screenHeight: 800, windowWidth: 360, windowHeight: 800},
        globals: clock.globals,
    });
    android.runFrames();
    assert.deepEqual([android.command('graphicsWidth'), android.command('graphicsHeight')], [1080, 2400]);
    assert.deepEqual([android.values('resizeWidth'), android.values('resizeHeight')], [[1080], [2400]]);

    // Opening keyboard triggers setKeyboardActive(true) before wx.showKeyboard, then Android fires
    // a transient height-only onWindowResize before onKeyboardHeightChange arrives.
    android.command('continuous', 0);
    android.runFrames();
    assert.equal(android.pendingFrames(), 0);
    android.command('setKeyboardActive', 1);
    android.fire('windowResize', {windowWidth: 360, windowHeight: 781});
    assert.equal(android.pendingFrames(), 0, 'ignored keyboard resize must not wake a dormant loop');
    assert.equal(clock.pendingCount(), 0, 'no reconcile timer while keyboard is still active');
    android.runFrames();
    assert.deepEqual([android.command('graphicsWidth'), android.command('graphicsHeight')], [1080, 2400]);
    assert.deepEqual([android.values('resizeWidth'), android.values('resizeHeight')], [[1080], [2400]],
        'transient keyboard-open height-only resize must not resize canvas or app');
    android.command('continuous', 1);

    // Keyboard opens, then closes; trailing onWindowResize within 600ms settle window is also ignored.
    android.fire('keyboardHeightChange', {height: 260});
    clock.advance(2000);
    android.fire('windowResize', {windowWidth: 360, windowHeight: 781});
    android.runFrames();
    assert.deepEqual(android.values('resizeHeight'), [2400], 'height-only resize while keyboard is open is ignored');

    android.fire('keyboardHeightChange', {height: 0});
    android.fire('keyboardComplete', {});
    assert.equal(clock.pendingCount(), 1, 'closing keyboard schedules one settle reconcile timer');
    clock.advance(300);
    android.fire('windowResize', {windowWidth: 360, windowHeight: 781});
    android.runFrames();
    assert.deepEqual(android.values('resizeHeight'), [2400], 'trailing resize inside 600ms settle window is ignored');

    // After the 600ms settle window, a genuine non-keyboard height resize (e.g. split-screen) still applies
    // and cancels the pending reconcile timer.
    clock.advance(400);
    Object.assign(android.windowInfo, {windowHeight: 500});
    android.fire('windowResize', {windowWidth: 360, windowHeight: 500});
    assert.equal(clock.pendingCount(), 0, 'applied resize cancels deferred reconcile timer');
    android.runFrames();
    assert.deepEqual([android.command('graphicsWidth'), android.command('graphicsHeight')], [1080, 1500]);
    assert.deepEqual(android.values('resizeHeight'), [2400, 1500]);

    // Closing via setKeyboardActive(0) (e.g. onPause/Cancel) without a trailing keyboardHeightChange(0)
    // or keyboardComplete must clear keyboardHeightOpen so future height-only resizes are not permanently latched.
    android.command('setKeyboardActive', 1);
    android.fire('keyboardHeightChange', {height: 260});
    android.command('setKeyboardActive', 0);
    clock.advance(700);
    Object.assign(android.windowInfo, {windowHeight: 800});
    android.fire('windowResize', {windowWidth: 360, windowHeight: 800});
    android.runFrames();
    assert.deepEqual(android.values('resizeHeight'), [2400, 1500, 2400],
        'setKeyboardActive(false) clears latched keyboardHeightOpen after settle window');

    // Also test wx.onHide clearing latched keyboardHeightOpen cleanly.
    android.command('setKeyboardActive', 1);
    android.fire('keyboardHeightChange', {height: 260});
    android.fire('hide');
    android.fire('show', {});
    clock.advance(700);
    Object.assign(android.windowInfo, {windowHeight: 500});
    android.fire('windowResize', {windowWidth: 360, windowHeight: 500});
    android.runFrames();
    assert.deepEqual(android.values('resizeHeight'), [2400, 1500, 2400, 1500],
        'hide() clears latched keyboardHeightOpen so post-resume split-screen resizes apply');

    // pixelRatio change while keyboard is open still applies immediately.
    android.command('setKeyboardActive', 1);
    android.fire('keyboardHeightChange', {height: 200});
    Object.assign(android.windowInfo, {pixelRatio: 2, windowHeight: 500});
    android.fire('windowResize', {windowWidth: 360, windowHeight: 500});
    android.runFrames();
    assert.deepEqual([android.command('graphicsWidth'), android.command('graphicsHeight')], [720, 1000]);

    // Rotation (width change) while keyboard is open must still resize immediately.
    Object.assign(android.windowInfo, {pixelRatio: 3, screenWidth: 800, screenHeight: 360, windowWidth: 800, windowHeight: 360});
    android.fire('windowResize', {windowWidth: 800, windowHeight: 360});
    android.runFrames();
    assert.deepEqual([android.command('graphicsWidth'), android.command('graphicsHeight')], [2400, 1080]);
    assert.deepEqual(android.values('resizeWidth'), [1080, 1080, 1080, 1080, 720, 2400]);
    assert.equal(clock.pendingCount(), 0, 'no leaked reconcile timers');

    // Desktop WeChat height-only window resizing is never suppressed.
    const mac = startProbe('mac', noopProxy(), {
        canvas: {width: 800, height: 1200},
        window: {pixelRatio: 2, screenWidth: 1440, screenHeight: 900, windowWidth: 400, windowHeight: 600},
        globals: clock.globals,
    });
    mac.runFrames();
    mac.command('setKeyboardActive', 1);
    mac.fire('windowResize', {windowWidth: 400, windowHeight: 500});
    mac.runFrames();
    assert.deepEqual([mac.command('graphicsWidth'), mac.command('graphicsHeight')], [800, 1000]);
    assert.deepEqual(mac.values('resizeHeight'), [1200, 1000]);
    assert.equal(clock.pendingCount(), 0);
});

test('deferred mobile keyboard resize reconciles after settle without spurious wakeups, handles hide/show, clock jump, and stale timers', () => {
    const clock = createFakeClock(20_000);
    const android = startProbe('android', noopProxy(), {
        canvas: {width: 1080, height: 2400},
        window: {pixelRatio: 3, screenWidth: 360, screenHeight: 800, windowWidth: 360, windowHeight: 800},
        globals: clock.globals,
    });
    android.runFrames();
    android.command('continuous', 0);
    android.runFrames();
    assert.equal(android.pendingFrames(), 0);

    // 1. Normal keyboard open -> transient shrink -> close -> restore: reconcile sees unchanged
    // windowInfo (including safeArea == null) and does NOT wake the dormant loop or resize.
    android.command('setKeyboardActive', 1);
    android.fire('keyboardHeightChange', {height: 0}); // pre-open height=0 must not schedule early
    assert.equal(clock.pendingCount(), 0);
    android.fire('windowResize', {windowWidth: 360, windowHeight: 781});
    android.fire('keyboardHeightChange', {height: 260});
    android.fire('keyboardHeightChange', {height: 0});
    android.fire('keyboardComplete', {});
    assert.equal(clock.pendingCount(), 1);
    clock.advance(650);
    assert.equal(clock.flushDue(), 1);
    assert.equal(clock.pendingCount(), 0);
    assert.equal(android.pendingFrames(), 0, 'no-op reconcile must not wake dormant loop even with safeArea=null');
    assert.deepEqual(android.values('resizeHeight'), [2400]);

    // 2. Real portrait split-screen while keyboard is open (no further windowResize after close):
    // deferred resize reconciles automatically once keyboard closes and settles.
    android.command('setKeyboardActive', 1);
    android.fire('keyboardHeightChange', {height: 260});
    Object.assign(android.windowInfo, {windowHeight: 400});
    android.fire('windowResize', {windowWidth: 360, windowHeight: 400});
    assert.deepEqual(android.values('resizeHeight'), [2400], 'deferred while keyboard open');
    android.fire('keyboardComplete', {});
    assert.equal(clock.pendingCount(), 1);
    // Reopening keyboard before timer fires cancels timer while preserving deferredResizePending.
    clock.advance(200);
    android.command('setKeyboardActive', 1);
    assert.equal(clock.pendingCount(), 0, 'reopening keyboard cancels armed reconcile timer');
    android.command('setKeyboardActive', 0);
    assert.equal(clock.pendingCount(), 1, 'closing reopened keyboard re-arms reconcile timer');
    clock.advance(650);
    assert.equal(clock.flushDue(), 1);
    assert.equal(android.pendingFrames(), 1, 'changed reconcile wakes dormant loop');
    android.runFrames();
    assert.deepEqual([android.command('graphicsWidth'), android.command('graphicsHeight')], [1080, 1200]);
    assert.deepEqual(android.values('resizeHeight'), [2400, 1200]);

    // 3. Height-only resize arriving inside the 600ms post-close settle window schedules reconcile directly.
    android.command('setKeyboardActive', 1);
    android.command('setKeyboardActive', 0);
    assert.equal(clock.pendingCount(), 0, 'no timer scheduled when nothing was deferred');
    clock.advance(200);
    Object.assign(android.windowInfo, {windowHeight: 800});
    android.fire('windowResize', {windowWidth: 360, windowHeight: 800});
    assert.equal(clock.pendingCount(), 1, 'ignored settle-window resize arms reconcile immediately');
    assert.equal(clock.scheduled.at(-1).delay, 450, 'delay targets remaining 400ms settle + 50ms margin');
    clock.advance(450);
    assert.equal(clock.flushDue(), 1);
    android.runFrames();
    assert.deepEqual(android.values('resizeHeight'), [2400, 1200, 2400]);

    // 4. hide() cancels pending timer without losing deferredResizePending; show() reconciles after settle.
    android.command('setKeyboardActive', 1);
    android.fire('keyboardHeightChange', {height: 260});
    Object.assign(android.windowInfo, {windowHeight: 500});
    android.fire('windowResize', {windowWidth: 360, windowHeight: 500});
    android.fire('hide');
    assert.equal(clock.pendingCount(), 0, 'hide() must not leave a background reconcile timer armed');
    clock.advance(700);
    android.fire('show', {});
    assert.equal(clock.pendingCount(), 0, 'show() after settle reconciles immediately');
    assert.equal(android.command('graphicsHeight'), 1500, 'reconciled before resume/frame');
    android.runFrames();
    assert.deepEqual(android.values('resizeHeight'), [2400, 1200, 2400, 1500]);

    // 5. Backward Date.now() clock jump (>24.85 days) never latches settle window or overflows delayMs.
    android.command('setKeyboardActive', 1);
    android.command('setKeyboardActive', 0);
    clock.setNow(clock.now() - 30 * 24 * 60 * 60 * 1000);
    Object.assign(android.windowInfo, {windowHeight: 800});
    android.fire('windowResize', {windowWidth: 360, windowHeight: 800});
    android.runFrames();
    assert.deepEqual(android.values('resizeHeight'), [2400, 1200, 2400, 1500, 2400],
        'backward clock jump must treat settle window as expired instead of suppressing indefinitely');
    assert.equal(clock.pendingCount(), 0);

    // 6. Stale timer callback (when host clearTimeout races delivery) is rejected by reconcileGeneration.
    const racingClock = createFakeClock(50_000, {noopClear: true});
    const racing = startProbe('android', noopProxy(), {
        canvas: {width: 1080, height: 2400},
        window: {pixelRatio: 3, screenWidth: 360, screenHeight: 800, windowWidth: 360, windowHeight: 800},
        globals: racingClock.globals,
    });
    racing.runFrames();
    racing.command('setKeyboardActive', 1);
    Object.assign(racing.windowInfo, {windowHeight: 400});
    racing.fire('windowResize', {windowWidth: 360, windowHeight: 400});
    racing.command('setKeyboardActive', 0); // arms timer #0
    racing.command('setKeyboardActive', 1); // cancels timer #0 (bumped generation), keyboard open again
    racingClock.advance(700);
    assert.equal(racingClock.flushDue(), 1, 'stale timer #0 callback delivers');
    assert.equal(racing.command('graphicsHeight'), 2400, 'stale timer callback must not reconcile while reopened');
    racing.command('setKeyboardActive', 0); // arms timer #1
    racingClock.advance(650);
    assert.equal(racingClock.flushDue(), 1);
    assert.equal(racing.command('graphicsHeight'), 1200, 'fresh timer #1 reconciles cleanly');
});
