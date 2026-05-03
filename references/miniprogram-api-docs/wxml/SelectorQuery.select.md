> Source: https://developers.weixin.qq.com/miniprogram/dev/api/wxml/SelectorQuery.select.html

## NodesRef SelectorQuery.select(string selector)

**小程序插件**：支持

相关文档: [获取界面上的节点信息](../../framework/view/selector.html)

## # 功能描述

在当前页面下选择第一个匹配选择器 `selector` 的节点。返回一个 `NodesRef` 对象实例，可以用于获取节点信息。

## # 参数

### # string selector

选择器

## # 返回值

### # NodesRef

## # selector 语法

selector类似于 CSS 的选择器，但仅支持下列语法。

- ID选择器：#the-id
 - class选择器（可以连续指定多个）：.a-class.another-class
 - 子元素选择器：.the-parent > .the-child
 - 后代选择器：.the-ancestor .the-descendant
 - 跨自定义组件的后代选择器：.the-ancestor >>> .the-descendant
 - 多选择器的并集：#a-node, .some-other-nodes

 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)