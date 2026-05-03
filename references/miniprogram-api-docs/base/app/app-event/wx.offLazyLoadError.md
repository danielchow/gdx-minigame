> Source: https://developers.weixin.qq.com/miniprogram/dev/api/base/app/app-event/wx.offLazyLoadError.html

## wx.offLazyLoadError(function listener)

基础库 2.24.3 开始支持，低版本需做[兼容处理](../../../../framework/compatibility.html)。

**小程序插件**：不支持

**微信 鸿蒙 OS 版**：支持

相关文档: [分包异步化](../../../../framework/subpackages/async.html)

## # 功能描述

移除小程序异步组件加载失败事件的监听函数

## # 参数

### # function listener

onLazyLoadError 传入的监听函数。不传此参数则移除所有监听函数。

## # 示例代码

```js
const listener = function (res) { console.log(res) }

wx.onLazyLoadError(listener)
wx.offLazyLoadError(listener) // 需传入与监听时同一个的函数对象
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)