import {readFileSync} from 'node:fs';
import {deflateSync} from 'node:zlib';
import vm from 'node:vm';
import assert from 'node:assert/strict';
import test from 'node:test';
import {loadEngine, typedArrays} from './engine.mjs';

const root = new URL('../../../build/regression-probes/native-texture/minigame/', import.meta.url);
function crc(bytes) {
    let value = -1;
    for (const byte of bytes) {
        value ^= byte;
        for (let bit = 0; bit < 8; bit++) value = (value >>> 1) ^ ((value & 1) ? 0xedb88320 : 0);
    }
    return (value ^ -1) >>> 0;
}
function chunk(type, content) {
    const data = Buffer.concat([Buffer.from(type), content]);
    const length = Buffer.alloc(4), checksum = Buffer.alloc(4);
    length.writeUInt32BE(content.length); checksum.writeUInt32BE(crc(data));
    return Buffer.concat([length, data, checksum]);
}
function png({width = 2, height = 2, type = 6, gamma = false} = {}) {
    const header = Buffer.alloc(13);
    header.writeUInt32BE(width, 0); header.writeUInt32BE(height, 4); header[8] = 8; header[9] = type;
    const channels = ({0:1, 2:3, 3:1, 4:2, 6:4})[type];
    const scanlines = Buffer.alloc(height * (width * channels + 1));
    for (let y = 0; y < height; y++) for (let x = 0; x < width; x++) {
        const offset = y * (width * channels + 1) + 1 + x * channels;
        for (let c = 0; c < channels; c++) scanlines[offset + c] = type === 3 ? x % 2 : [17,34,51,68][c];
    }
    return Buffer.concat([Buffer.from('89504e470d0a1a0a', 'hex'), chunk('IHDR', header),
        ...(type === 3 ? [chunk('PLTE', Buffer.from([17,34,51,60,70,80])), chunk('tRNS', Buffer.from([68,255]))] : []),
        ...(gamma ? [chunk('gAMA', Buffer.from([0,0,177,143]))] : []),
        chunk('IDAT', deflateSync(scanlines)), chunk('IEND', Buffer.alloc(0))]);
}
const noop = values => new Proxy(values, {get: (target, key) => key in target ? target[key] : () => null});

