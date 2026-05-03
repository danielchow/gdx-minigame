> Source: https://developers.weixin.qq.com/miniprogram/dev/api/canvas/CanvasContext.moveTo.html

## CanvasContext.moveTo(number x, number y)

CanvasContext 是旧版的接口，新版 [Canvas 2D](../../component/canvas.html) 接口与 Web 一致

从基础库 [2.9.0](../../framework/compatibility.html) 开始，本接口停止维护，请使用 [RenderingContext](RenderingContext.html) 代替

**小程序插件**：支持

相关文档: [旧版画布迁移指南](../../framework/ability/canvas-legacy-migration.html)、[canvas 组件介绍](../../component/canvas.html)

## # 功能描述

把路径移动到画布中的指定点，不创建线条。用 `stroke` 方法来画线条

## # 参数

### # number x

目标位置的 x 坐标

### # number y

目标位置的 y 坐标

## # 示例代码

```javascript
const ctx = wx.createCanvasContext('myCanvas')
ctx.moveTo(10, 10)
ctx.lineTo(100, 10)

ctx.moveTo(10, 50)
ctx.lineTo(100, 50)
ctx.stroke()
ctx.draw()
```

 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)