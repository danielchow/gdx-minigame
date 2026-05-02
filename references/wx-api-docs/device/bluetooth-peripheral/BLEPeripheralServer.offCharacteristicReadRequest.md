> Source: https://developers.weixin.qq.com/minigame/dev/api/device/bluetooth-peripheral/BLEPeripheralServer.offCharacteristicReadRequest.html
# BLEPeripheralServer.offCharacteristicReadRequest(function listener)
基础库 2.10.3 开始支持，低版本需做[兼容处理](../../../guide/runtime/client-lib/compatibility.html)。

**微信 鸿蒙 OS 版**：支持

相关文档: [蓝牙介绍](../../../guide/device/bluetooth.html)
## 功能描述
移除已连接的设备请求读当前外围设备的特征值事件的监听函数
## 参数
### function listener
onCharacteristicReadRequest 传入的监听函数。不传此参数则移除所有监听函数。
## 示例代码
```js
const listener = function (res) { console.log(res) }

BLEPeripheralServer.onCharacteristicReadRequest(listener)
BLEPeripheralServer.offCharacteristicReadRequest(listener) // 需传入与监听时同一个的函数对象
```