async function start({paths = ['native.png'], assets = {}, rename = {}, packageRoot = 'assets/', missing = false,
    createThrows = false, srcThrows = false, uploadThrows = false, uploadError = 0} = {}) {
    const engine = new URL('subpackages/engine/gdx.wasm.js', root);
    const {gdx} = await loadEngine(engine, readFileSync(new URL('gdx.wasm', engine)));
    const stats = {decodes:0, frees:0, uploads:[], parameters:[], events:[], paths:paths.join('\n'), logs:[], images:[], mipmaps:0};
    const realLoad = gdx.Gdx.prototype.g2d_load, realFree = gdx.Gdx.prototype.g2d_free;
    gdx.Gdx.prototype.g2d_load = function(...args) {stats.decodes++; return realLoad.apply(this,args);};
    gdx.Gdx.prototype.g2d_free = function(...args) {stats.frees++; return realFree.apply(this,args);};
    const files = {'native.png':png(), 'old.png':png(), ...assets};
    const disk = new Map(Object.entries(files).map(([path, data]) => [packageRoot + (rename[path] ?? path), data]));
    disk.set('assets/preload.txt', Object.entries(files).map(([path,data]) => 'i:b:' + path + ':' + data.length + ':false').join('\n'));
    disk.set('assets/rename-map.json', JSON.stringify(rename));
    const frames = [], timers = new Map(), listeners = {};
    let nextTimer = 1, nextTexture = 1, boundTexture, nextError = 0;
    const pixelStore = new Map([[37440,true],[37441,false],[37443,0x9244],[3317,4]]);
    const gl = noop({getParameter(key) {return pixelStore.has(key) ? pixelStore.get(key) : 'WebGL 1.0 (native texture probe)';},
        pixelStorei(key,value) {pixelStore.set(key,value);}, getSupportedExtensions: () => [], getExtension: () => null,
        createTexture: () => ({id:nextTexture++}), bindTexture(target,texture) {boundTexture = texture;},
        texParameteri(...args) {stats.parameters.push(args);},
        texImage2D(...args) {
            const native = args.length === 6;
            if (native && uploadThrows) throw new Error('native upload failed');
            if (native) {
                assert.equal(pixelStore.get(37441), false);
                assert.equal(pixelStore.get(37443), 0);
                assert.equal(pixelStore.get(37440), false);
                assert.equal(args[2], 6408); assert.equal(args[3], 6408); assert.equal(args[4], 5121);
                nextError = uploadError;
            }
            stats.uploads.push({native, level:args[1], texture:boundTexture,
                source:native ? args[5].src : null,
                pixels:native ? null : args[8] && Array.from(args[8])});
        },
        getError() {const result = nextError; nextError = 0; return result;},
        generateMipmap() {stats.mipmaps++;},
    });
    const on = name => callback => (listeners[name] ??= []).push(callback);
    const wx = noop({
        getWindowInfo: () => ({pixelRatio:1, screenWidth:100, screenHeight:200, windowWidth:100, windowHeight:200, safeArea:null}),
        getDeviceInfo: () => ({platform:'android'}), getSystemInfoSync: () => ({platform:'android', pixelRatio:1}),
        getFileSystemManager: () => noop({readFileSync(path, encoding) {
            const value = disk.get(path);
            if (value === undefined) throw new Error('missing: ' + path);
            if (encoding) return value.toString();
            const bytes = Buffer.from(value);
            return bytes.buffer.slice(bytes.byteOffset, bytes.byteOffset + bytes.byteLength);
        }}),
        env:{USER_DATA_PATH:'wxfile://usr'}, getStorageSync: () => '', getStorageInfoSync: () => ({keys:[]}),
        createImage() {
            if (createThrows) throw new Error('create failed');
            const image = {width:2, height:2, _src:null,
                set src(value) {
                    assert.equal(this.premultiplyAlpha, false, 'disable premultiplication BEFORE src');
                    this._src = value; this.lateLoad = this.onload; this.lateError = this.onerror;
                    if (srcThrows) throw new Error('src failed');
                }, get src() {return this._src;}};
            stats.images.push(image); return image;
        },
        onShow:on('show'), onHide:on('hide'), onWindowResize:on('resize'), onWindowStateChange:on('windowState'),
        onTouchStart:on('touch'), onTouchMove:on('touchmove'), onTouchEnd:on('touchend'), onTouchCancel:on('touchcancel'),
        onAudioInterruptionBegin:on('interruption'), onAudioInterruptionEnd:on('interruptionEnd'),
    });
    const console = noop(Object.fromEntries(['log','info','warn','error'].map(name => [name, (...args) => stats.logs.push(args.join(' '))])));
    const context = vm.createContext({exports:{}, console, performance, wx, Gdx:gdx, window:{Gdx:gdx},
        canvas:{width:100,height:200,getContext: () => gl}, __nativeProbe:stats,
        __miniGameAssetPackages:{isMissing: () => missing, root: () => packageRoot},
        requestAnimationFrame(callback) {frames.push(callback);return frames.length;}, cancelAnimationFrame() {},
        setTimeout(callback,delay) {const id=nextTimer++;timers.set(id,{callback,delay});return id;},
        clearTimeout(id) {timers.delete(id);}, ...typedArrays});
    vm.runInContext(readFileSync(new URL('subpackages/game/app.js',root),'utf8'),context,{timeout:5000});
    let mainError;
    context.exports.main([],error => {mainError=error;});
    if (mainError) throw mainError;
    const command = (operation,path='native.png',argument=0) => stats.command(operation,path,argument);
    return {stats, frames, timers, gl, pixelStore, command,
        fire(name,event={}) {for (const callback of listeners[name] ?? []) callback(event);},
        runFrames() {const queued=frames.splice(0);for (const callback of queued) callback(0);return queued.length;},
        timer(delay) {for (const [id,timer] of [...timers]) if (timer.delay===delay) {timers.delete(id);timer.callback();}},
        loaded(path='native.png',size={width:2,height:2}) {
            const image=stats.images.find(image => image.src === packageRoot + (rename[path] ?? path));
            Object.assign(image,size); image.onload(); return image;
        },
        warnings() {return stats.logs.filter(line => line.includes('[NativeTextures]'));},
    };
}

