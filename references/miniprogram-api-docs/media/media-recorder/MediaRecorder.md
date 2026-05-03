> Source: https://developers.weixin.qq.com/miniprogram/dev/api/media/media-recorder/MediaRecorder.html

## MediaRecorder

基础库 2.11.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

可通过 [wx.createMediaRecorder](wx.createMediaRecorder.html) 创建。

[MediaRecorder](MediaRecorder.html) WebGL 画面录制器，可以进行录制相关操作，在结束录制时导出视频文件

## # 方法

### # Promise MediaRecorder.pause()

暂停录制

### # Promise MediaRecorder.resume()

恢复录制

### # Promise MediaRecorder.start()

开始录制

### # Promise MediaRecorder.stop()

结束录制

### # Promise MediaRecorder.requestFrame(function callback)

请求下一帧录制，在 callback 里完成一帧渲染后开始录制当前帧

### # MediaRecorder.on(string eventName, function callback)

注册监听录制事件的回调函数。当对应事件触发时，回调函数会被执行。

### # MediaRecorder.off(string eventName, function callback)

取消监听录制事件。当对应事件触发时，该回调函数不再执行。

### # Promise MediaRecorder.destroy()

销毁录制器
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)