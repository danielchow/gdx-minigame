> Source: https://developers.weixin.qq.com/miniprogram/dev/api/device/network/wx.onNetworkStatusChange.html

## wx.onNetworkStatusChange(function listener)

基础库 1.1.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**小程序插件**：支持

**微信 Windows 版**：支持

**微信 鸿蒙 OS 版**：支持

相关文档: [弱网体验优化](../../../framework/performance/weak-network.html)、[网络调优](../../../framework/performance/network.html)

## # 功能描述

监听网络状态变化事件

## # 参数

### # function listener

网络状态变化事件的监听函数

#### # 参数

##### # Object res
 |  | 属性 | 类型 | 说明 |
| --- | --- | --- | --- |
|  | isConnected | boolean | 当前是否有网络连接 |
|  | networkType | string | 网络类型 |
|  | 合法值 | 说明 |
| wifi | wifi 网络 |
| 2g | 2g 网络 |
| 3g | 3g 网络 |
| 4g | 4g 网络 |
| 5g | 5g 网络 |
| unknown | Android 下不常见的网络类型 |
| none | 无网络 |
## # 示例代码

```js
wx.onNetworkStatusChange(function (res) {
  console.log(res.isConnected)
  console.log(res.networkType)
})
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)