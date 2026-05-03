> Source: https://developers.weixin.qq.com/miniprogram/dev/api/ui/window/wx.onWindowResize.html

## wx.onWindowResize(function listener)

基础库 2.3.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**小程序插件**：不支持

**微信 Windows 版**：支持

**微信 Mac 版**：支持

**微信 鸿蒙 OS 版**：支持

## # 功能描述

监听窗口尺寸变化事件

## # 参数

### # function listener

窗口尺寸变化事件的监听函数

#### # 参数

##### # Object res
 |  | 属性 | 类型 | 说明 |
| --- | --- | --- | --- |
|  | size | Object |  |
|  |  | 结构属性 | 类型 | 说明 |
|  | windowWidth | number | 变化后的窗口宽度，单位 px |
|  | windowHeight | number | 变化后的窗口高度，单位 px | The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)