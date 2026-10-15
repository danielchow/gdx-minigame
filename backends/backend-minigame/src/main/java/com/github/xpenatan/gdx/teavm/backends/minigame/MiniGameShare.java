package com.github.xpenatan.gdx.teavm.backends.minigame;

import com.github.xpenatan.gdx.teavm.backends.minigame.bindings.WX;
import java.util.LinkedHashMap;
import java.util.Map;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.typedarrays.Int8Array;

/**
 * WeChat share cards ({@code wx.shareAppMessage}), the top-right 转发 and 分享到朋友圈 menu
 * items, and the query a card carries back into the game.
 * <p>
 * Always supply an image: without one WeChat shares a screenshot of the game. Card images are
 * shown at 5:4. A query has the form {@code k1=v1&k2=v2}. Sharing does not need a user gesture.
 * <p>
 * Callbacks run on WeChat's JS thread, which is the game's only thread, but between frames and
 * possibly before {@code create()}: post work that touches GL or Scene2D to the next frame.
 */
public final class MiniGameShare {

    private static JSObject encodeCanvas;
    private static boolean chatMenu;
    private static boolean timelineMenu;

    private MiniGameShare() {}

    /** Called when the player opens 转发; answer with {@link MenuReply#reply} now or later. */
    public interface MenuProvider {
        void provide(MenuReply reply);
    }

    /**
     * For 转发, WeChat waits about three seconds for a late reply, then sends the placeholder
     * card. For Moments only a reply made before {@link MenuProvider#provide} returns counts.
     */
    public interface MenuReply {
        /** A null title, image or query keeps the placeholder's. */
        void reply(String title, String imagePath, String query);
    }

    public interface LaunchQueryListener {
        /** A non-empty query from the launch or from a later {@code wx.onShow}. */
        void onQuery(Map<String, String> query);
    }

    public static boolean isSupported() {
        return hasShareAppMessage();
    }

    /** Opens WeChat's contact picker for a card. */
    public static void shareAppMessage(String title, String imagePath, String query) {
        nativeShareAppMessage(title, imagePath, query);
    }

    /**
     * Shows 转发 (friends and groups) and answers it with {@code provider}.
     * The placeholder card is sent when the provider replies late or not at all.
     */
    public static void setShareMenu(String title, String imagePath, String query, MenuProvider provider) {
        nativeSetShareMenu(title, imagePath, query, reply -> provider.provide(reply::reply));
        chatMenu = true;
        showShareMenu();
    }

    /**
     * Shows 分享到朋友圈 (Moments) and answers it with {@code provider}, which must reply before
     * it returns: WeChat ignores a late answer, so render the card synchronously. The placeholder
     * is posted when the provider does not reply in time. The card image is also the post's
     * full-size preview, which would otherwise be a screenshot of the game.
     * <p>
     * iOS WeChat ignores a preview the game wrote at runtime (temp or user files), then takes that
     * screenshot, which costs 4-7 s. On iOS the preview is therefore always the placeholder's
     * {@code imagePath}, which must be a file in the code package; the card still is the cover.
     * <p>
     * Moments is a WeChat beta, documented for Android but also offered on iOS. Does nothing where WeChat lacks
     * {@code wx.onShareTimeline}.
     */
    public static void setTimelineShare(String title, String imagePath, String query, MenuProvider provider) {
        if (!hasShareTimeline()) return;
        boolean packagePreviewOnly = "ios".equals(WX.getPlatform());
        nativeSetTimelineShare(title, imagePath, query, packagePreviewOnly, reply -> provider.provide(reply::reply));
        timelineMenu = true;
        showShareMenu();
    }

    /** One menu list for both setters, so either order keeps both items. */
    private static void showShareMenu() {
        nativeShowShareMenu(chatMenu, timelineMenu);
    }

