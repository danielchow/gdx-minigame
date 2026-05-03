> Source: https://developers.weixin.qq.com/miniprogram/dev/api/canvas/CanvasContext.setGlobalAlpha.html

## CanvasContext.setGlobalAlpha(number alpha)

CanvasContext 是旧版的接口，新版 [Canvas 2D](../../component/canvas.html) 接口与 Web 一致

从基础库 [1.9.90](../../framework/compatibility.html) 开始，本接口停止维护，请使用 [CanvasContext.globalAlpha](CanvasContext.html) 代替

**小程序插件**：支持

相关文档: [旧版画布迁移指南](../../framework/ability/canvas-legacy-migration.html)、[canvas 组件介绍](../../component/canvas.html)

## # 功能描述

设置全局画笔透明度。

## # 参数

### # number alpha

透明度。范围 0-1，0 表示完全透明，1 表示完全不透明。

## # 示例代码

```javascript
const ctx = wx.createCanvasContext('myCanvas')

ctx.setFillStyle('red')
ctx.fillRect(10, 10, 150, 100)
ctx.setGlobalAlpha(0.2)
ctx.setFillStyle('blue')
ctx.fillRect(50, 50, 150, 100)
ctx.setFillStyle('yellow')
ctx.fillRect(100, 100, 150, 100)

ctx.draw()
```

 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)