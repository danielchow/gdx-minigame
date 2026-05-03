> Source: https://developers.weixin.qq.com/miniprogram/dev/api/canvas/CanvasContext.rect.html

## CanvasContext.rect(number x, number y, number width, number height)

CanvasContext 是旧版的接口，新版 [Canvas 2D](../../component/canvas.html) 接口与 Web 一致

从基础库 [2.9.0](../../framework/compatibility.html) 开始，本接口停止维护，请使用 [RenderingContext](RenderingContext.html) 代替

**小程序插件**：支持

相关文档: [旧版画布迁移指南](../../framework/ability/canvas-legacy-migration.html)、[canvas 组件介绍](../../component/canvas.html)

## # 功能描述

创建一个矩形路径。需要用 [`fill`](CanvasContext.fill.html) 或者 [`stroke`](CanvasContext.stroke.html) 方法将矩形真正的画到 `canvas` 中

## # 参数

### # number x

矩形路径左上角的横坐标

### # number y

矩形路径左上角的纵坐标

### # number width

矩形路径的宽度

### # number height

矩形路径的高度

## # 示例代码

```javascript
const ctx = wx.createCanvasContext('myCanvas')
ctx.rect(10, 10, 150, 75)
ctx.setFillStyle('red')
ctx.fill()
ctx.draw()
```

 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)