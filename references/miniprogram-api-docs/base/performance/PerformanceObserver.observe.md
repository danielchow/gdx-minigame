> Source: https://developers.weixin.qq.com/miniprogram/dev/api/base/performance/PerformanceObserver.observe.html

## PerformanceObserver.observe(Object options)

基础库 2.11.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**小程序插件**：不支持

## # 功能描述

开始监听

## # 参数

### # Object options

设置 type 监听单个类型的指标，设置 entryTypes 监听多个类型指标。
 |  | 属性 | 类型 | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- | --- | --- |
|  | type | string |  | 否 | 指标类型。不能和 entryTypes 同时使用 |
|  | 合法值 | 说明 |
| navigation | 路由 |
| render | 渲染 |
| script | 脚本 |
| loadPackage | 代码包下载 |  entryTypes Array.<string>  否 指标类型列表。不能和 type 同时使用。 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)