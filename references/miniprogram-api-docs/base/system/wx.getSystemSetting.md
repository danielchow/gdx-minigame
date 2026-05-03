> Source: https://developers.weixin.qq.com/miniprogram/dev/api/base/system/wx.getSystemSetting.html

## Object wx.getSystemSetting()

基础库 2.20.1 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**小程序插件**：支持，需要小程序基础库版本不低于 [2.21.3](../../../framework/compatibility.html)

**微信 Windows 版**：支持

**微信 Mac 版**：支持

**微信 鸿蒙 OS 版**：支持

## # 功能描述

获取设备设置

## # 返回值

### # Object
 |  | 属性 | 类型 | 说明 |
| --- | --- | --- | --- |
|  | bluetoothEnabled | boolean | 蓝牙的系统开关 |
|  | locationEnabled | boolean | 地理位置的系统开关 |
|  | wifiEnabled | boolean | Wi-Fi 的系统开关 |
|  | deviceOrientation | string | 设备方向（注意：IOS客户端横屏游戏获取deviceOrientation可能不准，建议以屏幕宽高为准） |
|  | 合法值 | 说明 |
| portrait | 竖屏 |
| landscape | 横屏 |
## # 示例代码

```js
const systemSetting = wx.getSystemSetting()

console.log(systemSetting.bluetoothEnabled)
console.log(systemSetting.deviceOrientation)
console.log(systemSetting.locationEnabled)
console.log(systemSetting.wifiEnabled)
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)