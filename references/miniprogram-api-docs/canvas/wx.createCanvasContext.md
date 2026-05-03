> Source: https://developers.weixin.qq.com/miniprogram/dev/api/canvas/wx.createCanvasContext.html

## CanvasContext wx.createCanvasContext(string canvasId, Object this)

从基础库 [2.9.0](../../framework/compatibility.html) 开始，本接口停止维护，请使用 [Canvas](Canvas.html) 代替

**小程序插件**：支持，需要小程序基础库版本不低于 [1.9.6](../../framework/compatibility.html)

**微信 Windows 版**：支持

**微信 Mac 版**：支持

**微信 鸿蒙 OS 版**：支持

相关文档: [旧版画布迁移指南](../../framework/ability/canvas-legacy-migration.html)、[canvas 组件介绍](../../component/canvas.html)

## # 功能描述

创建 canvas 的绘图上下文 [CanvasContext](CanvasContext.html) 对象

## # 参数

### # string canvasId

要获取上下文的 [canvas](../../component/canvas.html) 组件 canvas-id 属性

### # Object this

在自定义组件下，当前组件实例的this，表示在这个自定义组件下查找拥有 canvas-id 的 [canvas](../../component/canvas.html) ，如果省略则不在任何自定义组件内查找

## # 返回值

### # CanvasContext
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)