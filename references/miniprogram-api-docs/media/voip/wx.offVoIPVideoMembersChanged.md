> Source: https://developers.weixin.qq.com/miniprogram/dev/api/media/voip/wx.offVoIPVideoMembersChanged.html

## wx.offVoIPVideoMembersChanged(function listener)

基础库 2.11.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**小程序插件**：不支持

**微信 Windows 版**：支持

**微信 Mac 版**：支持

## # 功能描述

移除实时语音通话成员视频状态变化事件的监听函数

## # 参数

### # function listener

onVoIPVideoMembersChanged 传入的监听函数。不传此参数则移除所有监听函数。

## # 示例代码

```js
const listener = function (res) { console.log(res) }

wx.onVoIPVideoMembersChanged(listener)
wx.offVoIPVideoMembersChanged(listener) // 需传入与监听时同一个的函数对象
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)