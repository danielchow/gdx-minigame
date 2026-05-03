> Source: https://developers.weixin.qq.com/miniprogram/dev/api/ext/wx.getExtConfigSync.html

## Object wx.getExtConfigSync()

基础库 1.1.0 开始支持，低版本需做[兼容处理](../../framework/compatibility.html)。

**小程序插件**：不支持

**微信 Windows 版**：支持

**微信 Mac 版**：支持

## # 功能描述

[wx.getExtConfig](wx.getExtConfig.html) 的同步版本。

## # 返回值

### # Object

第三方平台自定义的数据

## # Tips

- 本接口暂时无法通过 [wx.canIUse](../base/wx.canIUse.html) 判断是否兼容，开发者需要自行判断 [wx.getExtConfigSync](wx.getExtConfigSync.html) 是否存在来兼容


```js
let extConfig = wx.getExtConfigSync? wx.getExtConfigSync(): {}
console.log(extConfig)
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)