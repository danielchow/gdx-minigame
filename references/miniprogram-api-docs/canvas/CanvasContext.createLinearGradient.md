> Source: https://developers.weixin.qq.com/miniprogram/dev/api/canvas/CanvasContext.createLinearGradient.html

## CanvasGradient CanvasContext.createLinearGradient(number x0, number y0, number x1, number y1)

CanvasContext 是旧版的接口，新版 [Canvas 2D](../../component/canvas.html) 接口与 Web 一致

从基础库 [2.9.0](../../framework/compatibility.html) 开始，本接口停止维护，请使用 [RenderingContext](RenderingContext.html) 代替

**小程序插件**：支持

相关文档: [旧版画布迁移指南](../../framework/ability/canvas-legacy-migration.html)、[canvas 组件介绍](../../component/canvas.html)

## # 功能描述

创建一个线性的渐变颜色。返回的`CanvasGradient`对象需要使用 [CanvasGradient.addColorStop()](CanvasGradient.addColorStop.html) 来指定渐变点，至少要两个。

## # 参数

### # number x0

起点的 x 坐标

### # number y0

起点的 y 坐标

### # number x1

终点的 x 坐标

### # number y1

终点的 y 坐标

## # 返回值

### # CanvasGradient

## # 示例代码

```javascript
const ctx = wx.createCanvasContext('myCanvas')

// Create linear gradient
const grd = ctx.createLinearGradient(0, 0, 200, 0)
grd.addColorStop(0, 'red')
grd.addColorStop(1, 'white')

// Fill with gradient
ctx.setFillStyle(grd)
ctx.fillRect(10, 10, 150, 80)
ctx.draw()
```

 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)