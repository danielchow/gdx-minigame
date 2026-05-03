> Source: https://developers.weixin.qq.com/miniprogram/dev/api/network/tcp/TCPSocket.offClose.html

## TCPSocket.offClose(function listener)

**小程序插件**：不支持

**微信 Windows 版**：支持

**微信 Mac 版**：支持

**微信 鸿蒙 OS 版**：支持

相关文档: [网络使用说明](../../../framework/ability/network.html)

## # 功能描述

移除一旦 socket 完全关闭就发出该事件的监听函数

## # 参数

### # function listener

onClose 传入的监听函数。不传此参数则移除所有监听函数。

## # 示例代码

```js
const listener = function (res) { console.log(res) }

TCPSocket.onClose(listener)
TCPSocket.offClose(listener) // 需传入与监听时同一个的函数对象
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)