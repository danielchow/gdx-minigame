> Source: https://developers.weixin.qq.com/miniprogram/dev/api/canvas/CanvasContext.setFillStyle.html

## CanvasContext.setFillStyle(string|CanvasGradient color)

CanvasContext 是旧版的接口，新版 [Canvas 2D](../../component/canvas.html) 接口与 Web 一致

从基础库 [1.9.90](../../framework/compatibility.html) 开始，本接口停止维护，请使用 [CanvasContext.fillStyle](CanvasContext.html) 代替

**小程序插件**：支持

相关文档: [旧版画布迁移指南](../../framework/ability/canvas-legacy-migration.html)、[canvas 组件介绍](../../component/canvas.html)

## # 功能描述

设置填充色。

## # 参数

### # string|CanvasGradient color

填充的颜色，默认颜色为 black。

## # 代码示例

```js
const ctx = wx.createCanvasContext('myCanvas')
ctx.setFillStyle('red')
ctx.fillRect(10, 10, 150, 75)
ctx.draw()
```

 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)