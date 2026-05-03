> Source: https://developers.weixin.qq.com/miniprogram/dev/api/ui/menu/wx.offUserOffTranslation.html

## wx.offUserOffTranslation(function listener)

基础库 3.14.2 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**小程序插件**：不支持

**微信 Windows 版**：支持

**微信 鸿蒙 OS 版**：支持

## # 功能描述

移除用户主动取消翻译的事件的监听函数

## # 参数

### # function listener

onUserOffTranslation 传入的监听函数。不传此参数则移除所有监听函数。

## # 示例代码

```js
const listener = function (res) { console.log(res) }

wx.onUserOffTranslation(listener)
wx.offUserOffTranslation(listener) // 需传入与监听时同一个的函数对象
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)