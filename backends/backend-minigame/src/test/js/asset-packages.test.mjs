import assert from 'node:assert/strict';
import {test} from 'node:test';
import {createRequire} from 'node:module';
const require = createRequire(import.meta.url);
const {create} = require('../../main/resources/templates/minigame/asset-packages.js');
const files = {'theme/regal/cards.atlas': 'regal', 'theme/regal/cards.png': 'regal', 'cardback/blue.atlas': 'backs'};
function fixture() {
    const requests = [];
    const api = create({loadSubpackage(request) { requests.push(request); return {}; }}, files);
    return {api, requests};
}
test('manifest membership is not readiness; atlas and texture share one request', async () => {
    const {api, requests} = fixture();
    assert.equal(api.isMissing('/theme/regal/cards.atlas'), true);
    assert.equal(api.isMissing('theme/default/cards.atlas'), false);
    assert.equal(api.root('theme/regal/cards.png'), 'optional/regal/');
    assert.equal(api.root('ui.png'), 'assets/');
    const a = api.load(['theme/regal/cards.atlas', 'theme/regal/cards.png']);
    const b = api.load(['theme/regal/cards.png']);
    assert.equal(requests.length, 1);
    assert.equal(api.isLoading('theme/regal/cards.png'), true);
    requests[0].success();
    await Promise.all([a, b]);
    assert.equal(api.isMissing('theme/regal/cards.atlas'), false);
    assert.equal(api.isLoading('theme/regal/cards.png'), false);
    await api.load(['theme/regal/cards.png']);
    assert.equal(requests.length, 1);
});
test('failure is retryable and late callbacks cannot complete the next attempt', async () => {
    const {api, requests} = fixture();
    const first = api.load(['theme/regal/cards.atlas']);
    requests[0].fail(new Error('offline'));
    await assert.rejects(first, /offline/);
    assert.equal(api.isMissing('theme/regal/cards.atlas'), true);
    const second = api.load(['theme/regal/cards.atlas']);
    requests[0].success();
    assert.equal(api.isMissing('theme/regal/cards.atlas'), true);
    requests[1].success();
    await second;
    assert.equal(api.isMissing('theme/regal/cards.atlas'), false);
});
test('a fresh version/runtime must load the selected package again, and only that package', async () => {
    const old = fixture();
    const loading = old.api.load(['theme/regal/cards.atlas']); old.requests[0].success(); await loading;
    const next = fixture();
    assert.equal(next.api.isMissing('theme/regal/cards.atlas'), true);
    const selected = next.api.load(['theme/regal/cards.atlas', 'cardback/blue.atlas']);
    assert.deepEqual(next.requests.map(r => r.name), ['regal', 'backs']);
    next.requests.forEach(r => r.success()); await selected;
});
test('stalled callback times out and retry can succeed', async (t) => {
    t.mock.timers.enable({apis: ['setTimeout']});
    const {api, requests} = fixture();
    const first = api.load(['theme/regal/cards.atlas']);
    const rejected = assert.rejects(first, /stalled/);
    t.mock.timers.tick(30001); await rejected;
    const retry = api.load(['theme/regal/cards.atlas']); requests[1].success(); await retry;
});
test('missing API and synchronous failures never falsely mark a package ready', async () => {
    const unsupported = create({}, files);
    await assert.rejects(unsupported.load(['theme/regal/cards.atlas']), /unavailable/);
    const synchronous = create({loadSubpackage(r) { r.fail(new Error('failed')); }}, files);
    await assert.rejects(synchronous.load(['theme/regal/cards.atlas']), /failed/);
    await assert.rejects(synchronous.load(['theme/regal/cards.atlas']), /failed/);
    assert.equal(synchronous.isMissing('theme/regal/cards.atlas'), true);
});
