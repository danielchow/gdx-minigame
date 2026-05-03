> Source: https://developers.weixin.qq.com/miniprogram/dev/api/ai/visionkit/VKFrame.getCameraTexture.html

## Object VKFrame.getCameraTexture(WebGLRenderingContext gl)

基础库 2.20.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**小程序插件**：支持，需要小程序基础库版本不低于 [2.20.0](../../../framework/compatibility.html)

## # 功能描述

获取当前帧纹理，目前只支持 YUV 纹理。

## # 参数

### # WebGLRenderingContext gl

画布

## # 返回值

### # Object

帧纹理对象
 | 属性 | 类型 | 说明 |
| --- | --- | --- |
| yTexture | WebGLTexture | Y 分量纹理 |
| uvTexture | WebGLTexture | UV 分量纹理 | The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)