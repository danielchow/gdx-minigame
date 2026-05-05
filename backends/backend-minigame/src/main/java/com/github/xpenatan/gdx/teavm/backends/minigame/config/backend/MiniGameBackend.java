package com.github.xpenatan.gdx.teavm.backends.minigame.config.backend;

import com.badlogic.gdx.files.FileHandle;
import com.github.xpenatan.gdx.teavm.backends.minigame.config.MiniGameBuildConfiguration;
import com.github.xpenatan.gdx.teavm.backends.shared.config.AssetFilter;
import com.github.xpenatan.gdx.teavm.backends.shared.config.AssetsCopy;
import com.github.xpenatan.gdx.teavm.backends.shared.config.compiler.TeaBackend;
import com.github.xpenatan.gdx.teavm.backends.shared.config.compiler.TeaCompilerData;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.teavm.tooling.TeaVMTargetType;

/**
 * Build backend for WeChat Mini Game.
 * Generates game.js, game.json, project.config.json, adapter.js, and first-screen.js
 * with a subpackages/ directory layout for parallel loading.
 */
public class MiniGameBackend extends TeaBackend {

    public String appId = "";
    public String orientation = "portrait";
    public String description = "WeChat Mini Game";
    public String targetFileName = "app";
    public String mainClassArgs = "";
    public AssetFilter scriptFilter;

    public MiniGameBackend setAppId(String appId) {
        this.appId = appId;
        return this;
    }

    public MiniGameBackend setOrientation(String orientation) {
        this.orientation = orientation;
        return this;
    }

    public MiniGameBackend setDescription(String description) {
        this.description = description;
        return this;
    }

    public MiniGameBackend setTargetFileName(String targetFileName) {
        this.targetFileName = targetFileName;
        return this;
    }

    public MiniGameBackend setMainClassArgs(String mainClassArgs) {
        this.mainClassArgs = mainClassArgs;
        return this;
    }

    public MiniGameBackend setScriptFilter(AssetFilter scriptFilter) {
        this.scriptFilter = scriptFilter;
        return this;
    }

    @Override
    protected void setup(TeaCompilerData data) {
        targetType = TeaVMTargetType.JAVASCRIPT;
        tool.setTargetFileName(data.outputName + ".js");

        if(data.releasePath != null) {
            releasePath = new FileHandle(data.releasePath.getAbsolutePath().replace("\\", "/"));
        } else {
            releasePath = new FileHandle(new File(data.output, "minigame").getAbsolutePath().replace("\\", "/"));
        }
        tool.setTargetDirectory(releasePath.file());
        setupMinigame(data);
    }

    @Override
    protected void build(TeaCompilerData data) {
        super.build(data);
        // Post-build steps (TeaVM output now exists)
        fixFreetypeModuleScoping();
        // moveJsFilesToRoot() removed — JS files stay in subpackages/engine/

        // Move TeaVM output (app.js) to game subpackage — can't do this in copyAssets()
        // because TeaVM hasn't produced the file yet at that point
        FileHandle gameFolder = releasePath.child("subpackages/game");
        gameFolder.mkdirs();
        FileHandle appJs = releasePath.child(targetFileName + ".js");
        if (appJs.exists()) {
            appJs.moveTo(gameFolder.child(targetFileName + ".js"));
            System.out.println("[MiniGameBackend] Moved " + targetFileName + ".js to subpackages/game/");

            // Insert Module declaration after "use strict" so TeaVM's strict-mode code can access
            // the freetype Module object set by freetype.js via globalThis.Module
            FileHandle movedAppJs = gameFolder.child(targetFileName + ".js");
            String appContent = movedAppJs.readString();
            String moduleDecl = "var Module=typeof globalThis!==\"undefined\"&&globalThis.Module?globalThis.Module:{};\n";
            // Insert after the "use strict"; directive (first line)
            if (appContent.startsWith("\"use strict\";")) {
                appContent = "\"use strict\";\n" + moduleDecl + appContent.substring("\"use strict\";\n".length());
            } else {
                appContent = moduleDecl + appContent;
            }
            movedAppJs.writeString(appContent, false);
            System.out.println("[MiniGameBackend] Inserted Module declaration into " + targetFileName + ".js");
        }
    }

    private void setupMinigame(TeaCompilerData data) {
        if (appId == null || appId.isEmpty()) {
            System.out.println("[MiniGameBackend] WARNING: appId is empty. Set via setAppId() for production builds.");
        }
        generateGameJson();
        generateProjectConfigJson();
        generateGameJs(data);
        generateAdapter();
        generateFirstScreen();
    }

