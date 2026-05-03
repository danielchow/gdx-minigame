> Source: https://developers.weixin.qq.com/miniprogram/dev/api/wxml/NodesRef.context.html

## SelectorQuery NodesRef.context(function callback)

基础库 2.4.2 开始支持，低版本需做[兼容处理](../../framework/compatibility.html)。

**小程序插件**：支持

相关文档: [获取界面上的节点信息](../../framework/view/selector.html)

## # 功能描述

添加节点的 Context 对象查询请求。目前支持 [VideoContext](../media/video/VideoContext.html)、[CanvasContext](../canvas/CanvasContext.html)、[LivePlayerContext](../media/live/LivePlayerContext.html)、[EditorContext](../media/editor/EditorContext.html)和 [MapContext](../media/map/MapContext.html) 的获取。

## # 参数

### # function callback

回调函数，在执行 `SelectorQuery.exec` 方法后，返回节点信息。

#### # 参数

##### # Object res
 | 属性 | 类型 | 说明 |
| --- | --- | --- |
| context | Object | 节点对应的 Context 对象 |
## # 返回值

### # SelectorQuery

## # 示例代码

```js
Page({
  getContext () {
    wx.createSelectorQuery().select('.the-video-class').context(function(res){
      console.log(res.context) // 节点对应的 Context 对象。如：选中的节点是 <video> 组件，那么此处即返回 VideoContext 对象
    }).exec()
  }
})
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)