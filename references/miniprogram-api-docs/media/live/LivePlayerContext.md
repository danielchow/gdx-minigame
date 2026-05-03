> Source: https://developers.weixin.qq.com/miniprogram/dev/api/media/live/LivePlayerContext.html

## LivePlayerContext

相关文档: [live-player 组件](../../../component/live-player.html)

`LivePlayerContext` 实例，可通过 [wx.createLivePlayerContext](wx.createLivePlayerContext.html) 获取。

[LivePlayerContext](LivePlayerContext.html) 通过 `id` 跟一个 [live-player](../../../component/live-player.html) 组件绑定，操作对应的 [live-player](../../../component/live-player.html) 组件。

## # 方法

### # LivePlayerContext.play()

播放

### # LivePlayerContext.stop()

停止

### # LivePlayerContext.mute()

静音

### # LivePlayerContext.pause()

暂停

### # LivePlayerContext.resume()

恢复

### # LivePlayerContext.requestFullScreen(Object object)

进入全屏

### # LivePlayerContext.exitFullScreen()

退出全屏

### # LivePlayerContext.exitPictureInPicture()

退出小窗，该方法可在任意页面调用

### # LivePlayerContext.snapshot(Object object)

截图

### # LivePlayerContext.requestBackgroundPlayback()

进入后台小窗播放模式。

### # LivePlayerContext.exitBackgroundPlayback()

退出后台小窗播放模式。

### # LivePlayerContext.startCasting()

开始投屏, 拉起半屏搜索设备。仅支持在 tap 事件回调内调用。

### # LivePlayerContext.switchCasting()

切换投屏设备。仅支持在 tap 事件回调内调用。

### # LivePlayerContext.reconnectCasting()

重连投屏设备。仅支持在 tap 事件回调内调用。

### # LivePlayerContext.exitCasting()

退出投屏。仅支持在 tap 事件回调内调用。

## # 示例代码

[在开发者工具中预览效果](https://developers.weixin.qq.com/s/UzWEzmm763Y4)
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)