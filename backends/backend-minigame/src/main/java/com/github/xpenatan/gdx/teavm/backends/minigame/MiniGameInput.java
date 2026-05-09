package com.github.xpenatan.gdx.teavm.backends.minigame;

import com.badlogic.gdx.AbstractInput;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.input.NativeInputConfiguration;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.TimeUtils;
import com.github.xpenatan.gdx.teavm.backends.minigame.bindings.TouchCallback;
import com.github.xpenatan.gdx.teavm.backends.minigame.bindings.WX;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSArrayReader;
import org.teavm.jso.dom.events.Touch;
import org.teavm.jso.dom.html.HTMLCanvasElement;

/**
 * Input implementation for WeChat Mini Game.
 * Uses wx.onTouchStart/Move/End/Cancel for touch events.
 * Synthesizes mouse events from touch for scene2d compatibility.
 */
public class MiniGameInput extends AbstractInput {

    private static final int MAX_TOUCHES = 20;

    private final MiniGameApplication app;
    private final HTMLCanvasElement canvas;

    private boolean[] touched = new boolean[MAX_TOUCHES];
    private int[] touchX = new int[MAX_TOUCHES];
    private int[] touchY = new int[MAX_TOUCHES];
    private int[] deltaX = new int[MAX_TOUCHES];
    private int[] deltaY = new int[MAX_TOUCHES];

    private IntMap<Boolean> pressedButtons = new IntMap<>();
    private IntMap<Integer> touchMap = new IntMap<>(20);

    private InputProcessor processor;
    private boolean justTouched = false;

    private int mouseLastX = 0;
    private int mouseLastY = 0;
    private long currentEventTimeStamp = 0;

    // Cached scale factors for touch coordinate mapping
    private float touchScaleX;
    private float touchScaleY;

    public MiniGameInput(MiniGameApplication app, HTMLCanvasElement canvas) {
        this.app = app;
        this.canvas = canvas;
        // Cache scale factors: canvas physical pixels / CSS pixels
        touchScaleX = (float) canvas.getWidth() / (float) WX.getScreenWidth();
        touchScaleY = (float) canvas.getHeight() / (float) WX.getScreenHeight();
        setupTouchEvents();
    }

    private void setupTouchEvents() {
        WX.onTouchStart((TouchCallback) event -> handleTouchEvent(event, 0));
        WX.onTouchMove((TouchCallback) event -> handleTouchEvent(event, 1));
        WX.onTouchEnd((TouchCallback) event -> handleTouchEvent(event, 2));
        WX.onTouchCancel((TouchCallback) event -> handleTouchEvent(event, 2));
    }

    @JSBody(params = "event", script = "return event.changedTouches;")
    private static native JSArrayReader<Touch> getChangedTouches(JSObject event);

    private int getAvailablePointer() {
        for (int i = 0; i < MAX_TOUCHES; i++) {
            if (!touchMap.containsValue(i, false)) return i;
        }
        return 0;
    }

    private void handleTouchEvent(JSObject event, int type) {
        currentEventTimeStamp = TimeUtils.nanoTime();
        JSArrayReader<Touch> changedTouches = getChangedTouches(event);
        if (changedTouches == null) return;

        if (type == 0) justTouched = true;

        for (int i = 0; i < changedTouches.getLength(); i++) {
            Touch touch = changedTouches.get(i);
            int real = touch.getIdentifier();
            int x = Math.round((float) touch.getClientX() * touchScaleX);
            int y = Math.round((float) touch.getClientY() * touchScaleY);

            if (type == 0) { // TOUCH_DOWN
                int touchId;
                touchMap.put(real, touchId = getAvailablePointer());
                touched[touchId] = true;
                touchX[touchId] = x;
                touchY[touchId] = y;
                deltaX[touchId] = 0;
                deltaY[touchId] = 0;
                if (touchId == 0) pressedButtons.put(Input.Buttons.LEFT, true);
                if (processor != null) processor.touchDown(x, y, touchId, Input.Buttons.LEFT);
            }
            else if (type == 1) { // TOUCH_MOVE
                int touchId = touchMap.get(real, -1);
                if (touchId >= 0) {
                    deltaX[touchId] = x - touchX[touchId];
                    deltaY[touchId] = y - touchY[touchId];
                    touchX[touchId] = x;
                    touchY[touchId] = y;
                    if (processor != null) processor.touchDragged(x, y, touchId);
                }
            }
            else { // TOUCH_UP / TOUCH_CANCEL
                Integer touchIdObj = touchMap.get(real);
                if (touchIdObj != null) {
                    int touchId = touchIdObj;
                    touchMap.remove(real);
                    touched[touchId] = false;
                    deltaX[touchId] = x - touchX[touchId];
                    deltaY[touchId] = y - touchY[touchId];
                    touchX[touchId] = x;
                    touchY[touchId] = y;
                    if (processor != null) processor.touchUp(x, y, touchId, Input.Buttons.LEFT);
                    if (touchId == 0) {
                        boolean anyTouched = false;
                        for (int j = 0; j < MAX_TOUCHES; j++) if (touched[j]) { anyTouched = true; break; }
                        if (!anyTouched) pressedButtons.remove(Input.Buttons.LEFT);
                    }
                }
            }
            mouseLastX = x;
            mouseLastY = y;
        }
    }


