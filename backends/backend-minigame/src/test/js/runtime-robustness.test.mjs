import {readFileSync} from 'node:fs';
import vm from 'node:vm';
import assert from 'node:assert/strict';
import test from 'node:test';
import {typedArrays} from './engine.mjs';

const app = new URL('../../../build/regression-probes/runtime-robustness/minigame/subpackages/game/app.js', import.meta.url);

/** InnerAudioContext that, like WeChat's, adds a listener on every on*() call. */
class MockAudioContext {
    constructor() {
        this.ended = [];
        this.errors = [];
        this.state = 'idle';
        this.destroyed = false;
    }
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

function startProbe() {
    const frames = [];
    const listeners = {};
    const contexts = [];
    const modals = [];
    const restarts = [];
    const report = {events: []};
    const on = name => listener => (listeners[name] ??= []).push(listener);
    const gl = noopProxy({getParameter: () => 'WebGL 1.0 (probe)', getExtension: () => null,
        getSupportedExtensions: () => []});
    const wx = noopProxy({
        getWindowInfo: () => ({pixelRatio: 1, screenWidth: 100, screenHeight: 200, safeArea: null}),
        getDeviceInfo: () => ({platform: 'devtools'}),
        getFileSystemManager: () => noopProxy({readFileSync() {throw new Error('missing');}}),
        env: {USER_DATA_PATH: 'wxfile://usr'},
        getStorageSync: () => '',
        getStorageInfoSync: () => ({keys: []}),
        createInnerAudioContext() {
            const context = new MockAudioContext();
            contexts.push(context);
            return context;
        },
        onShow: on('show'), onHide: on('hide'),
        onAudioInterruptionBegin: on('interruptionBegin'), onAudioInterruptionEnd: on('interruptionEnd'),
        onTouchStart: on('touchstart'), onTouchMove: on('touchmove'),
        onTouchEnd: on('touchend'), onTouchCancel: on('touchcancel'),
        showModal: options => modals.push(options),
        restartMiniProgram: options => restarts.push(options),
    });
    const canvas = {width: 100, height: 200, getContext: () => gl};
    const quiet = noopProxy();
    const context = vm.createContext({exports: {}, console: quiet, performance, wx, canvas, __robustnessProbe: report,
        requestAnimationFrame(callback) {frames.push(callback); return frames.length;},
        cancelAnimationFrame() {}, setTimeout, clearTimeout, ...typedArrays});
    vm.runInContext(readFileSync(app, 'utf8'), context, {timeout: 5000});
    let mainError;
    context.exports.main([], error => {mainError = error;});
    assert.equal(mainError, undefined);

    const fire = (name, event) => {for (const listener of listeners[name] ?? []) listener(event);};
    return {
        report, contexts, modals, restarts, listeners, fire,
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
