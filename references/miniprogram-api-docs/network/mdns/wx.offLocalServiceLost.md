> Source: https://developers.weixin.qq.com/miniprogram/dev/api/network/mdns/wx.offLocalServiceLost.html

## wx.offLocalServiceLost(function listener)

基础库 2.4.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**小程序插件**：支持，需要小程序基础库版本不低于 [2.15.0](../../../framework/compatibility.html)

相关文档: [局域网通信](../../../framework/ability/mDNS.html)

## # 功能描述

移除 mDNS 服务离开的事件的监听函数

## # 参数

### # function listener

onLocalServiceLost 传入的监听函数。不传此参数则移除所有监听函数。

## # 示例代码

```js
const listener = function (res) { console.log(res) }

wx.onLocalServiceLost(listener)
wx.offLocalServiceLost(listener) // 需传入与监听时同一个的函数对象
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)