test('native first upload, typed pixel readers and synchronous managed reload use the real decoder correctly', async () => {
    const p=await start();
    assert.equal(p.frames.length,0);
    const image=p.loaded(); p.runFrames();
    assert.deepEqual(p.stats.events,['create','resize','render']);
    assert.equal(p.command('pixelFile'),0x11223344);
    assert.equal(p.command('directData'),0x11223344);
    assert.equal(p.stats.decodes,2);
    const oldStore=new Map(p.pixelStore);
    assert.equal(p.command('factory'),1,'plain texture uses Custom data');
    assert.equal(p.stats.decodes,2,'native texture never calls stb_image');
    assert.deepEqual(p.pixelStore,oldStore,'restore all four pixel-store values');
    assert.equal(p.stats.uploads.at(-1).source,'assets/native.png');
    assert.equal(image.onload,null); assert.equal(image.onerror,null);
    assert.equal(p.timers.size,0,'claiming the final image cancels cache expiry');
    assert.equal(p.command('pixelException'),1);
    assert.equal(p.command('managed'),1);
    assert.equal(p.command('width'),2); assert.equal(p.command('height'),2);
    assert.equal(p.command('drawManaged'),1,'Texture.draw keeps its existing managed-texture rejection');
    p.command('filter');
    assert.equal(p.command('reload'),1);
    assert.equal(p.stats.decodes,3,'managed reload deliberately decodes once through stb');
    assert.equal(p.command('filterPreserved'),1);
    assert.deepEqual(p.stats.uploads.at(-1).pixels.slice(0,4),[17,34,51,68]);
    assert.equal(p.command('pixelException'),1);
    assert.equal(p.command('doublePrepare'),1);
    assert.equal(p.stats.decodes,4);
    p.command('dispose');
});

test('real AssetManager atlas dependencies opt in with explicit RGBA, retaining refcount/filter on reload', async () => {
    const atlas='native.png\nsize: 2, 2\nformat: RGBA8888\nfilter: Linear, Linear\nrepeat: none\ntile\n  bounds: 0, 0, 2, 2\n';
    const p=await start({assets:{'test.atlas':atlas}});
    p.loaded();p.runFrames();
    assert.equal(p.command('atlas','test.atlas'),1);
    assert.equal(p.stats.decodes,0);
    assert.equal(p.command('references'),1);
    p.command('filter','test.atlas');
    assert.equal(p.command('reload','test.atlas',1),1);
    assert.equal(p.stats.decodes,1);
    assert.equal(p.command('references'),1);
    assert.equal(p.command('filterPreserved','test.atlas'),1);
    p.command('dispose');
});

test('empty opt-in and unlisted files stay byte-compatible; supplied Pixmaps and custom handles are not intercepted', async () => {
    const empty=await start({paths:[]});empty.runFrames();
    assert.equal(empty.stats.images.length,0);
    assert.equal(empty.command('factory'),0);assert.equal(empty.stats.decodes,1);
    const p=await start();p.loaded();p.runFrames();
    assert.equal(p.command('factory','old.png'),0);
    assert.equal(p.command('customHandle'),0);
    assert.equal(p.command('suppliedPixmap'),0);
    assert.equal(p.stats.decodes,3);
    assert.equal(p.command('factory','native.png',1),1,'pixel readers do not consume the opt-in image');
    assert.equal(p.stats.decodes,3);
});

test('requested conversions, mipmaps including CPU generation, and POT copying keep the old path', async () => {
    const p=await start();p.loaded();p.runFrames();
    assert.equal(p.command('factory','native.png',2),0,'RGB888 conversion stays on stb');
    p.command('cpuMipmaps');
    assert.equal(p.command('factory','native.png',3),0);
    assert.equal(p.stats.mipmaps,0,'honor the CPU mipmap switch');
    assert.ok(p.stats.uploads.some(upload => upload.level===1));
    const npot=await start({assets:{'native.png':png({width:3,height:2})}});
    npot.loaded('native.png',{width:3,height:2});npot.runFrames();
    npot.command('potFallback','',1);
    assert.equal(npot.command('factory'),0);
    assert.equal(npot.command('width'),3,'factory retains its default (no forced POT copy)');
    assert.equal(npot.command('potData'),4,'direct backend FileTextureData still honors copyToPOT');
    assert.equal(npot.command('height'),2);
    npot.command('potFallback','',0);
});

