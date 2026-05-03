> Source: https://developers.weixin.qq.com/miniprogram/dev/api/device/battery/wx.onBatteryInfoChange.html

## wx.onBatteryInfoChange(function listener)

基础库 3.5.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**小程序插件**：不支持

**微信 Windows 版**：支持

## # 功能描述

监听电池信息变化事件，目前只支持监听省电模式的切换

## # 参数

### # function listener

电池信息变化事件的监听函数

#### # 参数

##### # Object res
 | 属性 | 类型 | 说明 |
| --- | --- | --- |
| isLowPowerModeEnabled | boolean | 是否处于省电模式 |
## # 示例代码

```js
const cb = res => {
  console.log(res.isLowPowerModeEnabled)
}
wx.onBatteryInfoChange(cb)
// 取消监听
wx.offBatteryInfoChange(cb)
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)