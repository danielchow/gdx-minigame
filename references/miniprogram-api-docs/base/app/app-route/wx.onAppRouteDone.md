> Source: https://developers.weixin.qq.com/miniprogram/dev/api/base/app/app-route/wx.onAppRouteDone.html

## wx.onAppRouteDone(function listener)

基础库 3.5.5 开始支持，低版本需做[兼容处理](../../../../framework/compatibility.html)。

**小程序插件**：支持，需要小程序基础库版本不低于 [3.5.5](../../../../framework/compatibility.html)

**微信 鸿蒙 OS 版**：支持

## # 功能描述

监听当前路由动画执行完成的事件监听，详见 [页面路由监听](../../../../framework/app-service/route-event-listener.html)。

## # 参数

### # function listener

当前路由动画执行完成的事件的监听函数

#### # 参数

##### # Object res
 | 属性 | 类型 | 说明 |
| --- | --- | --- |
| path | string | 页面路径 |
| query | Object | 路由参数 |
| openType | string | 路由打开类型 |
| webviewId | number | 当前页面 id |
| timeStamp | number | 路由下发的时间戳 |
| routeEventId | string | 路由事件 id |
## # 注意

在低于 3.5.5 版本的基础库中也存在此接口，但参数可能与当前文档不同，请注意。

## # 示例代码

```js
const func = function (res) {
  console.log(res)
}
wx.onAppRouteDone(func)
// 取消监听
wx.offAppRouteDone(func)
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)