test('eligible palette+tRNS and NPOT keep dimensions; grayscale, RGB and color-managed inputs stay old', async () => {
    for (const type of [0,2,4]) {
        const p=await start({assets:{'native.png':png({type})}});p.runFrames();
        assert.equal(p.stats.images.length,0);assert.equal(p.command('factory'),0);assert.equal(p.stats.decodes,1);
    }
    const gamma=await start({assets:{'native.png':png({gamma:true})}});gamma.runFrames();
    assert.equal(gamma.stats.images.length,0);assert.equal(gamma.command('factory'),0);
    const palette=await start({assets:{'native.png':png({type:3,width:3,height:2})}});
    palette.loaded('native.png',{width:3,height:2});palette.runFrames();
    assert.equal(palette.command('factory'),1);assert.equal(palette.command('width'),3);
    assert.equal(palette.command('height'),2);assert.equal(palette.stats.decodes,0);
});

for (const mode of ['error','timeout','size','create','src','upload','glError']) {
    test(`${mode} falls back once to stb and late callbacks cannot resurrect an image`, async () => {
        const p=await start({createThrows:mode==='create',srcThrows:mode==='src',
            uploadThrows:mode==='upload',uploadError:mode==='glError'?1282:0});
        const image=p.stats.images[0];
        if (mode==='error') image.onerror({});
        else if (mode==='timeout') p.timer(100);
        else if (mode==='size') p.loaded('native.png',{width:99,height:2});
        else if (mode==='upload'||mode==='glError') p.loaded();
        p.runFrames();
        assert.equal(p.stats.events.filter(e=>e==='create').length,1);
        assert.equal(p.command('factory'),mode==='upload'||mode==='glError'?1:0);
        assert.equal(p.stats.decodes,1);
        assert.deepEqual(p.stats.uploads.at(-1).pixels.slice(0,4),[17,34,51,68]);
        assert.equal(p.warnings().length,1);
        if (image) {
            image.lateLoad();image.lateError();
            assert.equal(image.onload,null);assert.equal(image.onerror,null);
        }
        assert.equal(p.warnings().length,1);
        assert.equal(p.timers.size,0);
    });
}

test('a failed old decoder is still an error, never a successful blank texture', async () => {
    const corrupt=png();corrupt.fill(0,41,corrupt.length-12); // damage IDAT, not the IHDR
    const p=await start({assets:{'native.png':corrupt}});
    if (p.stats.images.length) p.stats.images[0].onerror({});
    p.runFrames();
    assert.throws(()=>p.command('factory'));
});

test('manifest, package roots and renamed paths are authoritative; unused images have bounded ownership', async () => {
    const p=await start({rename:{'native.png':'native.png.txt'},packageRoot:'optional/test/'});
    p.loaded();p.runFrames();
    assert.equal(p.command('factory'),1);
    assert.equal(p.stats.uploads.at(-1).source,'optional/test/native.png.txt');
    const unavailable=await start({missing:true});unavailable.runFrames();
    assert.equal(unavailable.stats.images.length,0);
    assert.throws(()=>unavailable.command('factory'));
    const unused=await start();unused.loaded();unused.runFrames();unused.timer(5000);
    assert.equal(unused.timers.size,0);
    assert.equal(unused.command('factory'),0,'expired optimization falls back, never waits');
});

test('startup gate covers show/input/runnable requests; hidden completion and on-demand startup do not spin', async () => {
    const p=await start({paths:['native.png','old.png','native.png']});
    assert.equal(p.stats.images.length,2,'deduplicate requested files');
    p.command('continuous','',0);p.command('postRunnable');p.command('requestFrame');
    p.fire('show');p.fire('resize',{windowWidth:100,windowHeight:200});
    p.fire('touch',{changedTouches:[{identifier:1,clientX:10,clientY:10}]});
    assert.equal(p.frames.length,0,'no wakeup bypasses the startup gate');
    p.loaded();assert.equal(p.frames.length,0,'wait for all images');
    p.fire('hide');p.loaded('old.png');
    assert.equal(p.frames.length,0,'completion while hidden stays hidden');
    assert.deepEqual(p.stats.events,[],'no pause/resume before create');
    p.fire('show');p.runFrames();
    assert.deepEqual(p.stats.events,['create','resize','runnable','render']);
    assert.equal(p.frames.length,0,'noncontinuous startup stops after its requested frame');
    assert.ok(p.stats.delta < 0.1,'native decode wait is not a large first animation delta');
    p.fire('hide');p.fire('show');p.runFrames();
    assert.deepEqual(p.stats.events.slice(-3),['pause','resume','render']);
});
