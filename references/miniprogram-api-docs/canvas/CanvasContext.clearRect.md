> Source: https://developers.weixin.qq.com/miniprogram/dev/api/canvas/CanvasContext.clearRect.html

## CanvasContext.clearRect(number x, number y, number width, number height)

CanvasContext 是旧版的接口，新版 [Canvas 2D](../../component/canvas.html) 接口与 Web 一致

从基础库 [2.9.0](../../framework/compatibility.html) 开始，本接口停止维护，请使用 [RenderingContext](RenderingContext.html) 代替

**小程序插件**：支持

相关文档: [旧版画布迁移指南](../../framework/ability/canvas-legacy-migration.html)、[canvas 组件介绍](../../component/canvas.html)

## # 功能描述

清除画布上在该矩形区域内的内容

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

clearRect 并非画一个白色的矩形在地址区域，而是清空，为了有直观感受，对 canvas 加了一层背景色。

```html
<canvas canvas-id="myCanvas" style="border: 1px solid; background: #123456;"/>
```

```javascript
const ctx = wx.createCanvasContext('myCanvas')
ctx.setFillStyle('red')
ctx.fillRect(0, 0, 150, 200)
ctx.setFillStyle('blue')
ctx.fillRect(150, 0, 150, 200)
ctx.clearRect(10, 10, 150, 75)
ctx.draw()
```

 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)