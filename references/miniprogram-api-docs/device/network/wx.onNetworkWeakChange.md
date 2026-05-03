> Source: https://developers.weixin.qq.com/miniprogram/dev/api/device/network/wx.onNetworkWeakChange.html

## wx.onNetworkWeakChange(function listener)

基础库 2.21.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**小程序插件**：不支持

**微信 Windows 版**：支持

相关文档: [弱网体验优化](../../../framework/performance/weak-network.html)、[网络调优](../../../framework/performance/network.html)

## # 功能描述

监听弱网状态变化事件

## # 参数

### # function listener

弱网状态变化事件的监听函数

#### # 参数

##### # Object res
 | 属性 | 类型 | 说明 |
| --- | --- | --- |
| weakNet | boolean | 当前是否处于弱网状态 |
| networkType | string | 当前网络类型 |
## # 示例代码

```js
wx.onNetworkWeakChange(function (res) {
  console.log(res.weakNet)
  console.log(res.networkType)
})
// 取消监听
wx.offNetworkWeakChange()
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)