import {readFileSync} from 'node:fs';
import vm from 'node:vm';
import assert from 'node:assert/strict';
import test from 'node:test';

const app = new URL('../../../build/regression-probes/storage-and-assets/minigame/subpackages/game/app.js', import.meta.url);

// Storage written by an older backend: "dup" exists under two type suffixes.
const seed = {
    'pref:p:dupi': '7', 'pref:p:dups': '8', 'pref:p:keepi': '1', 'pref:p:flagb': 'true',
    'pref:p:ratiof': '1.5', 'pref:p:texts': 'hello', 'other:key': 'untouched',
};
const packaged = {
    'assets/preload.txt': 'i:b:present.txt:3:0\n',
    'assets/rename-map.json': '{}\n',
    'assets/present.txt': 'hi!',
};

function runProbe() {
    const storage = new Map(Object.entries(seed));
    const report = {log: [], results: {}};
    const fsm = {
        readFileSync(path, encoding) {
            if (!(path in packaged)) throw new Error('no such file: ' + path);
            const text = packaged[path];
            return encoding ? text : new TextEncoder().encode(text).buffer;
        },
        mkdirSync() {},
    };
    const wx = {
        env: {USER_DATA_PATH: 'wxfile://usr'},
        getFileSystemManager: () => fsm,
        getStorageInfoSync: () => ({keys: [...storage.keys()]}),
        getStorageSync: key => storage.has(key) ? storage.get(key) : '',
        setStorageSync(key, value) { report.log.push(['set', key, value]); storage.set(key, String(value)); },
        removeStorageSync(key) { report.log.push(['remove', key]); storage.delete(key); },
    };
    const context = vm.createContext({exports: {}, console, performance, wx, __storageProbe: report,
        setTimeout() {}, clearTimeout() {}});
    vm.runInContext(readFileSync(app, 'utf8'), context, {timeout: 5000});
    vm.runInContext('exports.main([], error => {if (error) throw error;})', context, {timeout: 5000});
    return {storage, report};
}

const {storage, report} = runProbe();
const {results} = report;

/** Storage calls the probe made between two of its marks. */
function callsBetween(from, to) {
    const start = report.log.findIndex(entry => entry[0] === 'mark' && entry[1] === from);
    const end = report.log.findIndex(entry => entry[0] === 'mark' && entry[1] === to);
    assert(start >= 0 && end > start, `marks ${from} and ${to} must be recorded`);
    return report.log.slice(start + 1, end).map(([op, key]) => `${op} ${key}`);
}

test('probe ran every case', () => {
    assert.equal(results.main, 'complete');
    assert.deepEqual(Object.keys(results).filter(name => name.startsWith('error:'))
        .map(name => `${name}: ${results[name]}`), []);
});

test('flush writes only changed keys and removes only removed or retyped ones', () => {
    assert.deepEqual(callsBetween('flush-after-load', 'flush-unchanged'), ['remove pref:p:dupi'],
        'a loaded key is not rewritten; only the losing duplicate is deleted');
    assert.deepEqual(callsBetween('flush-unchanged', 'flush-two-changes'), []);
    assert.deepEqual(callsBetween('flush-two-changes', 'type-change').sort(),
        ['set pref:p:names', 'set pref:p:scorei']);
    assert.deepEqual(callsBetween('type-change', 'remove').sort(), ['remove pref:p:scorei', 'set pref:p:scores']);
    assert.deepEqual(callsBetween('remove', 'done'), ['remove pref:p:names']);
});

test('a type change leaves one storage key per preference', () => {
    const prefKeys = [...storage.keys()].filter(key => key.startsWith('pref:p:')).sort();
    assert.deepEqual(prefKeys, ['pref:p:dups', 'pref:p:flagb', 'pref:p:keepi', 'pref:p:ratiof',
        'pref:p:scores', 'pref:p:texts']);
    assert.equal(storage.get('other:key'), 'untouched');
    assert.equal(results['reload-score'], 'five');
    assert.equal(results['reload-has-name'], 'false');
    const encoded = [...storage.keys()].filter(key => !(key in seed) && !key.startsWith('pref:'));
    assert.equal(encoded.length, 1, 'encoded preferences also drop the old typed key');
    assert.equal(results['encoded-reload'], 'one');
});

test('a key stored under several types loads deterministically', () => {
    assert.equal(results['dup-string'], '8', 'the string-typed entry wins');
    assert.equal(results['dup-int'], '8');
});

test('getters convert mismatched types or return the default', () => {
    assert.equal(results['keep-as-string'], '1');
    assert.equal(results['keep-as-long'], '1');
    assert.equal(Number(results['keep-as-float']), 1);
    assert.equal(results['keep-as-boolean'], 'true', 'unparseable boolean returns the default');
    assert.equal(results['ratio-as-int'], '42');
    assert.equal(results['flag-as-int'], '3');
    assert.equal(results['flag-as-string'], 'true');
    assert.equal(results['text-as-boolean'], 'true');
    assert.equal(results['score-as-int'], '9');
});

test('asset loader and downloader listeners fire', () => {
    assert.equal(results['loader-present'], 'success:present.txt:hi!');
    assert.equal(results['loader-overwrite'], 'success:present.txt:hi!');
    assert.equal(results['loader-missing'], 'failure:missing.png');
    assert.equal(results.download, 'failure:https://example.com/a.png');
    assert.equal(results['pixmap-download'], 'failed');
});

test('preload and loadScript report completion', () => {
    assert.equal(results['loader-preload'], 'success:assets.txt');
    assert.equal(results['loader-script'], 'success:freetype.js');
});

test('AssetManager reports a missing file instead of loading forever', () => {
    assert.equal(results['manager-present'], 'loaded:hi!');
    assert.match(results['manager-missing'] ?? '', /^threw\|.*Couldn't load file: missing\.bin/);
});

test('file handles keep their type for equals and hashCode', () => {
    assert.equal(results['fh-type-field'], 'Internal');
    assert.equal(results['fh-equal-same'], 'true');
    assert.equal(results['fh-equal-cross-type'], 'false');
    assert.equal(results['fh-hash-same'], 'true');
    assert.equal(results['fh-map'], 'internal,2');
});
