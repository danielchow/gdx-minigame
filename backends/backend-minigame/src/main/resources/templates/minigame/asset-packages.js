// Asset-only subpackages. State belongs to this running game version, never user storage.
// Callers decide whether a failure needs retry UI; this layer only reports load readiness.
function create(wx, files) {
    var loaded = Object.create(null);
    var pending = Object.create(null);
    function normalize(path) { return path.trim().replace(/^\/+/, ''); }
    function owner(path) {
        path = normalize(path);
        return Object.prototype.hasOwnProperty.call(files, path) ? files[path] : null;
    }
    function load(name) {
        if (loaded[name]) return Promise.resolve();
        if (pending[name]) return pending[name].promise;
        var resolveLoad, rejectLoad;
        var entry = {timer: null, settled: false};
        entry.promise = new Promise(function(resolve, reject) { resolveLoad = resolve; rejectLoad = reject; });
        pending[name] = entry;
        function finish(error) {
            if (entry.settled) return;
            entry.settled = true;
            clearTimeout(entry.timer);
            delete pending[name];
            if (error) rejectLoad(error);
            else { loaded[name] = true; resolveLoad(); }
        }
        function progress() {
            if (entry.settled) return;
            clearTimeout(entry.timer);
            entry.timer = setTimeout(function() { finish(new Error('Asset package stalled: ' + name)); }, 30000);
        }
        progress();
        try {
            if (typeof wx.loadSubpackage !== 'function') throw new Error('loadSubpackage unavailable');
            var task = wx.loadSubpackage({name: name,
                success: function() { finish(null); },
                fail: function(error) { finish(error || new Error('Asset package failed: ' + name)); }});
            if (task && typeof task.onProgressUpdate === 'function') task.onProgressUpdate(progress);
        } catch (error) { finish(error); }
        return entry.promise;
    }
    return {
        isMissing: function(path) { var name = owner(path); return !!name && !loaded[name]; },
        downloadingCount: function() { return Object.keys(pending).length; },
        isLoading: function(path) { return !!pending[owner(path)]; },
        root: function(path) { var name = owner(path); return name ? 'optional/' + name + '/' : 'assets/'; },
        load: function(paths) {
            var names = [];
            paths.forEach(function(path) { var name = owner(path); if (name && names.indexOf(name) < 0) names.push(name); });
            return Promise.all(names.map(load));
        }
    };
}
module.exports = {create: create};
