package com.github.xpenatan.gdx.teavm.backends.minigame.regression;

import com.github.xpenatan.gdx.teavm.backends.minigame.MiniGameShare;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/** Drives MiniGameShare against the mocked wx object of share.test.mjs. */
public final class ShareProbe {
    private static MiniGameShare.MenuReply lateReply;

    public static void main(String[] args) {
        register((command, arg) -> {
            switch (command) {
                case "supported": return MiniGameShare.isSupported() ? 1 : 0;
                case "share": MiniGameShare.shareAppMessage("Can you solve it?", "wxfile://tmp/card.png", "deal=ABC123"); return 0;
                case "shareTitleOnly": MiniGameShare.shareAppMessage("Title only", null, null); return 0;
                case "menu":
                    // 0 replies at once, 1 later, 2 with no image, 3 never.
                    MiniGameShare.setShareMenu("Invite", "invite.png", "", reply -> {
                        record("provide:" + arg);
                        if (arg == 0) reply.reply("Deal card", "wxfile://tmp/deal.png", "deal=XYZ");
                        else if (arg == 1) lateReply = reply;
                        else if (arg == 2) reply.reply("No image", null, "deal=NOIMG");
                    });
                    return 0;
                case "replyLate": lateReply.reply("Late card", "wxfile://tmp/late.png", "deal=LATE"); return 0;
                case "listen":
                    MiniGameShare.setLaunchQueryListener(query -> {
                        StringBuilder entries = new StringBuilder("query");
                        query.forEach((key, value) -> entries.append(':').append(key).append('=').append(value));
                        record(entries.toString());
                    });
                    return 0;
                case "encode": {
                    // Two pixels, top row first: opaque red, then half-transparent blue.
                    byte[] rgba = {(byte) 255, 0, 0, (byte) 255, 0, 0, (byte) 255, (byte) 128};
                    record("path:" + MiniGameShare.encodePng(rgba, 2, 1));
                    return 0;
                }
                case "encodeWrongSize":
                    try {
                        MiniGameShare.encodePng(new byte[7], 2, 1);
                        record("encoded");
                    } catch (IllegalArgumentException expected) {
                        record("rejected");
                    }
                    return 0;
                case "encodeFails":
                    try {
                        MiniGameShare.encodePng(new byte[4], 1, 1);
                        record("encoded");
                    } catch (IllegalStateException expected) {
                        record("failed");
                    }
                    return 0;
                default: throw new IllegalArgumentException(command);
            }
        });
    }

    @JSFunctor
    interface Command extends JSObject {
        int run(String command, int arg);
    }

    @JSBody(params = "command", script = "globalThis.__shareProbe.command = command;")
    private static native void register(Command command);

    @JSBody(params = "event", script = "globalThis.__shareProbe.events.push(event);")
    private static native void record(String event);
}
