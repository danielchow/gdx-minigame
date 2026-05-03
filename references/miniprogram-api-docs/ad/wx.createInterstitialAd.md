> Source: https://developers.weixin.qq.com/miniprogram/dev/api/ad/wx.createInterstitialAd.html

## InterstitialAd wx.createInterstitialAd(Object object)

基础库 2.6.0 开始支持，低版本需做[兼容处理](../../framework/compatibility.html)。

**小程序插件**：支持，需要小程序基础库版本不低于 [2.8.1](../../framework/compatibility.html)

## # 功能描述

创建插屏广告组件。请通过 [wx.getSystemInfoSync()](../base/system/wx.getSystemInfoSync.html) 返回对象的 SDKVersion 判断基础库版本号后再使用该 API。每次调用该方法创建插屏广告都会返回一个全新的实例（小程序端的插屏广告实例不允许跨页面使用）。

## # 参数

### # Object object
 | 属性 | 类型 | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| adUnitId | string |  | 是 | 广告单元 id |
## # 返回值

### # InterstitialAd

插屏广告组件
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)