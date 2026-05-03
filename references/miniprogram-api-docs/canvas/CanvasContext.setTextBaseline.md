> Source: https://developers.weixin.qq.com/miniprogram/dev/api/canvas/CanvasContext.setTextBaseline.html

## CanvasContext.setTextBaseline(string textBaseline)

CanvasContext 是旧版的接口，新版 [Canvas 2D](../../component/canvas.html) 接口与 Web 一致

从基础库 [2.9.0](../../framework/compatibility.html) 开始，本接口停止维护，请使用 [RenderingContext](RenderingContext.html) 代替

基础库 1.4.0 开始支持，低版本需做[兼容处理](../../framework/compatibility.html)。

**小程序插件**：支持

相关文档: [旧版画布迁移指南](../../framework/ability/canvas-legacy-migration.html)、[canvas 组件介绍](../../component/canvas.html)

## # 功能描述

设置文字的竖直对齐

## # 参数

### # string textBaseline

文字的竖直对齐方式

**textBaseline 的合法值**
 | 值 | 说明 | 最低版本 |
| --- | --- | --- |
| top | 顶部对齐 |  |
| bottom | 底部对齐 |  |
| middle | 居中对齐 |  |
| normal |  |  |
## # 示例代码

```javascript
const ctx = wx.createCanvasContext('myCanvas')

ctx.setStrokeStyle('red')
ctx.moveTo(5, 75)
ctx.lineTo(295, 75)
ctx.stroke()

ctx.setFontSize(20)

ctx.setTextBaseline('top')
ctx.fillText('top', 5, 75)

ctx.setTextBaseline('middle')
ctx.fillText('middle', 50, 75)

ctx.setTextBaseline('bottom')
ctx.fillText('bottom', 120, 75)

ctx.setTextBaseline('normal')
ctx.fillText('normal', 200, 75)

ctx.draw()
```

 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)