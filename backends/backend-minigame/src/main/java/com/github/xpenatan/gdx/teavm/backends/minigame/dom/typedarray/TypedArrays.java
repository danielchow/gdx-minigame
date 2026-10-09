package com.github.xpenatan.gdx.teavm.backends.minigame.dom.typedarray;

import com.badlogic.gdx.utils.GdxRuntimeException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import org.teavm.classlib.PlatformDetector;
import org.teavm.jso.JSBody;
import org.teavm.jso.typedarrays.ArrayBufferView;
import org.teavm.jso.typedarrays.Float32Array;
import org.teavm.jso.typedarrays.Int16Array;
import org.teavm.jso.typedarrays.Int32Array;
import org.teavm.jso.typedarrays.Int8Array;
import org.teavm.jso.typedarrays.TypedArray;
import org.teavm.jso.typedarrays.Uint16Array;
import org.teavm.jso.typedarrays.Uint8Array;

/**
 * @author xpenatan
 */
public class TypedArrays {

    // Obtain the array reference from ArrayBufferView
    public static byte[] toByteArray(TypedArray array) {
        Int8Array intArray = new Int8Array(array);
        return intArray.copyToJavaArray();
    }

    public static ArrayBufferView getTypedArray(Buffer buffer) {
        return getTypedArray(false, buffer);
    }

    public static ArrayBufferView getTypedArray(boolean isUnsigned, Buffer buffer) {
        if(buffer instanceof ByteBuffer) {
            if(isUnsigned) {
                return getUint8Array(buffer);
            }
            else {
                return getInt8Array(buffer);
            }
        }
        else if(buffer instanceof ShortBuffer) {
            if(isUnsigned) {
                return getUint16Array(buffer);
            }
            else {
                return getInt16Array(buffer);
            }
        }
        else if(buffer instanceof IntBuffer) {
            return getInt32Array(buffer);
        }
        else if(buffer instanceof FloatBuffer) {
            return getFloat32Array(buffer);
        }
        throw new GdxRuntimeException("No support for buffer " + buffer.getClass());
    }

    public static Int8Array getInt8Array(Buffer buff) {
        if(PlatformDetector.isJavaScript() || buff.isDirect()) {
            return Int8Array.fromJavaBuffer(buff);
        }
        else {
            if(buff instanceof ByteBuffer) {
                ByteBuffer buffer = (ByteBuffer)buff;
                if(buffer.hasArray()) {
                    return Int8Array.copyFromJavaArray(buffer.array());
                }
                else {
                    int position = buffer.position();
                    int limit = buffer.limit();
                    int capacity = buffer.capacity();
                    buffer.position(0);
                    buffer.limit(capacity);
                    var array = new byte[capacity];
                    buffer.get(array);
                    buffer.position(position);
                    buffer.limit(limit);
                    return Int8Array.copyFromJavaArray(array);
                }
            }
            else {
                ArrayBufferView typedArray = getTypedArray(false, buff);
                return new Int8Array(typedArray.getBuffer());
            }
        }
    }

    public static Uint8Array getUint8Array(Buffer buff) {
        if(PlatformDetector.isJavaScript() || buff.isDirect()) {
            return Uint8Array.fromJavaBuffer(buff);
        }
        else {
            if(buff instanceof ByteBuffer) {
                ByteBuffer buffer = (ByteBuffer)buff;
                 if(buffer.hasArray()) {
                    var typedArray = Int8Array.copyFromJavaArray(buffer.array());
                    return new Uint8Array(typedArray.getBuffer());
                }
                else {
                     int position = buffer.position();
                     int limit = buffer.limit();
                     int capacity = buffer.capacity();
                     buffer.position(0);
                     buffer.limit(capacity);
                     var array = new byte[capacity];
                     buffer.get(array);
                     buffer.position(position);
                     buffer.limit(limit);
                    var typedArray = Int8Array.copyFromJavaArray(array);
                    return new Uint8Array(typedArray.getBuffer());
                }
            }
            else {
                ArrayBufferView typedArray = getTypedArray(true, buff);
                return new Uint8Array(typedArray.getBuffer());
            }
        }
    }

    public static Int16Array getInt16Array(Buffer buff) {
        if(PlatformDetector.isJavaScript() || buff.isDirect()) {
            return Int16Array.fromJavaBuffer(buff);
        }
        else {
            if(buff instanceof ShortBuffer) {
                ShortBuffer buffer = (ShortBuffer)buff;
                if(buffer.hasArray()) {
                    return Int16Array.copyFromJavaArray(buffer.array());
                }
                else {
                    int position = buffer.position();
                    int limit = buffer.limit();
                    int capacity = buffer.capacity();
                    buffer.position(0);
                    buffer.limit(capacity);
                    var array = new short[buffer.capacity()];
                    buffer.get(array);
                    buffer.position(position);
                    buffer.limit(limit);
                    return Int16Array.copyFromJavaArray(array);
                }
            }
            else if(buff instanceof ByteBuffer) {
                ByteBuffer buffer = (ByteBuffer)buff;
                Int8Array array = getInt8Array(buffer);
                return new Int16Array(array);
            }
            else {
                throw new RuntimeException("TypedArrays#getInt16Array - Unsupported buffer type " + buff.getClass().getSimpleName());
            }
        }
    }

