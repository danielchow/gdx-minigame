> Source: https://developers.weixin.qq.com/miniprogram/dev/api/ai/visionkit/VKFrame.getDisplayTransform.html

## Float32Array VKFrame.getDisplayTransform()

基础库 2.20.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**小程序插件**：支持，需要小程序基础库版本不低于 [2.20.0](../../../framework/compatibility.html)

## # 功能描述

获取纹理调整矩阵。默认获取到的纹理是未经裁剪调整的纹理，此矩阵可用于在着色器中根据帧对象尺寸对纹理进行裁剪。

## # 返回值

### # Float32Array

纹理调整矩阵
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)