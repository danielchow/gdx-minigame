> Source: https://developers.weixin.qq.com/miniprogram/dev/api/base/wx.base64ToArrayBuffer.html

## ArrayBuffer wx.base64ToArrayBuffer(string base64)

从基础库 [2.4.0](../../framework/compatibility.html) 开始，本接口停止维护

基础库 1.1.0 开始支持，低版本需做[兼容处理](../../framework/compatibility.html)。

**小程序插件**：支持

**微信 Windows 版**：支持

**微信 Mac 版**：支持

**微信 鸿蒙 OS 版**：支持

## # 功能描述

将 Base64 字符串转成 ArrayBuffer 对象

## # 参数

### # string base64

要转化成 ArrayBuffer 对象的 Base64 字符串

## # 返回值

### # ArrayBuffer

ArrayBuffer 对象

## # 示例代码

```javascript
const base64 = 'CxYh'
const arrayBuffer = wx.base64ToArrayBuffer(base64)
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)