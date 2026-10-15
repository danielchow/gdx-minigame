import {readFileSync} from 'node:fs';
import vm from 'node:vm';
import assert from 'node:assert/strict';
import test from 'node:test';
import {typedArrays} from './engine.mjs';

const app = new URL('../../../build/regression-probes/share/minigame/subpackages/game/app.js', import.meta.url);

/** A WeChat 2D canvas reduced to what an encode touches. */
class MockCanvas {
    constructor() {this.width = 300; this.height = 150; this.encodes = [];}
    getContext(type) {
        assert.equal(type, '2d');
        const canvas = this;
        return {
            createImageData: (width, height) => ({width, height, data: new Uint8ClampedArray(width * height * 4)}),
            putImageData(image, x, y) {canvas.drawn = {image, x, y};},
        };
    }
    toTempFilePathSync(options) {
        this.encodes.push(options);
        if (this.failNext) return '';
        return `wxfile://tmp/encoded-${this.encodes.length}.png`;
    }
}

function startProbe({launchQuery = {}, shareAppMessage = true, shareTimeline = true} = {}) {
    const listeners = {};
    const shares = [];
    const shareMenus = [];
    const canvases = [];
    const report = {events: []};
    const wx = {
        getLaunchOptionsSync: () => ({scene: 1007, query: launchQuery}),
        onShow: listener => (listeners.show ??= []).push(listener),
        showShareMenu: options => shareMenus.push(options),
        onShareAppMessage: listener => (listeners.shareMenu ??= []).push(listener),
        createCanvas() {
            const canvas = new MockCanvas();
            canvases.push(canvas);
            return canvas;
        },
    };
    if (shareAppMessage) wx.shareAppMessage = options => shares.push(options);
    if (shareTimeline) wx.onShareTimeline = listener => (listeners.timeline ??= []).push(listener);
    const context = vm.createContext({exports: {}, console, performance, wx, __shareProbe: report,
        setTimeout, clearTimeout, ...typedArrays});
    vm.runInContext(readFileSync(app, 'utf8'), context, {timeout: 5000});
    let mainError;
    context.exports.main([], error => {mainError = error;});
    assert.equal(mainError, undefined);
    return {
        report, shares, shareMenus, canvases, listeners,
        command: (name, arg = 0) => report.command(name, arg),
        openMenu: () => listeners.shareMenu.map(listener => listener({from: 'menu'})),
        openTimeline: () => listeners.timeline.map(listener => listener()),
    };
}

test('shareAppMessage passes title, image and query, and omits what the game left out', () => {
    const probe = startProbe();
    assert.equal(probe.command('supported'), 1);
    probe.command('share');
    probe.command('shareTitleOnly');
    assert.deepEqual(JSON.parse(JSON.stringify(probe.shares)), [
        {title: 'Can you solve it?', imageUrl: 'wxfile://tmp/card.png', query: 'deal=ABC123'},
        {title: 'Title only'},
    ]);
    assert.equal(startProbe({shareAppMessage: false}).command('supported'), 0);
});

test('the share menu offers friends only and answers with the provider card', () => {
    const probe = startProbe();
    probe.command('menu', 0);
    assert.deepEqual(JSON.parse(JSON.stringify(probe.shareMenus)), [{menus: ['shareAppMessage']}], 'no Moments');
    const [card] = probe.openMenu();
    assert.deepEqual({...card}, {title: 'Deal card', imageUrl: 'wxfile://tmp/deal.png', query: 'deal=XYZ'});
    assert.deepEqual(probe.report.events, ['provide:0']);
});

test('a late reply resolves the promise; the placeholder carries the invite image meanwhile', async () => {
    const probe = startProbe();
    probe.command('menu', 1);
    const [card] = probe.openMenu();
    assert.equal(card.title, 'Invite');
    assert.equal(card.imageUrl, 'invite.png', 'never a screenshot while WeChat waits');
    assert.equal(typeof card.promise?.then, 'function');
    let resolved;
    card.promise.then(value => {resolved = value;});
    await Promise.resolve();
    assert.equal(resolved, undefined, 'unresolved until the game replies');
    probe.command('replyLate');
    await Promise.resolve();
    assert.deepEqual({...resolved}, {title: 'Late card', imageUrl: 'wxfile://tmp/late.png', query: 'deal=LATE'});
    // A second reply to the same menu opening is ignored.
    probe.command('replyLate');
});

test('a reply without an image keeps the placeholder image; no reply leaves the placeholder', () => {
    const probe = startProbe();
    probe.command('menu', 2);
    const [card] = probe.openMenu();
    assert.deepEqual({...card}, {title: 'No image', imageUrl: 'invite.png', query: 'deal=NOIMG'});

    const silent = startProbe();
    silent.command('menu', 3);
    const [placeholder] = silent.openMenu();
    assert.equal(placeholder.title, 'Invite');
    assert.equal(placeholder.imageUrl, 'invite.png');
    assert.equal(placeholder.query, '');
    assert.equal(typeof placeholder.promise?.then, 'function', 'WeChat falls back to the placeholder after 3 s');
});

