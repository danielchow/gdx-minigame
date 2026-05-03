> Source: https://developers.weixin.qq.com/miniprogram/dev/api/base/app/app-route/wx.onBeforePageLoad.html

## wx.onBeforePageLoad(function listener)

基础库 3.5.5 开始支持，低版本需做[兼容处理](../../../../framework/compatibility.html)。

**小程序插件**：支持，需要小程序基础库版本不低于 [3.5.5](../../../../framework/compatibility.html)

**微信 鸿蒙 OS 版**：支持

## # 功能描述

监听路由事件引起新的页面实例化时，页面实例化前的事件监听，详见 [页面路由监听](../../../../framework/app-service/route-event-listener.html)。

## # 参数

### # function listener

路由事件的监听函数

#### # 参数

##### # Object res
 |  | 属性 | 类型 | 说明 |
| --- | --- | --- | --- |
|  | path | string | 页面路径 |
|  | query | Object | 路由参数 |
|  | componentFramework | string | 组件框架 |
|  | 合法值 | 说明 |
| exparser | 旧版小程序组件框架 |
| glass-easel | 新版小程序组件框架 |  openType string 路由打开类型  routeEventId string 路由事件 id
新旧版本小程序组件框架的说明详见：[glass-easel：新版微信小程序组件框架](../../../../framework/custom-component/glass-easel/introduction.html)

## # 示例代码

```js
const func = function (res) {
  console.log(res)
}
wx.onBeforePageLoad(func)
// 取消监听
wx.offBeforePageLoad(func)
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)