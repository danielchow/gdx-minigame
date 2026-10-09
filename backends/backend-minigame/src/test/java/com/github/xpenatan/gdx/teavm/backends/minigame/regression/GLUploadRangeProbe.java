package com.github.xpenatan.gdx.teavm.backends.minigame.regression;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.VertexBufferObject;
import com.badlogic.gdx.utils.BufferUtils;
import com.github.xpenatan.gdx.teavm.backends.minigame.WebGL20;
import com.github.xpenatan.gdx.teavm.backends.minigame.gl.WebGLRenderingContextExt;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

/**
 * Drives the backend's WebGL20 against a recording context (see gl-upload-range.test.mjs). GL calls must upload only
 * the elements they are given: a SpriteBatch-sized vertex buffer holding two sprites must not upload all 1000.
 */
public final class GLUploadRangeProbe {
    public static void main(String[] args) {
        WebGL20 gl = new WebGL20((WebGLRenderingContextExt)mockContext());
        Gdx.gl = gl;
        Gdx.gl20 = gl;

        // SpriteBatch(1000) layout: 4 vertices per sprite, position + packed color + texcoords = 5 floats each.
        VertexBufferObject vbo = new VertexBufferObject(false, 4000,
            VertexAttribute.Position(), VertexAttribute.ColorPacked(), VertexAttribute.TexCoords(0));
        float[] vertices = new float[40];
        for (int i = 0; i < vertices.length; i++) vertices[i] = i + 1;
        vbo.setVertices(vertices, 0, vertices.length);
        vbo.bind(null, new int[] {-1, -1, -1});
        record("vbo-bufferData", lastFloats("bufferData"));

        FloatBuffer floats = BufferUtils.newFloatBuffer(100);
        for (int i = 0; i < 100; i++) floats.put(i, i * 10);
        floats.position(8);
        floats.limit(12);
        gl.glBufferSubData(GL20.GL_ARRAY_BUFFER, 64, 16, floats);
        record("bufferSubData", lastFloats("bufferSubData"));

        int program = gl.glCreateProgram();
        gl.glUseProgram(program);
        int u = gl.glGetUniformLocation(program, "u");
        floats.position(4);
        floats.limit(100);
        gl.glUniformMatrix4fv(u, 1, false, floats);
        record("uniformMatrix4fv-buffer", lastFloats("uniformMatrix4fv"));
        gl.glUniform4fv(u, 2, vertices, 3);
        record("uniform4fv-array", lastFloats("uniform4fv"));
        gl.glUniformMatrix3fv(u, 1, false, vertices, 5);
        record("uniformMatrix3fv-array", lastFloats("uniformMatrix3fv"));
        IntBuffer ints = BufferUtils.newIntBuffer(10);
        for (int i = 0; i < 10; i++) ints.put(i, i * 3);
        ints.position(2);
        gl.glUniform2iv(u, 1, ints);
        record("uniform2iv-buffer", lastFloats("uniform2iv"));
        gl.glUniform1iv(u, 3, new int[] {9, 8, 7, 6, 5}, 1);
        record("uniform1iv-array", lastFloats("uniform1iv"));
        floats.position(20);
        gl.glVertexAttrib3fv(0, floats);
        record("vertexAttrib3fv", lastFloats("vertexAttrib3fv"));
        complete();
    }

    @JSBody(script = "return globalThis.__glMock;")
    private static native JSObject mockContext();

    /** The data argument of the most recent call, as a plain array of numbers. */
    @JSBody(params = "method", script = "var calls = globalThis.__glCalls[method];"
        + "var view = calls[calls.length - 1];"
        + "return Array.prototype.slice.call(view).join(',');")
    private static native String lastFloats(String method);

    @JSBody(params = {"name", "values"}, script = "globalThis.__glProbe.results[name] = values;")
    private static native void record(String name, String values);

    @JSBody(script = "globalThis.__glProbe.complete = true;")
    private static native void complete();
}
