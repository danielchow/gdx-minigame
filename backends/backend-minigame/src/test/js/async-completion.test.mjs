import {readFileSync} from 'node:fs';
import vm from 'node:vm';
import assert from 'node:assert/strict';
import test from 'node:test';

test('Mini Game futures complete once through inline polling and queued timers', () => {
    const app = new URL('../../../build/regression-probes/async-completion/minigame/subpackages/game/app.js', import.meta.url);
    const timers = [];
    const report = {events: []};
    const context = vm.createContext({exports: {}, console, performance, __asyncProbe: report,
        setTimeout(callback) {timers.push(callback); return timers.length;},
        clearTimeout() {}});
    vm.runInContext(readFileSync(app, 'utf8'), context, {timeout: 5000});
    // Run entry and callbacks inside the VM timeout so a loading regression
    // cannot leave the test hanging in an infinite synchronous finishLoading.
    vm.runInContext('exports.main([], error => {if (error) throw error; __asyncProbe.returned = true;})',
        context, {timeout: 5000});
    assert.equal(report.returned, true);
    const timerErrors = [];
    for (let i = 0; i < timers.length; i++) {
        assert(i < 30, 'unexpected callback loop');
        context.nextTimer = timers[i];
        try {vm.runInContext('nextTimer()', context, {timeout: 5000});}
        catch (error) {timerErrors.push(String(error));}
    }
    assert.deepEqual(timerErrors, [], 'failures belong to Future.get(), never a stale timer');
    const values = name => report.events.filter(event => event.event === name).map(event => event.value);
    const cases = ['inline-success', 'inline-failure', 'inline-dependencies', 'timer-success',
        'timer-failure', 'timer-error', 'poll-success', 'poll-failure'];
    for (const name of cases) {
        assert.deepEqual(values(name + '-loadAsync'), [1], `${name}: async task executes once`);
    }
    for (const name of ['inline-success', 'inline-dependencies', 'timer-success']) {
        assert.deepEqual(values(name + '-loadSync'), [1], `${name}: first result is published once`);
        assert.deepEqual(values(name + '-finish-returned'), [1]);
    }
    for (const name of ['inline-failure', 'timer-failure', 'timer-error']) {
        assert.deepEqual(values(name + '-loadSync'), [], `${name}: failure cannot publish an asset`);
        assert.deepEqual(values(name + '-finish-threw'), [1]);
    }
    assert.deepEqual(values('poll-success-get-returned'), [1, 1]);
    assert.deepEqual(values('poll-failure-get-threw'), [1, 1], 'failure remains readable after its pending timer');
});