test('the launch query is delivered at once, later onShow queries as they come, empty ones never', () => {
    const probe = startProbe({launchQuery: {deal: 'ABC123', from: 'share'}});
    probe.command('listen');
    assert.deepEqual(probe.report.events, ['query:deal=ABC123:from=share']);
    probe.listeners.show.forEach(listener => listener({scene: 1089, query: {}}));
    probe.listeners.show.forEach(listener => listener({scene: 1007, query: {deal: 'D1.xyz-_'}}));
    probe.listeners.show.forEach(listener => listener({scene: 1001}));
    assert.deepEqual(probe.report.events, ['query:deal=ABC123:from=share', 'query:deal=D1.xyz-_']);

    const plain = startProbe();
    plain.command('listen');
    assert.deepEqual(plain.report.events, [], 'an ordinary launch delivers nothing');
});

test('encodePng draws the exact pixels into one reused offscreen canvas and returns its PNG', () => {
    const probe = startProbe();
    probe.command('encode');
    probe.command('encode');
    assert.equal(probe.canvases.length, 1, 'one offscreen canvas for every encode');
    const [canvas] = probe.canvases;
    assert.equal(canvas.width, 2);
    assert.equal(canvas.height, 1);
    assert.deepEqual([...canvas.drawn.image.data], [255, 0, 0, 255, 0, 0, 255, 128], 'unsigned bytes, rows as given');
    assert.deepEqual([canvas.drawn.x, canvas.drawn.y], [0, 0]);
    assert.deepEqual({...canvas.encodes[0]}, {x: 0, y: 0, width: 2, height: 1, destWidth: 2, destHeight: 1, fileType: 'png'});
    assert.deepEqual(probe.report.events, ['path:wxfile://tmp/encoded-1.png', 'path:wxfile://tmp/encoded-2.png']);
});

test('encodePng rejects a wrong pixel count and reports a failed encode', () => {
    const probe = startProbe();
    probe.command('encodeWrongSize');
    assert.equal(probe.canvases.length, 0, 'nothing reaches WeChat');
    probe.command('encode');
    probe.canvases[0].failNext = true;
    probe.command('encodeFails');
    assert.deepEqual(probe.report.events.filter(e => !e.startsWith('path:')), ['rejected', 'failed']);
});

test('Moments shows the provider card, with the card image as its preview', () => {
    const probe = startProbe();
    probe.command('timeline', 0);
    assert.deepEqual(JSON.parse(JSON.stringify(probe.shareMenus)), [{menus: ['shareTimeline']}]);
    const [card] = probe.openTimeline();
    assert.deepEqual({...card}, {title: 'Deal card', imageUrl: 'wxfile://tmp/deal.png',
        imagePreviewUrl: 'wxfile://tmp/deal.png', query: 'deal=XYZ'}, 'never the default screenshot preview');
    assert.deepEqual(probe.report.events, ['timeline:0']);
});

test('Moments posts the placeholder when the provider replies late or not at all', () => {
    const invite = {title: 'Invite', imageUrl: 'invite.png', imagePreviewUrl: 'invite.png', query: ''};
    const late = startProbe();
    late.command('timeline', 1);
    const [card] = late.openTimeline();
    assert.deepEqual({...card}, invite, 'WeChat ignores promises for Moments');
    assert.equal(card.promise, undefined);
    late.command('replyLate');
    assert.deepEqual({...card}, invite, 'a reply after returning changes nothing');

    const silent = startProbe();
    silent.command('timeline', 3);
    assert.deepEqual({...silent.openTimeline()[0]}, invite);

    const noImage = startProbe();
    noImage.command('timeline', 2);
    assert.deepEqual({...noImage.openTimeline()[0]}, {title: 'No image', imageUrl: 'invite.png',
        imagePreviewUrl: 'invite.png', query: 'deal=NOIMG'});
});

test('chat and Moments menus stay shown together in either order', () => {
    const chatFirst = startProbe();
    chatFirst.command('menu', 0);
    chatFirst.command('timeline', 0);
    assert.deepEqual(JSON.parse(JSON.stringify(chatFirst.shareMenus.at(-1))), {menus: ['shareAppMessage', 'shareTimeline']});

    const timelineFirst = startProbe();
    timelineFirst.command('timeline', 0);
    timelineFirst.command('menu', 0);
    assert.deepEqual(JSON.parse(JSON.stringify(timelineFirst.shareMenus.at(-1))), {menus: ['shareAppMessage', 'shareTimeline']});
});

test('without wx.onShareTimeline, Moments stays hidden and the chat menu is unchanged', () => {
    const probe = startProbe({shareTimeline: false});
    probe.command('timeline', 0);
    probe.command('menu', 0);
    assert.deepEqual(JSON.parse(JSON.stringify(probe.shareMenus)), [{menus: ['shareAppMessage']}]);
});
