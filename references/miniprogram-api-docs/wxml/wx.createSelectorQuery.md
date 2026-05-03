> Source: https://developers.weixin.qq.com/miniprogram/dev/api/wxml/wx.createSelectorQuery.html

## SelectorQuery wx.createSelectorQuery()

基础库 1.4.0 开始支持，低版本需做[兼容处理](../../framework/compatibility.html)。

**小程序插件**：支持，需要小程序基础库版本不低于 [1.9.6](../../framework/compatibility.html)

**微信 Windows 版**：支持

**微信 Mac 版**：支持

**微信 鸿蒙 OS 版**：支持

相关文档: [获取界面上的节点信息](../../framework/view/selector.html)

## # 功能描述

返回一个 SelectorQuery 对象实例。在自定义组件或包含自定义组件的页面中，应使用 `this.createSelectorQuery()` 来代替。

## # 返回值

### # SelectorQuery

## # 示例代码

```js
const query = wx.createSelectorQuery()
query.select('#the-id').boundingClientRect()
query.selectViewport().scrollOffset()
query.exec(function(res){
  res[0].top       // #the-id节点的上边界坐标
  res[1].scrollTop // 显示区域的竖直滚动位置
})
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)