    // === AbstractInput / Input implementation ===

    @Override public float getAccelerometerX() { return 0; }
    @Override public float getAccelerometerY() { return 0; }
    @Override public float getAccelerometerZ() { return 0; }
    @Override public float getGyroscopeX() { return 0; }
    @Override public float getGyroscopeY() { return 0; }
    @Override public float getGyroscopeZ() { return 0; }
    @Override public int getMaxPointers() { return MAX_TOUCHES; }

    @Override public int getX() { return touchX[0]; }
    @Override public int getX(int pointer) { return pointer >= 0 && pointer < MAX_TOUCHES ? touchX[pointer] : 0; }
    @Override public int getDeltaX() { return deltaX[0]; }
    @Override public int getDeltaX(int pointer) { return pointer >= 0 && pointer < MAX_TOUCHES ? deltaX[pointer] : 0; }
    @Override public int getY() { return touchY[0]; }
    @Override public int getY(int pointer) { return pointer >= 0 && pointer < MAX_TOUCHES ? touchY[pointer] : 0; }
    @Override public int getDeltaY() { return deltaY[0]; }
    @Override public int getDeltaY(int pointer) { return pointer >= 0 && pointer < MAX_TOUCHES ? deltaY[pointer] : 0; }

    @Override public boolean isTouched(int pointer) { return pointer >= 0 && pointer < MAX_TOUCHES && touched[pointer]; }

    @Override public boolean justTouched() { return justTouched; }

    @Override public float getPressure() { return isTouched() ? 1.0f : 0.0f; }
    @Override public float getPressure(int pointer) { return isTouched(pointer) ? 1.0f : 0.0f; }

    @Override public boolean isButtonPressed(int button) {
        return pressedButtons.containsKey(button) && pressedButtons.get(button);
    }
    @Override public boolean isButtonJustPressed(int button) { return false; }

    @Override public void getTextInput(TextInputListener listener, String title, String text, String hint) {}
    @Override public void getTextInput(TextInputListener listener, String title, String text, String hint, OnscreenKeyboardType keyboardType) {}

    @Override public void setOnscreenKeyboardVisible(boolean visible) {
        if (visible) WX.showKeyboard(null); else WX.hideKeyboard();
    }
    @Override public void setOnscreenKeyboardVisible(boolean visible, OnscreenKeyboardType type) {
        setOnscreenKeyboardVisible(visible);
    }

    @Override public void openTextInputField(NativeInputConfiguration configuration) { setOnscreenKeyboardVisible(true); }
    @Override public void closeTextInputField(boolean sendReturn) { setOnscreenKeyboardVisible(false); }
    @Override public void setKeyboardHeightObserver(KeyboardHeightObserver observer) {}

    @Override public void vibrate(int milliseconds) { WX.vibrateLong(); }
    @Override public void vibrate(int milliseconds, boolean fallback) { WX.vibrateLong(); }
    @Override public void vibrate(int milliseconds, int amplitude, boolean fallback) { WX.vibrateLong(); }
    @Override public void vibrate(VibrationType vibrationType) { WX.vibrateShort(); }

    @Override public float getAzimuth() { return 0; }
    @Override public float getPitch() { return 0; }
    @Override public float getRoll() { return 0; }
    @Override public void getRotationMatrix(float[] matrix) {}

    @Override public long getCurrentEventTime() { return currentEventTimeStamp; }

    @Override public void setInputProcessor(InputProcessor processor) { this.processor = processor; }
    @Override public InputProcessor getInputProcessor() { return processor; }

    @Override public boolean isPeripheralAvailable(Peripheral peripheral) {
        return peripheral == Peripheral.MultitouchScreen;
    }

    @Override public int getRotation() { return 0; }
    @Override public Orientation getNativeOrientation() { return Orientation.Portrait; }

    @Override public void setCursorCatched(boolean catched) {}
    @Override public boolean isCursorCatched() { return false; }
    @Override public void setCursorPosition(int x, int y) {}

    @Override public boolean isTouched() {
        for (int i = 0; i < MAX_TOUCHES; i++) if (touched[i]) return true;
        return false;
    }

    public void reset() {
        justTouched = false;
        for (int i = 0; i < MAX_TOUCHES; i++) { deltaX[i] = 0; deltaY[i] = 0; }
    }
}
