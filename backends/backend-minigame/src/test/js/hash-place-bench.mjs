// Measures time and sampled heap allocation of libGDX ObjectIntMap lookups in a compiled hash-place
// probe. Not part of `check`; run it by hand against a probe built with and without
// FibonacciHashTransformer:
//   node src/test/js/hash-place-bench.mjs [app.js ...]
// With no arguments it measures build/regression-probes/hash-place.
import {readFileSync} from 'node:fs';
import {Session} from 'node:inspector/promises';
import vm from 'node:vm';
import {typedArrays} from './engine.mjs';

const apps = process.argv.length > 2 ? process.argv.slice(2)
    : [new URL('../../../build/regression-probes/hash-place/minigame/subpackages/game/app.js', import.meta.url)];
const rounds = 200_000;
const lookupsPerRound = 8;

function startProbe(app) {
    const context = vm.createContext({exports: {}, console, performance, setTimeout, clearTimeout, ...typedArrays});
    vm.runInContext(readFileSync(app, 'utf8'), context, {timeout: 5000});
    context.exports.main([], error => {if (error) throw error;});
    return (command, arg = 0) => context.__hashPlaceProbe(command, arg);
}

async function measure(app) {
    const run = startProbe(app);
    run('bench', 20_000); // warm-up
    // Time without the profiler (sampling slows allocation-heavy code down), then sample allocation.
    const times = [];
    for (let repeat = 0; repeat < 5; repeat++) {
        const start = performance.now();
        run('bench', rounds);
        times.push(performance.now() - start);
    }
    const session = new Session();
    session.connect();
    let sampledBytes = 0;
    for (let repeat = 0; repeat < 5; repeat++) {
        await session.post('HeapProfiler.startSampling',
            {samplingInterval: 256, includeObjectsCollectedByMajorGC: true, includeObjectsCollectedByMinorGC: true});
        run('bench', rounds);
        const {profile} = await session.post('HeapProfiler.stopSampling');
        sampledBytes += profile.samples.reduce((sum, sample) => sum + sample.size, 0);
    }
    session.disconnect();
    times.sort((a, b) => a - b);
    const lookups = rounds * lookupsPerRound;
    const median = times[Math.floor(times.length / 2)];
    return {
        app: String(app).split('/').slice(-3).join('/'),
        'median ms': median.toFixed(1),
        'ns/lookup': (median * 1e6 / lookups).toFixed(1),
        'sampled MB': (sampledBytes / 5 / 1e6).toFixed(2),
        'bytes/lookup': (sampledBytes / 5 / lookups).toFixed(1),
    };
}

const results = [];
for (const app of apps) results.push(await measure(app));
console.log(`${rounds} rounds x ${lookupsPerRound} ObjectIntMap<String>.get, median of 5, sampled allocation includes collected objects`);
console.table(results);
