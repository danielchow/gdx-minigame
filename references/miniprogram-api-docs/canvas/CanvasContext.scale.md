> Source: https://developers.weixin.qq.com/miniprogram/dev/api/canvas/CanvasContext.scale.html

## CanvasContext.scale(number scaleWidth, number scaleHeight)

CanvasContext 是旧版的接口，新版 [Canvas 2D](../../component/canvas.html) 接口与 Web 一致

从基础库 [2.9.0](../../framework/compatibility.html) 开始，本接口停止维护，请使用 [RenderingContext](RenderingContext.html) 代替

**小程序插件**：支持

相关文档: [旧版画布迁移指南](../../framework/ability/canvas-legacy-migration.html)、[canvas 组件介绍](../../component/canvas.html)

## # 功能描述

在调用后，之后创建的路径其横纵坐标会被缩放。多次调用倍数会相乘。

## # 参数

### # number scaleWidth

横坐标缩放的倍数 (1 = 100%，0.5 = 50%，2 = 200%)

### # number scaleHeight

纵坐标轴缩放的倍数 (1 = 100%，0.5 = 50%，2 = 200%)

## # 示例代码

```javascript
const ctx = wx.createCanvasContext('myCanvas')

ctx.strokeRect(10, 10, 25, 15)
ctx.scale(2, 2)
ctx.strokeRect(10, 10, 25, 15)
ctx.scale(2, 2)
ctx.strokeRect(10, 10, 25, 15)

ctx.draw()
```

 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)