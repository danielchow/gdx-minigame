> Source: https://developers.weixin.qq.com/miniprogram/dev/api/canvas/CanvasContext.measureText.html

## Object CanvasContext.measureText(string text)

CanvasContext 是旧版的接口，新版 [Canvas 2D](../../component/canvas.html) 接口与 Web 一致

从基础库 [2.9.0](../../framework/compatibility.html) 开始，本接口停止维护，请使用 [RenderingContext](RenderingContext.html) 代替

基础库 1.9.90 开始支持，低版本需做[兼容处理](../../framework/compatibility.html)。

**小程序插件**：支持

相关文档: [旧版画布迁移指南](../../framework/ability/canvas-legacy-migration.html)、[canvas 组件介绍](../../component/canvas.html)

## # 功能描述

测量文本尺寸信息。目前仅返回文本宽度。同步接口。

## # 参数

### # string text

要测量的文本

## # 返回值

### # Object
 | 属性 | 类型 | 说明 |
| --- | --- | --- |
| width | number | 文本的宽度 |
## # 示例代码

```javascript
const ctx = wx.createCanvasContext('myCanvas')
  ctx.font = 'italic bold 20px cursive'
  const metrics = ctx.measureText('Hello World')
  console.log(metrics.width)
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)