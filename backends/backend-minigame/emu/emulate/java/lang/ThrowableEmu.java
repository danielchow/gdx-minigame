package emulate.java.lang;

import com.github.xpenatan.gdx.teavm.backends.minigame.MiniGameApplication;
import com.github.xpenatan.gdx.teavm.backends.web.gen.Emulate;

@Emulate(value = Throwable.class, updateCode = true)
public class ThrowableEmu {

    @Emulate
    public void printStackTrace() {
        MiniGameApplication.printErrorStack(this);
    }
}