    /** Delivers the launch query now, if any, and each later {@code wx.onShow} query. */
    public static void setLaunchQueryListener(LaunchQueryListener listener) {
        nativeOnLaunchQuery(query -> {
            Map<String, String> values = new LinkedHashMap<>();
            String[] entries = queryEntries(query);
            for (int i = 0; i + 1 < entries.length; i += 2) values.put(entries[i], entries[i + 1]);
            if (!values.isEmpty()) listener.onQuery(values);
        });
    }

    /**
     * Encodes RGBA pixels, rows top to bottom, as a PNG temp file for a share card. WeChat's
     * offscreen canvas encodes natively, about 50-100 ms for 1000×800 on a mid-range Android.
     *
     * @return the temp file path, usable as a card image
     * @throws IllegalStateException if WeChat cannot encode the image
     */
    public static String encodePng(byte[] rgba, int width, int height) {
        if (width <= 0 || height <= 0 || rgba.length != width * height * 4) {
            throw new IllegalArgumentException("Expected " + width + "×" + height + " RGBA pixels, got " + rgba.length + " bytes");
        }
        // The first wx.createCanvas() is the screen; later ones are offscreen.
        if (encodeCanvas == null) encodeCanvas = createCanvas();
        String path = nativeEncodePng(encodeCanvas, Int8Array.copyFromJavaArray(rgba), width, height);
        if (path == null || path.isEmpty()) throw new IllegalStateException("WeChat did not encode the share image");
        return path;
    }

    @JSFunctor
    interface NativeReply extends JSObject {
        void reply(String title, String imagePath, String query);
    }

    @JSFunctor
    interface NativeProvider extends JSObject {
        void provide(NativeReply reply);
    }

    @JSFunctor
    interface NativeQueryListener extends JSObject {
        void onQuery(JSObject query);
    }

    // TeaVM renames @JSBody parameters to short names: scripts declare only long local names.

    @JSBody(script = "return typeof wx !== 'undefined' && typeof wx.shareAppMessage === 'function';")
    private static native boolean hasShareAppMessage();

    @JSBody(params = {"title", "imagePath", "query"}, script = ""
        + "var shareCard = {};"
        + "if (title != null) shareCard.title = title;"
        + "if (imagePath != null) shareCard.imageUrl = imagePath;"
        + "if (query != null) shareCard.query = query;"
        + "wx.shareAppMessage(shareCard);")
    private static native void nativeShareAppMessage(String title, String imagePath, String query);

    @JSBody(params = {"title", "imagePath", "query", "provider"}, script = ""
        + "function shareCardOf(cardTitle, cardImage, cardQuery) {"
        + "  var shareCard = {};"
        + "  if (cardTitle != null) shareCard.title = cardTitle;"
        + "  if (cardImage != null) shareCard.imageUrl = cardImage;"
        + "  if (cardQuery != null) shareCard.query = cardQuery;"
        + "  return shareCard;"
        + "}"
        + "var placeholderCard = shareCardOf(title, imagePath, query);"
        + "wx.onShareAppMessage(function () {"
        + "  var pendingReply = {card: null, resolve: null};"
        + "  provider(function (cardTitle, cardImage, cardQuery) {"
        + "    var shareCard = shareCardOf(cardTitle != null ? cardTitle : placeholderCard.title,"
        + "      cardImage != null ? cardImage : placeholderCard.imageUrl,"
        + "      cardQuery != null ? cardQuery : placeholderCard.query);"
        + "    if (pendingReply.resolve) pendingReply.resolve(shareCard);"
        + "    else if (!pendingReply.card) pendingReply.card = shareCard;"
        + "  });"
        + "  if (pendingReply.card) return pendingReply.card;"
        + "  var lateCard = shareCardOf(placeholderCard.title, placeholderCard.imageUrl, placeholderCard.query);"
        + "  lateCard.promise = new Promise(function (resolveCard) { pendingReply.resolve = resolveCard; });"
        + "  return lateCard;"
        + "});")
    private static native void nativeSetShareMenu(String title, String imagePath, String query, NativeProvider provider);

