> Source: https://developers.weixin.qq.com/miniprogram/dev/api/wxml/SelectorQuery.in.html

## SelectorQuery SelectorQuery.in(Component component)

基础库 1.6.0 开始支持，低版本需做[兼容处理](../../framework/compatibility.html)。

**小程序插件**：支持

相关文档: [获取界面上的节点信息](../../framework/view/selector.html)

## # 功能描述

将选择器的选取范围更改为自定义组件 `component` 内。（初始时，选择器仅选取页面范围的节点，不会选取任何自定义组件中的节点）。

## # 参数

### # Component component

自定义组件实例

## # 返回值

### # SelectorQuery

## # 示例代码

```js
Component({
  queryMultipleNodes (){
    const query = wx.createSelectorQuery().in(this)
    query.select('#the-id').boundingClientRect(function(res){
      res.top // 这个组件内 #the-id 节点的上边界坐标
    }).exec()
  }
})
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)