> Source: https://developers.weixin.qq.com/miniprogram/dev/api/canvas/CanvasContext.fillText.html

## CanvasContext.fillText(string text, number x, number y, number maxWidth)

CanvasContext 是旧版的接口，新版 [Canvas 2D](../../component/canvas.html) 接口与 Web 一致

从基础库 [2.9.0](../../framework/compatibility.html) 开始，本接口停止维护，请使用 [RenderingContext](RenderingContext.html) 代替

**小程序插件**：支持

相关文档: [旧版画布迁移指南](../../framework/ability/canvas-legacy-migration.html)、[canvas 组件介绍](../../component/canvas.html)

## # 功能描述

在画布上绘制被填充的文本

## # 参数

### # string text

在画布上输出的文本

### # number x

绘制文本的左上角 x 坐标位置

### # number y

绘制文本的左上角 y 坐标位置

### # number maxWidth

需要绘制的最大宽度，可选

## # 示例代码

```javascript
const ctx = wx.createCanvasContext('myCanvas')

ctx.setFontSize(20)
ctx.fillText('Hello', 20, 20)
ctx.fillText('MINA', 100, 100)

ctx.draw()
```

 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)