    /**
     * Generate game.json — WeChat Mini Game configuration.
     */
    private void generateGameJson() {
        String json = "{\n" +
            "    \"deviceOrientation\": \"" + orientation + "\",\n" +
            "    \"networkTimeout\": {\n" +
            "        \"request\": 5000,\n" +
            "        \"connectSocket\": 5000,\n" +
            "        \"uploadFile\": 5000,\n" +
            "        \"downloadFile\": 500000\n" +
            "    },\n" +
            "    \"subpackages\": [\n" +
            "        { \"name\": \"engine\", \"root\": \"subpackages/engine/\" },\n" +
            "        { \"name\": \"game\", \"root\": \"subpackages/game/\" },\n" +
            "        { \"name\": \"assets\", \"root\": \"assets/\" }\n" +
            "    ]\n" +
            "}\n";

        FileHandle gameJson = releasePath.child("game.json");
        gameJson.writeString(json, false);
    }

    /**
     * Generate project.config.json — WeChat DevTools project config.
     */
    private void generateProjectConfigJson() {
        String json = "{\n" +
            "    \"description\": \"" + description + "\",\n" +
            "    \"setting\": {\n" +
            "        \"urlCheck\": false,\n" +
            "        \"es6\": true\n" +
            "    },\n" +
            "    \"compileType\": \"game\",\n" +
            "    \"appid\": \"" + appId + "\",\n" +
            "    \"libVersion\": \"3.12.1\",\n" +
            "    \"packOptions\": {\n" +
            "        \"ignore\": []\n" +
            "    }\n" +
            "}\n";

        FileHandle projectConfig = releasePath.child("project.config.json");
        projectConfig.writeString(json, false);
    }

    /**
     * Generate game.js — Main package entry point.
     * Uses parallel subpackage loading with WebGL splash screen.
     */
    private void generateGameJs(TeaCompilerData data) {
        String js = "// game.js — WeChat Mini Game entry point\n" +
            "// Generated by MiniGameBackend\n\n" +
            "console.log('[game.js] Requiring adapter...');\n" +
            "var adapter = require('./adapter.js');\n" +
            "var firstScreen = require('./first-screen');\n" +
            "console.log('[game.js] Adapter and first-screen loaded');\n\n" +
            "// Create single canvas for WebGL\n" +
            "var canvas = wx.createCanvas();\n" +
            "adapter.setup(canvas);\n" +
            "firstScreen.start(canvas);\n" +
            "console.log('[game.js] Canvas created and splash drawn', canvas.width, 'x', canvas.height);\n\n" +
            "// Helper: load subpackage with error handling\n" +
            "function loadSubpackage(name) {\n" +
            "    if (typeof wx.loadSubpackage !== 'function') {\n" +
            "        console.log('[game.js] wx.loadSubpackage not available, skipping ' + name);\n" +
            "        return Promise.resolve();\n" +
            "    }\n" +
            "    return new Promise(function(resolve) {\n" +
            "        wx.loadSubpackage({\n" +
            "            name: name,\n" +
            "            success: function() {\n" +
            "                console.log('[game.js] Subpackage \"' + name + '\" loaded');\n" +
            "                resolve();\n" +
            "            },\n" +
            "            fail: function(err) {\n" +
            "                console.log('[game.js] Subpackage \"' + name + '\" load issue:', err.errMsg || err);\n" +
            "                resolve();\n" +
            "            }\n" +
            "        });\n" +
            "    });\n" +
            "}\n\n" +
            "async function startGame() {\n" +
            "    try {\n" +
            "        // Phase 1: Load engine and assets in parallel\n" +
            "        console.log('[game.js] Loading engine and assets in parallel...');\n" +
            "        await Promise.all([\n" +
            "            loadSubpackage('engine'),\n" +
            "            loadSubpackage('assets')\n" +
            "        ]);\n" +
            "        console.log('[game.js] Engine and assets subpackages loaded');\n\n" +
            "        // Phase 2: Preload assets from filesystem into memory\n" +
            "        console.log('[game.js] Preloading assets...');\n" +
            "        await adapter.preloadAssets(function(loaded, total) {\n" +
            "            firstScreen.setProgress(loaded / total);\n" +
            "        });\n" +
            "        console.log('[game.js] Assets preloaded, count:', globalThis.__preloadedAssets ? Object.keys(globalThis.__preloadedAssets).length : 0);\n\n" +
            "        // Phase 3: Wait for engine to initialize (Gdx bridge set by gdx.wasm.js)\n" +
            "        console.log('[game.js] Waiting for engine initialization...');\n" +
            "        var gdxWaitStart = Date.now();\n" +
            "        while (typeof window.Gdx === 'undefined') {\n" +
            "            if (Date.now() - gdxWaitStart > 30000) {\n" +
            "                throw new Error('Timed out waiting for Gdx to initialize (30s)');\n" +
            "            }\n" +
            "            await new Promise(function(r) { setTimeout(r, 50); });\n" +
            "        }\n" +
            "        console.log('[game.js] window.Gdx available, type:', typeof window.Gdx);\n\n" +
            "        // Phase 4: Load game code subpackage\n" +
            "        console.log('[game.js] Loading game code subpackage...');\n" +
            "        await loadSubpackage('game');\n" +
            "        console.log('[game.js] Game code subpackage loaded');\n\n" +
            "        // Phase 5: Start the compiled game via globalThis bridge\n" +
            "        var mainModule = globalThis.__gameApp;\n" +
            "        if (mainModule && mainModule.main) {\n" +
            "            console.log('[game.js] Calling main()...');\n" +
            "            mainModule.main([" + mainClassArgs + "]);\n" +
            "            console.log('[game.js] main() returned');\n" +
            "        } else {\n" +
            "            console.error('[game.js] ERROR: __gameApp not available, cannot call main()');\n" +
            "        }\n\n" +
            "        // Redraw splash to bridge canvas resize gap\n" +
            "        firstScreen.redraw();\n" +
            "    } catch (err) {\n" +
            "        console.error('[game.js] FAILED:', err);\n" +
            "    }\n" +
            "}\n\n" +
            "startGame();\n";

        FileHandle gameJs = releasePath.child("game.js");
        gameJs.writeString(js, false);
    }

