> Source: https://developers.weixin.qq.com/miniprogram/dev/api/ai/visionkit/wx.isVKSupport.html

## boolean wx.isVKSupport(string version)

基础库 2.22.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**小程序插件**：支持，需要小程序基础库版本不低于 [2.22.0](../../../framework/compatibility.html)

## # 功能描述

判断支持版本

## # 参数

### # string version

**version 的合法值**
 | 值 | 说明 | 最低版本 |
| --- | --- | --- |
| v1 | 旧版本 |  |
| v2 | v2 版本，目前只有 iOS 基础库 2.22.0 以上支持 |  |
## # 返回值

### # boolean

是否支持对应版本的 vision kit

## # 示例代码

```js
const isSupportV2 = wx.isVKSupport('v2')
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)