> Source: https://developers.weixin.qq.com/miniprogram/dev/api/device/keyboard/wx.offKeyUp.html

## wx.offKeyUp(function listener)

基础库 3.6.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**小程序插件**：不支持

## # 功能描述

移除小程序全局键盘按键弹起事件的监听函数

## # 参数

### # function listener

onKeyUp 传入的监听函数。不传此参数则移除所有监听函数。

## # 示例代码

```js
const listener = function (res) { console.log(res) }

wx.onKeyUp(listener)
wx.offKeyUp(listener) // 需传入与监听时同一个的函数对象
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)