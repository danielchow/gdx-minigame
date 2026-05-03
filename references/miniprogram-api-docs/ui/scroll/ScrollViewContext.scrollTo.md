> Source: https://developers.weixin.qq.com/miniprogram/dev/api/ui/scroll/ScrollViewContext.scrollTo.html

## ScrollViewContext.scrollTo(Object object)

基础库 2.14.4 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**小程序插件**：支持

**微信 Windows 版**：支持

**微信 Mac 版**：支持

## # 功能描述

滚动至指定位置

## # 参数

### # Object object
 | 属性 | 类型 | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| top | number |  | 否 | 顶部距离 |
| left | number |  | 否 | 左边界距离 |
| velocity | number |  | 否 | 初始速度 (webview 仅在 iOS 下生效；skyline 在 3.14.3 后支持) |
| duration | number |  | 否 | 滚动动画时长 (webview 仅在 iOS 下生效；skyline 在 3.14.3 后支持) |
| animated | boolean |  | 否 | 是否启用滚动动画 | The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)