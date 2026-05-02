> Source: https://developers.weixin.qq.com/minigame/dev/api/device/bluetooth-peripheral/BLEPeripheralServer.offCharacteristicUnsubscribed.html
# BLEPeripheralServer.offCharacteristicUnsubscribed(function listener)
基础库 2.13.0 开始支持，低版本需做[兼容处理](../../../guide/runtime/client-lib/compatibility.html)。

相关文档: [蓝牙介绍](../../../guide/device/bluetooth.html)
## 功能描述
移除取消特征订阅事件的监听函数
## 参数
### function listener
onCharacteristicUnsubscribed 传入的监听函数。不传此参数则移除所有监听函数。
## 示例代码
```js
const listener = function (res) { console.log(res) }

BLEPeripheralServer.onCharacteristicUnsubscribed(listener)
BLEPeripheralServer.offCharacteristicUnsubscribed(listener) // 需传入与监听时同一个的函数对象
```
