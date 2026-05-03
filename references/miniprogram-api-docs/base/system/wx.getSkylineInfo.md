> Source: https://developers.weixin.qq.com/miniprogram/dev/api/base/system/wx.getSkylineInfo.html

## wx.getSkylineInfo(Object object)

基础库 2.26.2 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**以 [Promise 风格](../../../framework/app-service/api.html#异步-API-返回-Promise) 调用**：不支持

**小程序插件**：支持，需要小程序基础库版本不低于 [2.26.2](../../../framework/compatibility.html)

**微信 鸿蒙 OS 版**：支持

## # 功能描述

获取当前运行环境对于 [Skyline 渲染引擎](../../../framework/runtime/skyline/introduction.html) 的支持情况

## # 参数

### # Object object
 | 属性 | 类型 | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| success | function |  | 否 | 接口调用成功的回调函数 |
| fail | function |  | 否 | 接口调用失败的回调函数 |
| complete | function |  | 否 | 接口调用结束的回调函数（调用成功、失败都会执行） |
#### # object.success 回调函数

##### # 参数
 [#](#Object-res) Object res
当前运行环境对于 [Skyline 渲染引擎](../../../framework/runtime/skyline/introduction.html) 的支持情况
 |  | 属性 | 类型 | 说明 |
| --- | --- | --- | --- |
|  | isSupported | boolean | 当前运行环境是否支持 Skyline 渲染引擎 |
|  | version | string | 当前运行环境 Skyline 渲染引擎 的版本号，形如 `0.9.7` |
|  | reason | string | 当前运行环境不支持 Skyline 渲染引擎 的原因，仅在 `isSupported` 为 `false` 时出现 |
|  | 合法值 | 说明 |
| client not supported | 当前微信客户端不支持 Skyline 渲染引擎，可以尝试通过升级微信客户端解决 |
| baselib not supported | 当前基础库不支持 Skyline 渲染引擎，基础库会自动更新到当前客户端所能支持的最新的版本，基础库不支持时也可以尝试通过升级微信客户端解决 |
| a-b test not enabled | 命中了 _We 分析_ 平台上的 AB 实验关闭的情况。详细可以查看 Skyline 起步 > 配置 We 分析 AB 实验 一节 |
| SwitchRender option set to webview | 本地调试的快捷切换入口被设置为了强制使用 Webview. 详情可以查看 Skyline 起步 > 快捷切换入口 一节 | The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)