    @JSBody(script = "return typeof wx !== 'undefined' && typeof wx.onShareTimeline === 'function';")
    private static native boolean hasShareTimeline();

    @JSBody(params = {"title", "imagePath", "query", "packagePreviewOnly", "provider"}, script = ""
        + "function timelineCardOf(cardTitle, cardImage, cardQuery) {"
        + "  var timelineCard = {};"
        + "  if (cardTitle != null) timelineCard.title = cardTitle;"
        + "  if (cardImage != null) timelineCard.imageUrl = cardImage;"
        + "  var previewImage = packagePreviewOnly ? imagePath : cardImage;"
        + "  if (previewImage != null) timelineCard.imagePreviewUrl = previewImage;"
        + "  if (cardQuery != null) timelineCard.query = cardQuery;"
        + "  return timelineCard;"
        + "}"
        + "wx.onShareTimeline(function () {"
        + "  var timelineAnswer = {card: null, closed: false};"
        + "  provider(function (cardTitle, cardImage, cardQuery) {"
        + "    if (timelineAnswer.closed || timelineAnswer.card) return;"
        + "    timelineAnswer.card = timelineCardOf(cardTitle != null ? cardTitle : title,"
        + "      cardImage != null ? cardImage : imagePath, cardQuery != null ? cardQuery : query);"
        + "  });"
        + "  timelineAnswer.closed = true;"
        + "  return timelineAnswer.card || timelineCardOf(title, imagePath, query);"
        + "});")
    private static native void nativeSetTimelineShare(String title, String imagePath, String query, boolean packagePreviewOnly,
        NativeProvider provider);

    @JSBody(params = {"chatMenu", "timelineMenu"}, script = ""
        + "var shownMenus = [];"
        + "if (chatMenu) shownMenus.push('shareAppMessage');"
        + "if (timelineMenu) shownMenus.push('shareTimeline');"
        + "wx.showShareMenu({menus: shownMenus});")
    private static native void nativeShowShareMenu(boolean chatMenu, boolean timelineMenu);

    @JSBody(params = "listener", script = ""
        + "function deliverQuery(enterOptions) {"
        + "  var launchQuery = enterOptions && enterOptions.query;"
        + "  if (launchQuery && typeof launchQuery === 'object' && Object.keys(launchQuery).length) listener(launchQuery);"
        + "}"
        + "deliverQuery(wx.getLaunchOptionsSync());"
        + "wx.onShow(deliverQuery);")
    private static native void nativeOnLaunchQuery(NativeQueryListener listener);

    @JSBody(params = "query", script = ""
        + "var queryEntries = [];"
        + "Object.keys(query).forEach(function (queryKey) {"
        + "  var queryValue = query[queryKey];"
        + "  if (typeof queryValue === 'string') queryEntries.push(queryKey, queryValue);"
        + "});"
        + "return queryEntries;")
    private static native String[] queryEntries(JSObject query);

    @JSBody(script = "return wx.createCanvas();")
    private static native JSObject createCanvas();

    @JSBody(params = {"encodeCanvas", "pixels", "width", "height"}, script = ""
        + "encodeCanvas.width = width;"
        + "encodeCanvas.height = height;"
        + "var encodeContext = encodeCanvas.getContext('2d');"
        + "var imageData = encodeContext.createImageData(width, height);"
        + "imageData.data.set(new Uint8ClampedArray(pixels.buffer, pixels.byteOffset, pixels.length));"
        + "encodeContext.putImageData(imageData, 0, 0);"
        + "return encodeCanvas.toTempFilePathSync({x: 0, y: 0, width: width, height: height,"
        + "  destWidth: width, destHeight: height, fileType: 'png'});")
    private static native String nativeEncodePng(JSObject encodeCanvas, Int8Array pixels, int width, int height);
}
