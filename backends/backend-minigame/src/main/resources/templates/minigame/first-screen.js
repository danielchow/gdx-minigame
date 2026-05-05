// first-screen.js — WebGL splash screen for WeChat Mini Game
// Shows a colored rectangle during loading to avoid black screen

var GL = null;
var program = null;
var canvas = null;

exports.start = function(c) {
    canvas = c;
    try {
        GL = canvas.getContext('webgl');
        if (!GL) {
            console.log('[first-screen] WebGL not available, skipping splash');
            return;
        }
        GL.viewport(0, 0, canvas.width, canvas.height);
        GL.scissor(0, 0, canvas.width, canvas.height);
        GL.enable(GL.SCISSOR_TEST);
        GL.clearColor(0.1, 0.1, 0.15, 1.0);
        GL.clear(GL.COLOR_BUFFER_BIT);
        console.log('[first-screen] Splash drawn', canvas.width, 'x', canvas.height);
    } catch(e) {
        console.log('[first-screen] Splash failed:', e.message);
    }
};

exports.setProgress = function(ratio) {
    if (!GL) return;
    try {
        var w = canvas.width * ratio;
        GL.scissor(0, 0, Math.floor(w), canvas.height);
        GL.clearColor(0.2, 0.5, 0.8, 1.0);
        GL.clear(GL.COLOR_BUFFER_BIT);
        // Reset scissor for remaining area
        GL.scissor(Math.floor(w), 0, canvas.width - Math.floor(w), canvas.height);
        GL.clearColor(0.1, 0.1, 0.15, 1.0);
        GL.clear(GL.COLOR_BUFFER_BIT);
        GL.scissor(0, 0, canvas.width, canvas.height);
    } catch(e) {}
};

exports.redraw = function() {
    if (!GL) return;
    try {
        GL.scissor(0, 0, canvas.width, canvas.height);
        GL.clearColor(0.1, 0.1, 0.15, 1.0);
        GL.clear(GL.COLOR_BUFFER_BIT);
    } catch(e) {}
};

exports.end = function() {
    if (!GL) return;
    try {
        GL.disable(GL.SCISSOR_TEST);
    } catch(e) {}
    GL = null;
    program = null;
};
