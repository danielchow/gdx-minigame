import {readFileSync} from 'node:fs';
import {createRequire} from 'node:module';
import vm from 'node:vm';
import assert from 'node:assert/strict';
import {test} from 'node:test';
const require = createRequire(import.meta.url);
const {create} = require('../../main/resources/templates/minigame/asset-packages.js');
const app = new URL('../../../build/regression-probes/storage-and-assets/minigame/subpackages/game/app.js', import.meta.url);

for (const succeeds of [true, false]) test('release-compiled API gates existence and routes renamed payloads without changing listings: ' + succeeds, async () => {
    const requests = [], reads = [], report = {log: [], results: {}};
    const packaged = {
        'assets/preload.txt': 'i:b:theme/regal/card.custom:5:0\n',
        'assets/rename-map.json': '{"/theme/regal/card.custom":"/theme/regal/card.custom.txt"}',
        'optional/regal/theme/regal/card.custom.txt': 'regal',
    };
    const wx = {env: {USER_DATA_PATH: 'wxfile://usr'},
        loadSubpackage(r) {requests.push(r); return {};},
        getStorageInfoSync: () => ({keys: []}), getStorageSync: () => '',
        getFileSystemManager: () => ({mkdirSync() {}, readFileSync(path, encoding) {
            reads.push(path);
            if (!(path in packaged)) throw new Error('missing ' + path);
            return encoding ? packaged[path] : new TextEncoder().encode(packaged[path]).buffer;
        }})};
    const packages = create(wx, {'theme/regal/card.custom': 'regal'});
    const context = vm.createContext({exports: {}, console, performance, wx, __storageProbe: report,
        __miniGameAssetPackages: packages, setTimeout, clearTimeout});
    vm.runInContext(readFileSync(app, 'utf8'), context);
    vm.runInContext("exports.main(['packages'], error => {if (error) throw error;})", context);
    assert.equal(report.results['before-exists'], 'false');
    assert.equal(report.results['before-length'], '0');
    assert.equal(report.results.directory, 'true');
    assert.equal(report.results.listed, 'theme/regal/card.custom');
    assert.equal(report.results['early-read'], 'blocked');
    assert.equal(report.results.loading, 'true');
    assert.equal(reads.some(p => p.startsWith('optional/')), false);
    assert.equal(requests.length, 1);
    assert.equal(report.results['loader-before'], 'false');
    assert.equal(report.results['loader-queue'], 'true');
    assert.equal(report.results['loader-count'], '1');
    assert.equal(report.results['optional-loader'], undefined);
    if (succeeds) requests[0].success();
    else requests[0].fail({errMsg: 'network'});
    await new Promise(resolve => setImmediate(resolve));
    if (!succeeds) {
        assert.equal(report.results.ready, 'false');
        assert.equal(report.results['optional-loader'], 'failure:theme/regal/card.custom');
        assert.equal(reads.some(p => p.startsWith('optional/')), false);
        return;
    }
    assert.equal(report.results['optional-loader'], 'success:theme/regal/card.custom:regal');
    assert.equal(report.results.ready, 'true');
    assert.equal(report.results['after-exists'], 'true');
    assert.equal(report.results.contents, 'regal');
    assert(reads.includes('optional/regal/theme/regal/card.custom.txt'));
});
