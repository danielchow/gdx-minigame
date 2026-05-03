> Source: https://developers.weixin.qq.com/miniprogram/dev/api/device/screen/wx.onScreenRecordingStateChanged.html

## wx.onScreenRecordingStateChanged(function listener)

基础库 2.24.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**小程序插件**：不支持

**微信 iOS 版**：支持

**微信 Android 版**：不支持

## # 功能描述

监听用户录屏事件。

## # 参数

### # function listener

用户录屏事件的监听函数

#### # 参数

##### # Object res
 |  | 属性 | 类型 | 说明 |
| --- | --- | --- | --- |
|  | state | string | 录屏状态 |
|  | 合法值 | 说明 |
| start | 开始录屏 |
| stop | 结束录屏 |
## # 示例代码

```javascript
// 监听用户录屏事件
const handler = function (res) {
  console.log(res.state)
}
wx.onScreenRecordingStateChanged(handler)

// 取消监听用户录屏事件
wx.offScreenRecordingStateChanged(handler)
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)