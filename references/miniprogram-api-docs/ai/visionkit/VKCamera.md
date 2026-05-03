> Source: https://developers.weixin.qq.com/miniprogram/dev/api/ai/visionkit/VKCamera.html

## VKCamera

基础库 2.20.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

相机对象

## # 属性

### # Float32Array transform

相机原始的Pose矩阵

### # Float32Array viewMatrix

视图矩阵

### # Float32Array intrinsics

基础库 2.22.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

相机内参，只有 v2 版本支持

## # 方法

### # Float32Array VKCamera.getProjectionMatrix(number near, number far)

获取投影矩阵
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)