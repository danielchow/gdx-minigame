> Source: https://developers.weixin.qq.com/miniprogram/dev/api/device/bluetooth-peripheral/BLEPeripheralServer.onCharacteristicWriteRequest.html

## BLEPeripheralServer.onCharacteristicWriteRequest(function listener)

基础库 2.10.3 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**小程序插件**：不支持

**微信 鸿蒙 OS 版**：支持

相关文档: [蓝牙介绍](../../../framework/device/bluetooth.html)

## # 功能描述

监听已连接的设备请求写当前外围设备的特征值事件。收到该消息后需要立刻调用 [writeCharacteristicValue](BLEPeripheralServer.writeCharacteristicValue.html) 写回数据，否则主机不会收到响应。

## # 参数

### # function listener

已连接的设备请求写当前外围设备的特征值事件的监听函数

#### # 参数

##### # Object res
 | 属性 | 类型 | 说明 |
| --- | --- | --- |
| serviceId | String | 蓝牙特征对应服务的 UUID |
| characteristicId | String | 蓝牙特征的 UUID |
| callbackId | Number | 唯一标识码，调用 writeCharacteristicValue 时使用 |
| value | ArrayBuffer | 请求写入特征的二进制数据值 | The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)