    /**
     * Generate adapter.js — Browser polyfills for WeChat environment.
     * Flat file at root level (was adapter/index.js).
     * Includes real-device polyfills for window, document, performance.
     */
    private void generateAdapter() {
        String js = "// adapter.js — Browser polyfills for WeChat Mini Game\n" +
            "// Generated by MiniGameBackend\n\n" +
            "console.log('[adapter] Starting adapter initialization');\n" +
            "// === console polyfill (must be first — other polyfills use console.log) ===\n" +
            "if (typeof globalThis.console === 'undefined') {\n" +
            "    globalThis.console = { log: function(){}, error: function(){}, warn: function(){} };\n" +
            "}\n\n" +
            "// === window polyfill ===\n" +
            "// WeChat provides globalThis.window as a read-only getter on some platforms,\n" +
            "// but on real devices window may be completely undefined.\n" +
            "(function() {\n" +
            "    var win = (typeof window !== 'undefined') ? window : globalThis.window;\n" +
            "    if (!win) {\n" +
            "        win = globalThis;\n" +
            "        globalThis.window = globalThis;\n" +
            "        console.log('[adapter] window was undefined, created as globalThis alias');\n" +
            "    } else {\n" +
            "        console.log('[adapter] win exists, typeof win:', typeof win);\n" +
            "    }\n" +
            "    if (typeof win.requestAnimationFrame === 'undefined') {\n" +
            "        win.requestAnimationFrame = function(cb) { return setTimeout(cb, 16); };\n" +
            "    }\n" +
            "    if (typeof win.cancelAnimationFrame === 'undefined') {\n" +
            "        win.cancelAnimationFrame = function(id) { clearTimeout(id); };\n" +
            "    }\n" +
            "    if (typeof win.devicePixelRatio === 'undefined') {\n" +
            "        win.devicePixelRatio = wx.getSystemInfoSync().pixelRatio;\n" +
            "    }\n" +
            "    if (typeof win.innerWidth === 'undefined') {\n" +
            "        win.innerWidth = wx.getSystemInfoSync().screenWidth;\n" +
            "    }\n" +
            "    if (typeof win.innerHeight === 'undefined') {\n" +
            "        win.innerHeight = wx.getSystemInfoSync().screenHeight;\n" +
            "    }\n" +
            "    if (typeof win.addEventListener === 'undefined') {\n" +
            "        win.addEventListener = function() {};\n" +
            "    }\n" +
            "    if (typeof win.removeEventListener === 'undefined') {\n" +
            "        win.removeEventListener = function() {};\n" +
            "    }\n" +
            "    if (typeof win.location === 'undefined') {\n" +
            "        win.location = { href: './' };\n" +
            "    }\n" +
            "    if (!('Gdx' in win)) {\n" +
            "        win.Gdx = undefined;\n" +
            "    }\n" +
            "})();\n" +
            "console.log('[adapter] window polyfill done');\n\n" +
            "// === document polyfill ===\n" +
            "if (typeof document === 'undefined') {\n" +
            "    globalThis.document = {\n" +
            "        createElement: function(tag) {\n" +
            "            if (tag === 'canvas') return wx.createCanvas();\n" +
            "            return { style: {}, appendChild: function(){}, removeChild: function(){} };\n" +
            "        },\n" +
            "        getElementById: function() { return null; },\n" +
            "        addEventListener: function() {},\n" +
            "        removeEventListener: function() {},\n" +
            "        body: { appendChild: function(){}, removeChild: function(){} },\n" +
            "        head: { appendChild: function(){} },\n" +
            "        documentElement: { style: {} },\n" +
            "        querySelector: function() { return null; },\n" +
            "        querySelectorAll: function() { return []; }\n" +
            "    };\n" +
            "    console.log('[adapter] document polyfill created');\n" +
            "}\n\n" +
            "// === performance polyfill ===\n" +
            "if (typeof performance === 'undefined') {\n" +
            "    globalThis.performance = { now: Date.now };\n" +
            "    console.log('[adapter] performance polyfill created');\n" +
            "}\n\n" +
            "// === WebAssembly patch ===\n" +
            "if (typeof WXWebAssembly !== 'undefined') {\n" +
            "    if (typeof WebAssembly === 'undefined') {\n" +
            "        globalThis.WebAssembly = {};\n" +
            "    }\n" +
            "    var originalInstantiate = WebAssembly.instantiate;\n" +
            "    console.log('[adapter] WXWebAssembly detected, patching WebAssembly.instantiate');\n" +
            "    WebAssembly.instantiate = function(source, imports) {\n" +
            "        console.log('[adapter] WebAssembly.instantiate called, source type:', typeof source);\n" +
            "        if (source instanceof ArrayBuffer || source instanceof Uint8Array) {\n" +
            "            console.log('[adapter] Redirecting to WXWebAssembly.instantiate(\"subpackages/engine/gdx.wasm\")');\n" +
            "            return WXWebAssembly.instantiate('subpackages/engine/gdx.wasm', imports);\n" +
            "        }\n" +
            "        console.log('[adapter] Falling through to originalInstantiate');\n" +
            "        return originalInstantiate ? originalInstantiate(source, imports) : Promise.reject(new Error('No WASM support'));\n" +
            "    };\n" +
            "}\n\n" +
            "// === atob polyfill ===\n" +
            "if (typeof atob === 'undefined') {\n" +
            "    var atob = function(base64) {\n" +
            "        var chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/';\n" +
            "        var lookup = {};\n" +
            "        for (var i = 0; i < chars.length; i++) lookup[chars[i]] = i;\n" +
            "        var output = '';\n" +
            "        var buffer = 0, bufferLen = 0;\n" +
            "        for (var j = 0; j < base64.length; j++) {\n" +
            "            var ch = base64.charAt(j);\n" +
            "            if (ch === '=') break;\n" +
            "            buffer = (buffer << 6) | lookup[ch];\n" +
            "            bufferLen += 6;\n" +
            "            if (bufferLen >= 8) {\n" +
            "                bufferLen -= 8;\n" +
            "                output += String.fromCharCode((buffer >> bufferLen) & 0xFF);\n" +
            "            }\n" +
            "        }\n" +
            "        return output;\n" +
            "    };\n" +
            "    globalThis.atob = atob;\n" +
            "}\n\n" +
            "console.log('[adapter] Defining preloadAssets and exports.setup');\n" +
            "// === Asset preloader ===\n" +
            "var __preloadedAssets = {};\n" +
            "globalThis.__preloadedAssets = __preloadedAssets;\n\n" +
            "exports.preloadAssets = async function(onProgress) {\n" +
            "    try {\n" +
            "        var fs = wx.getFileSystemManager();\n" +
            "        var renameMap = {};\n" +
            "        try {\n" +
            "            var mapData = await new Promise(function(resolve, reject) {\n" +
            "                fs.readFile({\n" +
            "                    filePath: 'assets/rename-map.json',\n" +
            "                    encoding: 'utf-8',\n" +
            "                    success: function(res) { resolve(res.data); },\n" +
            "                    fail: function(err) { reject(err); }\n" +
            "                });\n" +
            "            });\n" +
            "            renameMap = JSON.parse(mapData);\n" +
            "            console.log('[adapter] Loaded rename map, entries:', Object.keys(renameMap).length);\n" +
            "        } catch(e) {\n" +
            "            console.log('[adapter] No rename-map.json found, proceeding without renaming');\n" +
            "        }\n" +
            "        var manifest = '';\n" +
            "        try {\n" +
            "            manifest = await new Promise(function(resolve, reject) {\n" +
            "                fs.readFile({\n" +
            "                    filePath: 'assets/preload.txt',\n" +
            "                    encoding: 'utf-8',\n" +
            "                    success: function(res) { resolve(res.data); },\n" +
            "                    fail: function(err) { reject(err); }\n" +
            "                });\n" +
            "            });\n" +
            "        } catch(e) {\n" +
            "            console.log('[adapter] preloadAssets: No manifest file, skipping');\n" +
            "            return;\n" +
            "        }\n" +
            "        var files = [];\n" +
            "        var lines = manifest.split('\\n');\n" +
            "        for (var i = 0; i < lines.length; i++) {\n" +
            "            var line = lines[i].trim();\n" +
            "            if (!line) continue;\n" +
            "            var tokens = line.split(':');\n" +
            "            if (tokens.length < 4) continue;\n" +
            "            var assetType = tokens[1];\n" +
            "            var assetPath = tokens[2];\n" +
            "            if (!assetPath) continue;\n" +
            "            if (assetType === 'd') {\n" +
            "                __preloadedAssets[assetPath] = { type: 'dir', data: null };\n" +
            "            } else {\n" +
            "                files.push(assetPath);\n" +
            "            }\n" +
            "        }\n" +
            "        var BATCH = 10;\n" +
            "        for (var i = 0; i < files.length; i += BATCH) {\n" +
            "            var batch = files.slice(i, Math.min(i + BATCH, files.length));\n" +
            "            var promises = batch.map(function(assetPath) {\n" +
            "                return new Promise(function(resolve) {\n" +
            "                    var readPath = renameMap[assetPath] || assetPath;\n" +
            "                    fs.readFile({\n" +
            "                        filePath: 'assets' + readPath,\n" +
            "                        success: function(res) {\n" +
            "                            __preloadedAssets[assetPath] = { type: 'file', data: res.data };\n" +
            "                            resolve();\n" +
            "                        },\n" +
            "                        fail: function(err) {\n" +
            "                            console.error('[adapter] Failed:', assetPath, err.errMsg);\n" +
            "                            __preloadedAssets[assetPath] = { type: 'file', data: new ArrayBuffer(0) };\n" +
            "                            resolve();\n" +
            "                        }\n" +
            "                    });\n" +
            "                });\n" +
            "            });\n" +
            "            await Promise.all(promises);\n" +
            "            if (onProgress) onProgress(Math.min(i + BATCH, files.length), files.length);\n" +
            "            await new Promise(function(r) { globalThis.requestAnimationFrame(r); });\n" +
            "        }\n" +
            "        console.log('[adapter] preloadAssets complete, assets:', Object.keys(__preloadedAssets).length);\n" +
            "    } catch(e) {\n" +
            "        console.error('[adapter] preloadAssets error:', e);\n" +
            "    }\n" +
            "};\n\n" +
            "exports.setup = function(canvas) {\n" +
            "    globalThis.canvas = canvas;\n" +
            "};\n\n" +
            "// === XMLHttpRequest polyfill (wraps wx.request) ===\n" +
            "if (typeof XMLHttpRequest === 'undefined') {\n" +
            "    var XHR = function() {\n" +
            "        this.readyState = 0;\n" +
            "        this.status = 0;\n" +
            "        this.statusText = '';\n" +
            "        this.responseText = '';\n" +
            "        this.response = '';\n" +
            "        this.responseType = '';\n" +
            "        this.responseHeaders = {};\n" +
            "        this._method = '';\n" +
            "        this._url = '';\n" +
            "        this._headers = {};\n" +
            "        this._async = true;\n" +
            "        this.onreadystatechange = null;\n" +
            "        this.onerror = null;\n" +
            "        this.onload = null;\n" +
            "    };\n" +
            "    XHR.UNSENT = 0;\n" +
            "    XHR.OPENED = 1;\n" +
            "    XHR.HEADERS_RECEIVED = 2;\n" +
            "    XHR.LOADING = 3;\n" +
            "    XHR.DONE = 4;\n" +
            "    XHR.prototype.open = function(method, url, async) {\n" +
            "        this._method = method;\n" +
            "        this._url = url;\n" +
            "        this._async = (async !== false);\n" +
            "        this.readyState = XHR.OPENED;\n" +
            "    };\n" +
            "    XHR.prototype.setRequestHeader = function(name, value) {\n" +
            "        this._headers[name] = value;\n" +
            "    };\n" +
            "    XHR.prototype.send = function(data) {\n" +
            "        var self = this;\n" +
            "        var opts = {\n" +
            "            url: self._url,\n" +
            "            method: self._method,\n" +
            "            header: self._headers,\n" +
            "            data: (data !== undefined && data !== null) ? data : undefined,\n" +
            "            dataType: 'text',\n" +
            "            success: function(res) {\n" +
            "                self.status = res.statusCode;\n" +
            "                self.statusText = '';\n" +
            "                self.responseText = (typeof res.data === 'string') ? res.data : JSON.stringify(res.data);\n" +
            "                self.response = self.responseText;\n" +
            "                self.responseHeaders = res.header || {};\n" +
            "                self.readyState = XHR.HEADERS_RECEIVED;\n" +
            "                if (self.onreadystatechange) self.onreadystatechange();\n" +
            "                self.readyState = XHR.LOADING;\n" +
            "                if (self.onreadystatechange) self.onreadystatechange();\n" +
            "                self.readyState = XHR.DONE;\n" +
            "                if (self.onreadystatechange) self.onreadystatechange();\n" +
            "                if (self.onload) self.onload();\n" +
            "            },\n" +
            "            fail: function(err) {\n" +
            "                self.status = 0;\n" +
            "                self.readyState = XHR.DONE;\n" +
            "                if (self.onreadystatechange) self.onreadystatechange();\n" +
            "                if (self.onerror) self.onerror(err);\n" +
            "            }\n" +
            "        };\n" +
            "        wx.request(opts);\n" +
            "    };\n" +
            "    XHR.prototype.abort = function() {};\n" +
            "    XHR.prototype.getAllResponseHeaders = function() {\n" +
            "        var result = '';\n" +
            "        for (var k in this.responseHeaders) {\n" +
            "            if (this.responseHeaders.hasOwnProperty(k)) {\n" +
            "                result += k + ': ' + this.responseHeaders[k] + '\\r\\n';\n" +
            "            }\n" +
            "        }\n" +
            "        return result;\n" +
            "    };\n" +
            "    XHR.prototype.getResponseHeader = function(name) {\n" +
            "        return this.responseHeaders[name] || null;\n" +
            "    };\n" +
            "    XHR.prototype.overrideMimeType = function() {};\n" +
            "    globalThis.XMLHttpRequest = XHR;\n" +
            "}\n" +
            "console.log('[adapter] Adapter fully loaded');\n";

        FileHandle adapterJs = releasePath.child("adapter.js");
        adapterJs.writeString(js, false);
    }

