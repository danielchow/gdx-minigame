> Source: https://developers.weixin.qq.com/miniprogram/dev/api/ui/scroll/ScrollViewContext.html

## ScrollViewContext

基础库 2.14.4 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

增强 ScrollView 实例，可通过 [wx.createSelectorQuery](../../wxml/wx.createSelectorQuery.html) 的 [NodesRef.node](../../wxml/NodesRef.node.html) 方法获取。 仅在 scroll-view 组件开启 enhanced 属性后生效。

## # 属性

### # boolean scrollEnabled

滚动开关

### # boolean bounces

设置滚动边界弹性 (仅在 iOS 下生效)

### # boolean showScrollbar

设置是否显示滚动条

### # boolean pagingEnabled

分页滑动开关

### # boolean fastDeceleration

设置滚动减速速率 (仅在 iOS 下生效)

### # boolean decelerationDisabled

取消滚动惯性 (仅在 iOS 下生效)

## # 方法

### # ScrollViewContext.triggerRefresh(Object object)

触发下拉刷新。

### # ScrollViewContext.closeRefresh()

关闭下拉刷新。

### # ScrollViewContext.triggerTwoLevel(Object object)

触发下拉二级。

### # ScrollViewContext.closeTwoLevel(Object object)

关闭下拉二级。

### # ScrollViewContext.scrollTo(Object object)

滚动至指定位置

### # ScrollViewContext.scrollIntoView(string selector, object ScrollIntoViewOptions)

滚动至指定位置

## # 示例代码

```js
wx.createSelectorQuery()
  .select('#scrollview')
  .node()
  .exec((res) => {
    const scrollView = res[0].node;
    scrollView.scrollEnabled = false;
  })
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)