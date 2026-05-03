> Source: https://developers.weixin.qq.com/miniprogram/dev/api/base/app/app-event/wx.offAudioInterruptionBegin.html

## wx.offAudioInterruptionBegin(function listener)

基础库 2.6.2 开始支持，低版本需做[兼容处理](../../../../framework/compatibility.html)。

**小程序插件**：支持，需要小程序基础库版本不低于 [2.15.0](../../../../framework/compatibility.html)

**微信 Windows 版**：支持

**微信 Mac 版**：支持

## # 功能描述

移除音频因为受到系统占用而被中断开始事件的监听函数

## # 参数

### # function listener

onAudioInterruptionBegin 传入的监听函数。不传此参数则移除所有监听函数。

## # 示例代码

```js
const listener = function (res) { console.log(res) }

wx.onAudioInterruptionBegin(listener)
wx.offAudioInterruptionBegin(listener) // 需传入与监听时同一个的函数对象
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)