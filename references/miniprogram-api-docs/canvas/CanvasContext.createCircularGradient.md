> Source: https://developers.weixin.qq.com/miniprogram/dev/api/canvas/CanvasContext.createCircularGradient.html

## CanvasGradient CanvasContext.createCircularGradient(number x, number y, number r)

CanvasContext 是旧版的接口，新版 [Canvas 2D](../../component/canvas.html) 接口与 Web 一致

从基础库 [2.9.0](../../framework/compatibility.html) 开始，本接口停止维护，请使用 [RenderingContext](RenderingContext.html) 代替

**小程序插件**：支持

相关文档: [旧版画布迁移指南](../../framework/ability/canvas-legacy-migration.html)、[canvas 组件介绍](../../component/canvas.html)

## # 功能描述

创建一个圆形的渐变颜色。起点在圆心，终点在圆环。返回的`CanvasGradient`对象需要使用 [CanvasGradient.addColorStop()](CanvasGradient.addColorStop.html) 来指定渐变点，至少要两个。

## # 参数

### # number x

圆心的 x 坐标

### # number y

圆心的 y 坐标

### # number r

圆的半径

## # 返回值

### # CanvasGradient

## # 示例代码

```javascript
const ctx = wx.createCanvasContext('myCanvas')

// Create circular gradient
const grd = ctx.createCircularGradient(75, 50, 50)
grd.addColorStop(0, 'red')
grd.addColorStop(1, 'white')

// Fill with gradient
ctx.setFillStyle(grd)
ctx.fillRect(10, 10, 150, 80)
ctx.draw()
```

 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)