    public static Uint16Array getUint16Array(Buffer buff) {
        if(PlatformDetector.isJavaScript() || buff.isDirect()) {
            return Uint16Array.fromJavaBuffer(buff);
        }
        else {
            if(buff instanceof ShortBuffer) {
                ShortBuffer buffer = (ShortBuffer)buff;
                if(buffer.hasArray()) {
                    var typedArray = Int16Array.copyFromJavaArray(buffer.array());
                    return new Uint16Array(typedArray.getBuffer());
                }
                else {
                    int position = buffer.position();
                    int limit = buffer.limit();
                    int capacity = buffer.capacity();
                    buffer.position(0);
                    buffer.limit(capacity);
                    var array = new short[buffer.capacity()];
                    buffer.get(array);
                    buffer.position(position);
                    buffer.limit(limit);
                    var typedArray = Int16Array.copyFromJavaArray(array);
                    return new Uint16Array(typedArray.getBuffer());
                }
            }
            else if(buff instanceof ByteBuffer) {
                ByteBuffer buffer = (ByteBuffer)buff;
                Uint8Array array = getUint8Array(buffer);
                return new Uint16Array(array.getBuffer());
            }
            else {
                throw new RuntimeException("TypedArrays#getUint16Array - Unsupported buffer type " + buff.getClass().getSimpleName());
            }
        }
    }

    public static Int32Array getInt32Array(Buffer buff) {
        if(PlatformDetector.isJavaScript() || buff.isDirect()) {
            return Int32Array.fromJavaBuffer(buff);
        }
        else {
            if(buff instanceof IntBuffer) {
                IntBuffer buffer = (IntBuffer)buff;
                if(buffer.hasArray()) {
                    return Int32Array.copyFromJavaArray(buffer.array());
                }
                else {
                    int position = buffer.position();
                    int limit = buffer.limit();
                    int capacity = buffer.capacity();
                    buffer.position(0);
                    buffer.limit(capacity);
                    var array = new int[buffer.capacity()];
                    buffer.get(array);
                    buffer.position(position);
                    buffer.limit(limit);
                    return Int32Array.copyFromJavaArray(array);
                }
            }
            else if(buff instanceof ByteBuffer) {
                ByteBuffer buffer = (ByteBuffer)buff;
                Int8Array array = getInt8Array(buffer);
                return new Int32Array(array);
            }
            else {
                throw new RuntimeException("TypedArrays#getInt32Array - Unsupported buffer type " + buff.getClass().getSimpleName());
            }
        }
    }

    public static Float32Array getFloat32Array(Buffer buff) {
        if(PlatformDetector.isJavaScript() || buff.isDirect()) {
            return Float32Array.fromJavaBuffer(buff);
        }
        else {
            if(buff instanceof FloatBuffer) {
                FloatBuffer buffer = (FloatBuffer)buff;
                if(buffer.hasArray()) {
                    return Float32Array.copyFromJavaArray(buffer.array());
                }
                else {
                    int position = buffer.position();
                    int limit = buffer.limit();
                    int capacity = buffer.capacity();
                    buffer.position(0);
                    buffer.limit(capacity);
                    var array = new float[buffer.capacity()];
                    buffer.get(array);
                    buffer.position(position);
                    buffer.limit(limit);
                    return Float32Array.copyFromJavaArray(array);
                }
            }
            else if(buff instanceof ByteBuffer) {
                ByteBuffer buffer = (ByteBuffer)buff;
                Int8Array array = getInt8Array(buffer);
                return new Float32Array(array);
            }
            else {
                throw new RuntimeException("TypedArrays#getFloat32Array - Unsupported buffer type " + buff.getClass().getSimpleName());
            }
        }
    }

    public static Int8Array getInt8Array(byte[] buffer) {
        return Int8Array.copyFromJavaArray(buffer);
    }

    public static void copy(Int8Array in, ByteBuffer out) {
        if(PlatformDetector.isJavaScript() || out.isDirect()) {
            Int8Array array = Int8Array.fromJavaBuffer(out);
            array.set(in);
        }
        else {
            var data = in.copyToJavaArray();
            out.put(data);
        }
    }

    // GL calls must upload only the elements they are given, as desktop and Android do. The views below
    // share memory with the Java buffer or array (no copy); getTypedArray alone covers the whole capacity.

    /** The buffer's remaining elements, [position, limit). */
    public static ArrayBufferView getRemaining(Buffer buffer) {
        return subarray(getTypedArray(buffer), buffer.position(), buffer.limit());
    }

    /** {@code length} floats starting at the buffer's position. */
    public static Float32Array getFloat32Range(FloatBuffer buffer, int length) {
        return subarray(getFloat32Array(buffer), buffer.position(), buffer.position() + length);
    }

    /** {@code length} ints starting at the buffer's position. */
    public static Int32Array getInt32Range(IntBuffer buffer, int length) {
        return subarray(getInt32Array(buffer), buffer.position(), buffer.position() + length);
    }

    /** {@code length} floats starting at {@code offset}. */
    public static Float32Array getFloat32Range(float[] array, int offset, int length) {
        return subarray(Float32Array.fromJavaArray(array), offset, offset + length);
    }

    /** {@code length} ints starting at {@code offset}. */
    public static Int32Array getInt32Range(int[] array, int offset, int length) {
        return subarray(Int32Array.fromJavaArray(array), offset, offset + length);
    }

    @JSBody(params = {"array", "begin", "end"}, script = "return array.subarray(begin, end);")
    private static native ArrayBufferView subarray(ArrayBufferView array, int begin, int end);

    @JSBody(params = {"array", "begin", "end"}, script = "return array.subarray(begin, end);")
    private static native Float32Array subarray(Float32Array array, int begin, int end);

    @JSBody(params = {"array", "begin", "end"}, script = "return array.subarray(begin, end);")
    private static native Int32Array subarray(Int32Array array, int begin, int end);
}
