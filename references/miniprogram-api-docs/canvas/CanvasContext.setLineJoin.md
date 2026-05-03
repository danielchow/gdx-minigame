> Source: https://developers.weixin.qq.com/miniprogram/dev/api/canvas/CanvasContext.setLineJoin.html

## CanvasContext.setLineJoin(string lineJoin)

CanvasContext 是旧版的接口，新版 [Canvas 2D](../../component/canvas.html) 接口与 Web 一致

从基础库 [1.9.90](../../framework/compatibility.html) 开始，本接口停止维护，请使用 [CanvasContext.lineJoin](CanvasContext.html) 代替

**小程序插件**：支持

相关文档: [旧版画布迁移指南](../../framework/ability/canvas-legacy-migration.html)、[canvas 组件介绍](../../component/canvas.html)

## # 功能描述

设置线条的交点样式

## # 参数

### # string lineJoin

线条的结束交点样式

**lineJoin 的合法值**
 | 值 | 说明 | 最低版本 |
| --- | --- | --- |
| bevel | 斜角 |  |
| round | 圆角 |  |
| miter | 尖角 |  |
## # 示例代码

```javascript
const ctx = wx.createCanvasContext('myCanvas')
ctx.beginPath()
ctx.moveTo(10, 10)
ctx.lineTo(100, 50)
ctx.lineTo(10, 90)
ctx.stroke()

ctx.beginPath()
ctx.setLineJoin('bevel')
ctx.setLineWidth(10)
ctx.moveTo(50, 10)
ctx.lineTo(140, 50)
ctx.lineTo(50, 90)
ctx.stroke()

ctx.beginPath()
ctx.setLineJoin('round')
ctx.setLineWidth(10)
ctx.moveTo(90, 10)
ctx.lineTo(180, 50)
ctx.lineTo(90, 90)
ctx.stroke()

ctx.beginPath()
ctx.setLineJoin('miter')
ctx.setLineWidth(10)
ctx.moveTo(130, 10)
ctx.lineTo(220, 50)
ctx.lineTo(130, 90)
ctx.stroke()

ctx.draw()
```

 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)