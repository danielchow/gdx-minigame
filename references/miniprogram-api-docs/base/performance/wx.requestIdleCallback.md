> Source: https://developers.weixin.qq.com/miniprogram/dev/api/base/performance/wx.requestIdleCallback.html

## wx.requestIdleCallback(function callback, Object object)

基础库 3.10.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**小程序插件**：不支持

**微信 Windows 版**：支持

**微信 Mac 版**：支持

**微信 鸿蒙 OS 版**：支持

## # 功能描述

注册一个函数，将在空闲时期被调用

## # 参数

### # function callback

### # Object object
 | 属性 | 类型 | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| timeout | number |  | 否 |  |
## # 示例代码

```js
const IdleCallbackId = wx.requestIdleCallback(() => {
  console.log('idle')
}, {
  timeout: 3000
})
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)