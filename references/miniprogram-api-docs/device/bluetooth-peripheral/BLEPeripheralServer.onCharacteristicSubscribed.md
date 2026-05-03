> Source: https://developers.weixin.qq.com/miniprogram/dev/api/device/bluetooth-peripheral/BLEPeripheralServer.onCharacteristicSubscribed.html

## BLEPeripheralServer.onCharacteristicSubscribed(function listener)

基础库 2.13.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**小程序插件**：不支持

相关文档: [蓝牙介绍](../../../framework/device/bluetooth.html)

## # 功能描述

监听特征订阅事件，仅 iOS 支持。

## # 参数

### # function listener

特征订阅事件的监听函数

#### # 参数

##### # Object res
 | 属性 | 类型 | 说明 |
| --- | --- | --- |
| serviceId | String | 蓝牙特征对应服务的 UUID |
| characteristicId | String | 蓝牙特征的 UUID | The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)