    /**
     * Generate first-screen.js — WebGL splash screen for loading.
     * Shows a colored rectangle via scissor+clear to avoid black screen.
     */
    private void generateFirstScreen() {
        String js = "// first-screen.js — WebGL splash screen for WeChat Mini Game\n" +
            "// Shows a colored rectangle during loading to avoid black screen\n\n" +
            "var GL = null;\n" +
            "var program = null;\n" +
            "var canvas = null;\n\n" +
            "exports.start = function(c) {\n" +
            "    canvas = c;\n" +
            "    try {\n" +
            "        GL = canvas.getContext('webgl');\n" +
            "        if (!GL) {\n" +
            "            console.log('[first-screen] WebGL not available, skipping splash');\n" +
            "            return;\n" +
            "        }\n" +
            "        GL.viewport(0, 0, canvas.width, canvas.height);\n" +
            "        GL.scissor(0, 0, canvas.width, canvas.height);\n" +
            "        GL.enable(GL.SCISSOR_TEST);\n" +
            "        GL.clearColor(0.1, 0.1, 0.15, 1.0);\n" +
            "        GL.clear(GL.COLOR_BUFFER_BIT);\n" +
            "        console.log('[first-screen] Splash drawn', canvas.width, 'x', canvas.height);\n" +
            "    } catch(e) {\n" +
            "        console.log('[first-screen] Splash failed:', e.message);\n" +
            "    }\n" +
            "};\n\n" +
            "exports.setProgress = function(ratio) {\n" +
            "    if (!GL) return;\n" +
            "    try {\n" +
            "        var w = canvas.width * ratio;\n" +
            "        GL.scissor(0, 0, Math.floor(w), canvas.height);\n" +
            "        GL.clearColor(0.2, 0.5, 0.8, 1.0);\n" +
            "        GL.clear(GL.COLOR_BUFFER_BIT);\n" +
            "        // Reset scissor for remaining area\n" +
            "        GL.scissor(Math.floor(w), 0, canvas.width - Math.floor(w), canvas.height);\n" +
            "        GL.clearColor(0.1, 0.1, 0.15, 1.0);\n" +
            "        GL.clear(GL.COLOR_BUFFER_BIT);\n" +
            "        GL.scissor(0, 0, canvas.width, canvas.height);\n" +
            "    } catch(e) {}\n" +
            "};\n\n" +
            "exports.redraw = function() {\n" +
            "    if (!GL) return;\n" +
            "    try {\n" +
            "        GL.scissor(0, 0, canvas.width, canvas.height);\n" +
            "        GL.clearColor(0.1, 0.1, 0.15, 1.0);\n" +
            "        GL.clear(GL.COLOR_BUFFER_BIT);\n" +
            "    } catch(e) {}\n" +
            "};\n\n" +
            "exports.end = function() {\n" +
            "    if (!GL) return;\n" +
            "    try {\n" +
            "        GL.disable(GL.SCISSOR_TEST);\n" +
            "    } catch(e) {}\n" +
            "    GL = null;\n" +
            "    program = null;\n" +
            "};\n";

        FileHandle firstScreenJs = releasePath.child("first-screen.js");
        firstScreenJs.writeString(js, false);
    }

