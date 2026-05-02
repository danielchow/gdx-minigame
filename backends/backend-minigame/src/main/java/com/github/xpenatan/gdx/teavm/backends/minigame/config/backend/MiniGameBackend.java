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
 * Generates game.js, game.json, project.config.json, and adapter/index.js
 * instead of index.html + web.xml.
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
        // Post-build steps
        fixFreetypeModuleScoping();
    }

    private void setupMinigame(TeaCompilerData data) {
        if (appId == null || appId.isEmpty()) {
            System.out.println("[MiniGameBackend] WARNING: appId is empty. Set via setAppId() for production builds.");
        }
        generateGameJson();
        generateProjectConfigJson();
        generateGameJs(data);
        generateAdapter();
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
            "        { \"name\": \"engine\", \"root\": \"scripts/\" },\n" +
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
     */
    private void generateGameJs(TeaCompilerData data) {
        String js = "// game.js — WeChat Mini Game entry point\n" +
            "// Generated by MiniGameBackend\n\n" +
            "console.log('[game.js] Step 1: Requiring adapter...');\n" +
            "var adapter;\n" +
            "try {\n" +
            "    adapter = require('./adapter/index.js');\n" +
            "} catch(e) {\n" +
            "    console.error('[game.js] FATAL: adapter require failed:', e);\n" +
            "    throw e;\n" +
            "}\n" +
            "const { setup } = adapter;\n" +
            "console.log('[game.js] Step 2: Adapter loaded');\n\n" +
            "// Phase 1: Setup shims and canvas (synchronous)\n" +
            "const canvas = wx.createCanvas();\n" +
            "console.log('[game.js] Step 3: Canvas created', canvas.width, 'x', canvas.height);\n" +
            "setup(canvas);\n\n" +
            "// Phase 2: Canvas is reserved for WebGL — do NOT call getContext('2d')\n" +
            "// (WeChat locks each canvas to one context type)\n" +
            "console.log('[game.js] Step 4: Canvas ready (preserving WebGL context)');\n\n" +
            "// Phase 3: Load subpackages and start game\n" +
            "async function startGame() {\n" +
            "    console.log('[game.js] Step 5: startGame() entered');\n" +
            "    try {\n" +
            "        // Load engine subpackage (freetype.js, gdx.wasm.js)\n" +
            "        if (typeof wx.loadSubpackage === 'function') {\n" +
            "            console.log('[game.js] Step 6: Loading engine subpackage...');\n" +
            "            await wx.loadSubpackage({ name: 'engine' }).promise;\n" +
            "            console.log('[game.js] Step 7: Engine subpackage loaded');\n" +
            "        } else {\n" +
            "            console.log('[game.js] Step 6: wx.loadSubpackage not available, skipping');\n" +
            "        }\n\n" +
            "        // Load assets subpackage\n" +
            "        if (typeof wx.loadSubpackage === 'function') {\n" +
            "            console.log('[game.js] Step 7a: Loading assets subpackage...');\n" +
            "            try {\n" +
            "                await wx.loadSubpackage({ name: 'assets' }).promise;\n" +
            "                console.log('[game.js] Step 7b: Assets subpackage loaded');\n" +
            "            } catch(e) {\n" +
            "                console.log('[game.js] Step 7b: Assets subpackage load failed or not needed:', e.message);\n" +
            "            }\n" +
            "        }\n" +
            "        // Preload assets from filesystem into memory\n" +
            "        console.log('[game.js] Step 7c: Preloading assets...');\n" +
            "        adapter.preloadAssets();\n" +
            "        console.log('[game.js] Step 7d: Assets preloaded, count:', globalThis.__preloadedAssets ? Object.keys(globalThis.__preloadedAssets).length : 0);\n\n" +
            "        // Load freetype.js (sets globalThis.Module synchronously)\n" +
            "        try { console.log('[game.js] Step 8: Requiring freetype.js...'); require('./freetype.js'); console.log('[game.js] Step 9: freetype.js loaded'); } catch(e) { console.log('[game.js] Step 9: freetype.js skipped:', e.message); }\n\n" +
            "        // Load gdx.wasm.js (sets window.Gdx ASYNC via self-execution)\n" +
            "        try {\n" +
            "            console.log('[game.js] Step 10: Requiring gdx.wasm.js...');\n" +
            "            require('./gdx.wasm.js');\n" +
            "            console.log('[game.js] Step 11: gdx.wasm.js required, polling for window.Gdx...');\n" +
            "            // Wait for Gdx to be available (with timeout)\n" +
            "            var gdxWaitStart = Date.now();\n" +
            "            var pollCount = 0;\n" +
            "            while (typeof window.Gdx === 'undefined') {\n" +
            "                if (Date.now() - gdxWaitStart > 30000) {\n" +
            "                    throw new Error('Timed out waiting for Gdx to initialize (30s)');\n" +
            "                }\n" +
            "                if (pollCount % 20 === 0) {\n" +
            "                    console.log('[game.js] Step 12: Still polling window.Gdx... (', pollCount, 'polls,', Date.now() - gdxWaitStart, 'ms)');\n" +
            "                }\n" +
            "                pollCount++;\n" +
            "                await new Promise(r => setTimeout(r, 50));\n" +
            "            }\n" +
            "            console.log('[game.js] Step 13: window.Gdx available! type:', typeof window.Gdx);\n" +
            "        } catch(e) { console.error('[game.js] WASM init failed:', e); throw e; }\n\n" +
            "        // Start the compiled game\n" +
            "        console.log('[game.js] Step 14: Requiring " + data.outputName + ".js...');\n" +
            "        const mainModule = require('./" + data.outputName + ".js');\n" +
            "        console.log('[game.js] Step 15: App module loaded, main:', typeof mainModule.main);\n" +
            "        if (mainModule && mainModule.main) {\n" +
            "            console.log('[game.js] Step 16: Calling main()...');\n" +
            "            mainModule.main([" + mainClassArgs + "]);\n" +
            "            console.log('[game.js] Step 17: main() returned!');\n" +
            "        }\n" +
            "    } catch (err) {\n" +
            "        console.error('[game.js] FAILED:', err);\n" +
            "    }\n" +
            "}\n\n" +
            "startGame();\n";

        FileHandle gameJs = releasePath.child("game.js");
        gameJs.writeString(js, false);
    }

    /**
     * Generate adapter/index.js — Browser polyfills for WeChat environment.
     */
    private void generateAdapter() {
        FileHandle adapterDir = releasePath.child("adapter");
        adapterDir.mkdirs();

        String js = "// adapter/index.js — Minimal browser polyfills for WeChat Mini Game\n" +
            "// Generated by MiniGameBackend\n\n" +
            "console.log('[adapter] 0: file parsed, starting eval');\n" +
            "// === console polyfill (must be first — other polyfills use console.log) ===\n" +
            "if (typeof globalThis.console === 'undefined') {\n" +
            "    globalThis.console = { log: function(){}, error: function(){}, warn: function(){} };\n" +
            "}\n\n" +
            "console.log('[adapter] A: starting window polyfill');\n" +
            "// === window polyfill ===\n" +
            "// WeChat provides globalThis.window as a read-only getter, so we patch\n" +
            "// properties onto the existing Window object instead of replacing it.\n" +
            "(function() {\n" +
            "    var win = (typeof window !== 'undefined') ? window : globalThis.window;\n" +
            "    if (!win) { console.log('[adapter] A: no window, skipping polyfill'); return; }\n" +
            "    console.log('[adapter] A: win exists, typeof win:', typeof win);\n" +
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
            "console.log('[adapter] A: window polyfill done');\n\n" +
            "// === WebAssembly patch ===\n" +
            "if (typeof WXWebAssembly !== 'undefined') {\n" +
            "    if (typeof WebAssembly === 'undefined') {\n" +
            "        globalThis.WebAssembly = {};\n" +
            "    }\n" +
            "    var originalInstantiate = WebAssembly.instantiate;\n" +
            "    console.log('[adapter] WXWebAssembly detected, patching WebAssembly.instantiate');\n" +
            "    WebAssembly.instantiate = function(source, imports) {\n" +
            "        console.log('[adapter] WebAssembly.instantiate called, source type:', typeof source, 'isArrayBuffer:', source instanceof ArrayBuffer, 'isUint8Array:', source instanceof Uint8Array);\n" +
            "        if (source instanceof ArrayBuffer || source instanceof Uint8Array) {\n" +
            "            console.log('[adapter] Redirecting to WXWebAssembly.instantiate(\"scripts/gdx.wasm\")');\n" +
            "            return WXWebAssembly.instantiate('scripts/gdx.wasm', imports);\n" +
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
            "console.log('[adapter] C: defining preloadAssets and exports.setup');\n" +
            "// === Asset preloader ===\n" +
            "var __preloadedAssets = {};\n" +
            "globalThis.__preloadedAssets = __preloadedAssets;\n\n" +
            "exports.preloadAssets = function() {\n" +
            "    try {\n" +
            "        var fs = wx.getFileSystemManager();\n" +
            "        var renameMap = {};\n" +
            "        try {\n" +
            "            var mapData = fs.readFileSync('assets/rename-map.json', 'utf-8');\n" +
            "            renameMap = JSON.parse(mapData);\n" +
            "            console.log('[adapter] Loaded rename map, entries:', Object.keys(renameMap).length);\n" +
            "        } catch(e) {\n" +
            "            console.log('[adapter] No rename-map.json found, proceeding without renaming');\n" +
            "        }\n" +
            "        var manifest = '';\n" +
            "        try {\n" +
            "            manifest = fs.readFileSync('assets/preload.txt', 'utf-8');\n" +
            "        } catch(e) {\n" +
            "            console.log('[adapter] preloadAssets: No manifest file, skipping');\n" +
            "            return;\n" +
            "        }\n" +
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
            "                try {\n" +
            "                    var readPath = renameMap[assetPath] || assetPath;\n" +
            "                    var data = fs.readFileSync('assets' + readPath);\n" +
            "                    __preloadedAssets[assetPath] = { type: 'file', data: data };\n" +
            "                } catch(e2) {\n" +
            "                    console.error('[adapter] preloadAssets: Failed to read', assetPath, e2);\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
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
            "console.log('[adapter] D: adapter fully loaded');\n";

        FileHandle adapterJs = adapterDir.child("index.js");
        adapterJs.writeString(js, false);
    }

    /**
     * Generate stub game.js in each subpackage root directory.
     * WeChat requires <root>/game.js as the subpackage entry point.
     */
    private void generateSubpackageEntryFiles() {
        String stub = "// Subpackage entry — loaded by wx.loadSubpackage()\n";
        releasePath.child("scripts/game.js").writeString(stub, false);
        releasePath.child("assets/game.js").writeString(stub, false);
    }

    /**
     * Fix freetype.js Module scoping — inject globalThis.Module = Module
     * so that compiled code can access it.
     */
    private void fixFreetypeModuleScoping() {
        FileHandle scriptsFolder = releasePath.child("scripts");
        FileHandle freetypeJs = scriptsFolder.child("freetype.js");
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
     * Extract embedded WASM binary from gdx.wasm.js and write it as scripts/gdx.wasm.
     * The JS file contains base64 WASM data in a data URL like:
     *   O="data:application/octet-stream;base64,AGFzbQ..."
     * We parse this out, decode the base64, and write the raw bytes as a .wasm file
     * so WXWebAssembly.instantiate('scripts/gdx.wasm', imports) can find it.
     */
    private void extractWasmFromJs() {
        // gdx.wasm.js lives in root (not scripts/) — JS files can't be require()'d from subpackages
        FileHandle wasmJs = releasePath.child("gdx.wasm.js");
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
            // Write the .wasm binary to scripts/ subpackage (WXWebAssembly reads from filesystem)
            FileHandle scriptsFolder = releasePath.child("scripts");
            FileHandle wasmFile = scriptsFolder.child("gdx.wasm");
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

        // Copy scripts to scripts/ directory
        FileHandle scriptsFolder = releasePath.child("scripts");
        AssetsCopy.copyResources(classLoader, scripts, scriptFilter, scriptsFolder);

        // Move gdx.wasm.js from scripts/ to root — JS files cannot be require()'d from subpackages
        // because WeChat's module system doesn't register subpackage JS files as CommonJS modules
        FileHandle wasmJsInScripts = scriptsFolder.child("gdx.wasm.js");
        if (wasmJsInScripts.exists()) {
            FileHandle wasmJsInRoot = releasePath.child("gdx.wasm.js");
            wasmJsInScripts.moveTo(wasmJsInRoot);
            System.out.println("[MiniGameBackend] Moved gdx.wasm.js to root (JS files must be in main package)");
        }

        // Generate subpackage entry files required by WeChat
        generateSubpackageEntryFiles();

        // Extract WASM binary from embedded base64 in gdx.wasm.js
        extractWasmFromJs();

        // Rename files with blocked extensions for WeChat readFileSync compatibility
        renameBlockedExtensions();
    }

    /**
     * Rename files with extensions that WeChat's readFileSync blocks (permission denied).
     * Appends .txt to blocked extensions and generates a rename-map.json for runtime lookup.
     */
    private void renameBlockedExtensions() {
        Set<String> allowedExts = new HashSet<>(Arrays.asList(
            "txt", "png", "jpg", "jpeg", "json", "js", "atlas",
            "ttf", "mp3", "wav", "xml", "fnt", "csv", "webp", "wasm"
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
