> Source: https://developers.weixin.qq.com/miniprogram/dev/api/canvas/Canvas.html

## Canvas

基础库 2.7.0 开始支持，低版本需做[兼容处理](../../framework/compatibility.html)。

相关文档: [画布指南](../../framework/ability/canvas.html)、[canvas 组件介绍](../../component/canvas.html)

Canvas 实例，可通过 [SelectorQuery](../wxml/SelectorQuery.html) 获取。

## # 属性

### # number width

画布宽度

### # number height

画布高度

## # 方法

### # RenderingContext Canvas.getContext(string contextType)

该方法返回 Canvas 的绘图上下文

### # Image Canvas.createImage()

创建一个图片对象。 支持在 2D Canvas 和 WebGL Canvas 下使用, 但不支持混用 2D 和 WebGL 的方法。

### # ImageData Canvas.createImageData()

创建一个 ImageData 对象。仅支持在 2D Canvas 中使用。

### # Path2D Canvas.createPath2D(Path2D path)

创建 Path2D 对象

### # number Canvas.requestAnimationFrame(function callback)

在下次进行重绘时执行。 支持在 2D Canvas 和 WebGL Canvas 下使用, 但不支持混用 2D 和 WebGL 的方法。

### # Canvas.cancelAnimationFrame(number requestID)

取消由 requestAnimationFrame 添加到计划中的动画帧请求。支持在 2D Canvas 和 WebGL Canvas 下使用, 但不支持混用 2D 和 WebGL 的方法。

### # string Canvas.toDataURL(string type, number encoderOptions)

返回一个包含图片展示的 data URI 。可以使用 type 参数其类型，默认为 PNG 格式。

## # 示例代码

2D Canvas 示例
[在开发者工具中预览效果](https://developers.weixin.qq.com/s/SHfgCmmq7UcM)

WebGL 示例
[在开发者工具中预览效果](https://developers.weixin.qq.com/s/qEGUOqmf7T8z)
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)