> Source: https://developers.weixin.qq.com/miniprogram/dev/api/media/media-recorder/MediaRecorder.on.html

## MediaRecorder.on(string eventName, function callback)

基础库 2.11.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**小程序插件**：支持

## # 功能描述

注册监听录制事件的回调函数。当对应事件触发时，回调函数会被执行。

## # 参数

### # string eventName

事件名

**eventName 的合法值**
 | 值 | 说明 | 最低版本 |
| --- | --- | --- |
| start | 录制开始事件。 |  |
| stop | 录制结束事件。返回 {tempFilePath, duration, fileSize} |  |
| pause | 录制暂停事件。 |  |
| resume | 录制继续事件。 |  |
| timeupdate | 录制时间更新事件。 |  |
### # function callback

事件触发时执行的回调函数
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)