    /**
     * Generate subpackage entry files — bridge entries for each subpackage.
     * WeChat requires <root>/game.js as the subpackage entry point.
     */
    private void generateSubpackageEntryFiles() {
        // Engine subpackage entry: requires freetype.js and gdx.wasm.js locally
        String engineEntry =
            "// Engine subpackage entry — auto-executed by wx.loadSubpackage({name: 'engine'})\n" +
            "// Requires freetype.js and gdx.wasm.js locally from within the subpackage\n\n" +
            "console.log('[engine] game.js executing');\n" +
            "try {\n" +
            "    var freetype = require('./freetype.js');\n" +
            "    globalThis.__engineExports = globalThis.__engineExports || {};\n" +
            "    globalThis.__engineExports['freetype'] = freetype;\n" +
            "    console.log('[engine] freetype.js loaded, exports:', typeof freetype);\n" +
            "} catch(e) { console.error('[engine] freetype.js require failed:', e.message); }\n\n" +
            "try {\n" +
            "    var wasmModule = require('./gdx.wasm.js');\n" +
            "    console.log('[engine] gdx.wasm.js loaded');\n" +
            "} catch(e) { console.error('[engine] gdx.wasm.js require failed:', e.message); }\n\n" +
            "globalThis.__engineReady = true;\n" +
            "console.log('[engine] game.js done');\n";

        // Game code subpackage entry: requires the TeaVM output file locally
        String gameEntry =
            "// Game code subpackage entry — auto-executed by wx.loadSubpackage({name: 'game'})\n\n" +
            "console.log('[game-code] game.js executing');\n" +
            "try {\n" +
            "    var app = require('./" + targetFileName + ".js');\n" +
            "    globalThis.__gameApp = app;\n" +
            "    console.log('[game-code] " + targetFileName + ".js loaded, main:', typeof app.main);\n" +
            "} catch(e) { console.error('[game-code] " + targetFileName + ".js require failed:', e.message); }\n\n" +
            "globalThis.__gameCodeReady = true;\n" +
            "console.log('[game-code] game.js done');\n";

        // Assets subpackage entry: simple stub
        String assetsEntry =
            "// Assets subpackage entry — loaded by wx.loadSubpackage({name: 'assets'})\n" +
            "// Files are read by adapter.preloadAssets() via wx.getFileSystemManager()\n" +
            "globalThis.__assetsReady = true;\n";

        // Write subpackage entry files
        releasePath.child("subpackages/engine").mkdirs();
        releasePath.child("subpackages/game").mkdirs();
        releasePath.child("subpackages/engine/game.js").writeString(engineEntry, false);
        releasePath.child("subpackages/game/game.js").writeString(gameEntry, false);
        releasePath.child("assets/game.js").writeString(assetsEntry, false);
    }

