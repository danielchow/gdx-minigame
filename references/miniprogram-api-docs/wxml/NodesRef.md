> Source: https://developers.weixin.qq.com/miniprogram/dev/api/wxml/NodesRef.html

## NodesRef

相关文档: [获取界面上的节点信息](../../framework/view/selector.html)

用于获取 WXML 节点信息的对象

## # 方法

### # SelectorQuery NodesRef.fields(Object fields, NodesRef.FieldsCallback callback)

获取节点的相关信息。需要获取的字段在fields中指定。返回值是 `nodesRef` 对应的 `selectorQuery`

### # SelectorQuery NodesRef.boundingClientRect(NodesRef.boundingClientRectCallback callback)

添加节点的布局位置的查询请求。相对于显示区域，以像素为单位。其功能类似于 DOM 的 `getBoundingClientRect`。返回 `NodesRef` 对应的 `SelectorQuery`。

### # SelectorQuery NodesRef.scrollOffset(NodesRef.scrollOffsetCallback callback)

添加节点的滚动位置查询请求。以像素为单位。节点必须是 `scroll-view` 或者 `viewport`，返回 `NodesRef` 对应的 `SelectorQuery`。

### # SelectorQuery NodesRef.context(NodesRef.contextCallback callback)

添加节点的 Context 对象查询请求。目前支持 [VideoContext](../media/video/VideoContext.html)、[CanvasContext](../canvas/CanvasContext.html)、[LivePlayerContext](../media/live/LivePlayerContext.html)、[EditorContext](../media/editor/EditorContext.html)和 [MapContext](../media/map/MapContext.html) 的获取。

### # SelectorQuery NodesRef.node(NodesRef.nodeCallback callback)

获取 Node 节点实例。目前支持 [Canvas](../canvas/Canvas.html) 和 [ScrollViewContext](../ui/scroll/ScrollViewContext.html) 的获取。

### # SelectorQuery NodesRef.ref(NodesRef.refCallback callback)

获取 `Node` 节点的 Ref 对象，可用于 `worklet` 函数内操作节点。仅 `Skyline` 下支持，`Node` 必须是非 `virtual` 类型。
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)