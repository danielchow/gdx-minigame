> Source: https://developers.weixin.qq.com/miniprogram/dev/api/ai/visionkit/VKSession.getVKFrame.html

## VKFrame VKSession.getVKFrame(number width, number height)

基础库 2.20.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**小程序插件**：支持，需要小程序基础库版本不低于 [2.20.0](../../../framework/compatibility.html)

## # 功能描述

获取帧对象，每调用一次都会触发一次帧分析过程。目前 VKSession 相机的最大帧数是 30 fps，因此调用 getVKFrame 的频率也可以限制在 30 fps，以减少渲染开销。

## # 参数

### # number width

宽度

### # number height

高度

## # 返回值

### # VKFrame

帧对象
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)