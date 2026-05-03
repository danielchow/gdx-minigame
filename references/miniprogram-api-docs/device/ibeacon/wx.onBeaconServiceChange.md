> Source: https://developers.weixin.qq.com/miniprogram/dev/api/device/ibeacon/wx.onBeaconServiceChange.html

## wx.onBeaconServiceChange(function listener)

基础库 1.2.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**小程序插件**：支持，需要小程序基础库版本不低于 [1.9.6](../../../framework/compatibility.html)

**微信 鸿蒙 OS 版**：支持

相关文档: [蓝牙信标 (Beacon)](../../../framework/device/beacon.html)

## # 功能描述

监听 Beacon 服务状态变化事件，仅能注册一个监听

## # 参数

### # function listener

Beacon 服务状态变化事件的监听函数

#### # 参数

##### # Object res
 | 属性 | 类型 | 说明 |
| --- | --- | --- |
| available | boolean | 服务目前是否可用 |
| discovering | boolean | 目前是否处于搜索状态 |
## # 示例代码

```js
wx.onBeaconServiceChange(res => {
   console.log(res.available, res.discovering)
})
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)