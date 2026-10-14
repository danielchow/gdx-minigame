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

function startProbe(platform = 'devtools', console = noopProxy()) {
    const frames = [];
    const listeners = {};
    const contexts = [];
    const modals = [];
    const restarts = [];
    const vibrations = [];
    const exits = [];
    const report = {events: []};
    const on = name => listener => (listeners[name] ??= []).push(listener);
    const gl = noopProxy({getParameter: () => 'WebGL 1.0 (probe)', getExtension: () => null,
        getSupportedExtensions: () => []});
    const windowInfo = {pixelRatio: 1, screenWidth: 100, screenHeight: 200, safeArea: null, calls: 0};
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
        showModal: options => modals.push(options),
        restartMiniProgram: options => restarts.push(options),
        exitMiniProgram: options => exits.push(options),
        vibrateShort: options => vibrations.push(['short', options?.type]),
        vibrateLong: () => vibrations.push(['long']),
    });
    const canvas = {width: 100, height: 200, getContext: () => gl};
    const context = vm.createContext({exports: {}, console, performance, wx, canvas, __robustnessProbe: report,
        requestAnimationFrame(callback) {frames.push(callback); return frames.length;},
        cancelAnimationFrame() {}, setTimeout, clearTimeout, ...typedArrays});
    vm.runInContext(readFileSync(app, 'utf8'), context, {timeout: 5000});
    let mainError;
    context.exports.main([], error => {mainError = error;});
    assert.equal(mainError, undefined);

    const fire = (name, event) => {for (const listener of listeners[name] ?? []) listener(event);};
    return {
        report, contexts, modals, restarts, vibrations, exits, listeners, fire, windowInfo,
        command: (name, arg = 0) => report.command(name, arg),
        /** Runs every frame queued so far; returns how many ran. */
        runFrames() {
            const queued = frames.splice(0);
            for (const frame of queued) frame(0);
            return queued.length;
        },
        pendingFrames: () => frames.length,
        touch: (name, identifier) => fire(name, {changedTouches: [{identifier, clientX: 10, clientY: 20}]}),
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
