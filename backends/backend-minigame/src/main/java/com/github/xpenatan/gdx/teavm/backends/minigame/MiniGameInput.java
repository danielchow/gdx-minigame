package com.github.xpenatan.gdx.teavm.backends.minigame;

import com.badlogic.gdx.AbstractInput;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.input.NativeInputConfiguration;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.TimeUtils;
import com.github.xpenatan.gdx.teavm.backends.minigame.bindings.TouchCallback;
import com.github.xpenatan.gdx.teavm.backends.minigame.bindings.WX;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.core.JSArrayReader;
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

    private IntSet pressedKeys = new IntSet();
    private IntMap<Boolean> pressedButtons = new IntMap<>();

    private InputProcessor processor;
    private boolean justTouched = false;

    private int mouseLastX = 0;
    private int mouseLastY = 0;
    private int currentEventTimeStamp = 0;

    public MiniGameInput(MiniGameApplication app, HTMLCanvasElement canvas) {
        this.app = app;
        this.canvas = canvas;
        setupTouchEvents();
    }

    private void setupTouchEvents() {
        WX.onTouchStart((TouchCallback) event -> handleTouchEvent(event, 0));
        WX.onTouchMove((TouchCallback) event -> handleTouchEvent(event, 1));
        WX.onTouchEnd((TouchCallback) event -> handleTouchEvent(event, 2));
        WX.onTouchCancel((TouchCallback) event -> handleTouchEvent(event, 2));
    }

    private native JSArrayReader<TouchJS> getChangedTouches(JSObject event) /*-{ return event.changedTouches; }-*/;

    private void handleTouchEvent(JSObject event, int type) {
        currentEventTimeStamp = (int)(TimeUtils.nanoTime() / 1000000);
        JSArrayReader<TouchJS> changedTouches = getChangedTouches(event);
        if (changedTouches == null) return;

        for (int i = 0; i < changedTouches.getLength(); i++) {
            TouchJS touch = changedTouches.get(i);
            int id = touch.getIdentifier();
            int x = touch.getClientX();
            int y = touch.getClientY();

            if (type == 0) { // TOUCH_DOWN
                if (id >= 0 && id < MAX_TOUCHES) {
                    touched[id] = true;
                    touchX[id] = x;
                    touchY[id] = y;
                    deltaX[id] = 0;
                    deltaY[id] = 0;
                }
                justTouched = true;
                pressedButtons.put(Input.Buttons.LEFT, true);
                if (processor != null) processor.touchDown(x, y, 0, Input.Buttons.LEFT);
            }
            else if (type == 1) { // TOUCH_MOVE
                if (id >= 0 && id < MAX_TOUCHES) {
                    deltaX[id] = x - touchX[id];
                    deltaY[id] = y - touchY[id];
                    touchX[id] = x;
                    touchY[id] = y;
                }
                if (processor != null) processor.touchDragged(x, y, 0);
            }
            else { // TOUCH_UP
                if (id >= 0 && id < MAX_TOUCHES) {
                    touched[id] = false;
                    deltaX[id] = x - touchX[id];
                    deltaY[id] = y - touchY[id];
                    touchX[id] = x;
                    touchY[id] = y;
                }
                if (processor != null) processor.touchUp(x, y, 0, Input.Buttons.LEFT);
                pressedButtons.remove(Input.Buttons.LEFT);
            }
            mouseLastX = x;
            mouseLastY = y;
        }
    }

    interface TouchJS extends JSObject {
        @JSProperty int getIdentifier();
        @JSProperty int getClientX();
        @JSProperty int getClientY();
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
