> Source: https://developers.weixin.qq.com/miniprogram/dev/api/route/router/wx.router.html

## wx.router

基础库 2.29.2 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

相关文档: [自定义路由](../../../framework/runtime/skyline/custom-route.html)

router 对象，可以通过 `wx.router` 获取。

## # 方法

### # router.addRouteBuilder(string routeType, CustomRouteBuilder routeBuilder)

添加自定义路由配置

### # router.removeRouteBuilder(string routeType)

移除自定义路由配置

### # router.getRouteContext(Object this)

获取页面对应的自定义路由上下文对象
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)