    /**
     * Fix freetype.js Module scoping — inject globalThis.Module = Module
     * so that compiled code can access it.
     */
    private void fixFreetypeModuleScoping() {
        FileHandle engineFolder = releasePath.child("subpackages/engine");
        FileHandle freetypeJs = engineFolder.child("freetype.js");
        if (freetypeJs.exists()) {
            try {
                String content = freetypeJs.readString();
                content = content.replace(
                    "Module[\"noExitRuntime\"]=true;",
                    "Module[\"noExitRuntime\"]=true;if(typeof globalThis!==\"undefined\"){globalThis.Module=Module}"
                );
                freetypeJs.writeString(content, false);
            } catch (Exception e) {
                // Non-critical, continue
            }
        }
    }

    /**
     * Extract embedded WASM binary from gdx.wasm.js and write it as subpackages/engine/gdx.wasm.
     * The JS file contains base64 WASM data in a data URL like:
     *   O="data:application/octet-stream;base64,AGFzbQ..."
     * We parse this out, decode the base64, and write the raw bytes as a .wasm file
     * so WXWebAssembly.instantiate('subpackages/engine/gdx.wasm', imports) can find it.
     */
    private void extractWasmFromJs() {
        // gdx.wasm.js lives in subpackages/engine/
        FileHandle wasmJs = releasePath.child("subpackages/engine/gdx.wasm.js");
        if (!wasmJs.exists()) {
            System.out.println("[MiniGameBackend] WARNING: gdx.wasm.js not found, skipping WASM extraction");
            return;
        }
        try {
            String content = wasmJs.readString();
            Pattern pattern = Pattern.compile("\"data:application/octet-stream;base64,([A-Za-z0-9+/=]+)\"");
            Matcher matcher = pattern.matcher(content);
            if (!matcher.find()) {
                System.out.println("[MiniGameBackend] WARNING: Could not find base64 WASM data in gdx.wasm.js");
                return;
            }
            String base64Data = matcher.group(1);
            byte[] wasmBytes = Base64.getDecoder().decode(base64Data);
            // Write the .wasm binary to subpackages/engine/ (WXWebAssembly reads from filesystem)
            FileHandle engineFolder = releasePath.child("subpackages/engine");
            FileHandle wasmFile = engineFolder.child("gdx.wasm");
            wasmFile.writeBytes(wasmBytes, false);
            System.out.println("[MiniGameBackend] Extracted gdx.wasm (" + wasmBytes.length + " bytes) from gdx.wasm.js");
        } catch (Exception e) {
            System.out.println("[MiniGameBackend] ERROR: Failed to extract WASM from gdx.wasm.js: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void copyAssets(TeaCompilerData data) {
        super.copyAssets(data);

        // Clean up old directory structure from incremental builds
        FileHandle oldScripts = releasePath.child("scripts");
        if (oldScripts.exists()) {
            oldScripts.deleteDirectory();
            System.out.println("[MiniGameBackend] Cleaned up old scripts/ directory");
        }
        FileHandle oldAdapter = releasePath.child("adapter");
        if (oldAdapter.exists()) {
            oldAdapter.deleteDirectory();
            System.out.println("[MiniGameBackend] Cleaned up old adapter/ directory");
        }
        // Clean up old root-level JS files from previous build layout
        String[] oldRootJs = {"freetype.js", "gdx.wasm.js", "app.js"};
        for (String js : oldRootJs) {
            FileHandle oldFile = releasePath.child(js);
            if (oldFile.exists()) {
                oldFile.delete();
                System.out.println("[MiniGameBackend] Cleaned up old root-level " + js);
            }
        }

        // Engine subpackage: scripts → subpackages/engine/
        FileHandle engineFolder = releasePath.child("subpackages/engine");
        engineFolder.mkdirs();
        AssetsCopy.copyResources(classLoader, scripts, scriptFilter, engineFolder);
        // gdx.wasm.js stays in engine folder — NO move to root

        // Generate subpackage entry files required by WeChat
        generateSubpackageEntryFiles();

        // Extract WASM binary from embedded base64 in gdx.wasm.js
        extractWasmFromJs();

        // Note: app.js move happens in build() post-build step (TeaVM output doesn't exist yet here)

        // Rename files with blocked extensions for WeChat readFileSync compatibility
        renameBlockedExtensions();
    }

    /**
     * Rename files with extensions that WeChat's readFileSync blocks (permission denied).
     * Appends .txt to blocked extensions and generates a rename-map.json for runtime lookup.
     */
    private void renameBlockedExtensions() {
        Set<String> allowedExts = new HashSet<>(Arrays.asList(
            // WeChat Mini Game official whitelist (69 extensions)
            // Source: https://developers.weixin.qq.com/minigame/dev/guide/base-ability/code-package.html
            "png", "jpg", "jpeg", "gif", "svg", "js", "json", "cer", "obj", "dae",
            "fbx", "mtl", "stl", "3ds", "mp3", "pvr", "wav", "plist", "ttf", "fnt",
            "gz", "ccz", "m4a", "mp4", "bmp", "atlas", "swf", "ani", "part", "proto",
            "bin", "sk", "mipmaps", "txt", "zip", "tt", "map", "ogg", "silk", "dbbin",
            "dbmv", "etc", "lmat", "lm", "ls", "lh", "lani", "lav", "lsani", "ltc",
            "aac", "astc", "br", "csv", "cur", "dat", "dds", "glb", "gltf", "ico",
            "ktx", "lmani", "lml", "pkm", "prefab", "scene", "skel", "wasm", "xml"
        ));
        FileHandle assetsDir = releasePath.child("assets");
        if (!assetsDir.exists()) return;

        // Build rename map
        StringBuilder json = new StringBuilder("{\n");
        int[] count = {0};
        renameFilesRecursive(assetsDir, assetsDir, allowedExts, json, count);

        if (count[0] > 0) {
            // Remove trailing comma
            int lastComma = json.lastIndexOf(",");
            if (lastComma > 0) json.deleteCharAt(lastComma);
            json.append("}\n");
            FileHandle mapFile = assetsDir.child("rename-map.json");
            mapFile.writeString(json.toString(), false);
            System.out.println("[MiniGameBackend] Generated rename-map.json with " + count[0] + " entries");
        }
    }

    private void renameFilesRecursive(FileHandle dir, FileHandle root, Set<String> allowedExts, StringBuilder json, int[] count) {
        FileHandle[] children = dir.list();
        if (children == null) return;
        for (FileHandle child : children) {
            if (child.isDirectory()) {
                renameFilesRecursive(child, root, allowedExts, json, count);
            } else {
                String name = child.name();
                // Skip metadata files and dotfiles
                if (name.equals("rename-map.json") || name.equals("preload.txt") || name.startsWith(".")) continue;
                String ext = child.extension().toLowerCase();
                if (!allowedExts.contains(ext)) {
                    String relativePath = child.path().substring(root.path().length() + 1).replace('\\', '/');
                    String newName = name + ".txt";
                    FileHandle renamed = child.sibling(newName);
                    // Delete target if exists (incremental builds)
                    if (renamed.exists()) renamed.delete();
                    child.moveTo(renamed);
                    String renamedRelative = renamed.path().substring(root.path().length() + 1).replace('\\', '/');
                    json.append("  \"/").append(relativePath).append("\": \"/").append(renamedRelative).append("\",\n");
                    count[0]++;
                }
            }
        }
    }
}
