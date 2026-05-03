> Source: https://developers.weixin.qq.com/miniprogram/dev/api/base/app/app-event/wx.offUnhandledRejection.html

## wx.offUnhandledRejection(function listener)

基础库 2.10.0 开始支持，低版本需做[兼容处理](../../../../framework/compatibility.html)。

**小程序插件**：不支持

**微信 鸿蒙 OS 版**：支持

## # 功能描述

移除未处理的 Promise 拒绝事件的监听函数

## # 参数

### # function listener

onUnhandledRejection 传入的监听函数。不传此参数则移除所有监听函数。

## # 示例代码

```js
const listener = function (res) { console.log(res) }

wx.onUnhandledRejection(listener)
wx.offUnhandledRejection(listener) // 需传入与监听时同一个的函数对象
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)