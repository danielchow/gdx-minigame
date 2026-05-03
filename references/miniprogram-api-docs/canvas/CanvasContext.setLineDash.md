> Source: https://developers.weixin.qq.com/miniprogram/dev/api/canvas/CanvasContext.setLineDash.html

## CanvasContext.setLineDash(Array.<number> pattern, number offset)

CanvasContext 是旧版的接口，新版 [Canvas 2D](../../component/canvas.html) 接口与 Web 一致

从基础库 [1.9.90](../../framework/compatibility.html) 开始，本接口停止维护，请使用 [CanvasContext.lineDashOffset](CanvasContext.html) 代替

基础库 1.6.0 开始支持，低版本需做[兼容处理](../../framework/compatibility.html)。

**小程序插件**：支持

相关文档: [旧版画布迁移指南](../../framework/ability/canvas-legacy-migration.html)、[canvas 组件介绍](../../component/canvas.html)

## # 功能描述

设置虚线样式。

## # 参数

### # Array.<number> pattern

一组描述交替绘制线段和间距（坐标空间单位）长度的数字

### # number offset

虚线偏移量

## # 示例代码

```javascript
const ctx = wx.createCanvasContext('myCanvas')

ctx.setLineDash([10, 20], 5);

ctx.beginPath();
ctx.moveTo(0,100);
ctx.lineTo(400, 100);
ctx.stroke();

ctx.draw()
```

 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)