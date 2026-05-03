> Source: https://developers.weixin.qq.com/miniprogram/dev/api/ai/visionkit/VKMarkerAnchor.html

## VKMarkerAnchor

基础库 2.24.5 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

marker anchor

## # 属性

### # number id

唯一标识

### # number type

类型

**type 的合法值**
 | 值 | 说明 | 最低版本 |
| --- | --- | --- |
| 1 | marker |  |
### # Float32Array transform

包含位置、旋转、放缩信息的矩阵，以列为主序

### # number markerId

marker id

### # string path

图片路径

## # 示例代码

[2D Marker能力使用参考](https://github.com/wechat-miniprogram/miniprogram-demo/tree/master/miniprogram/packageAPI/pages/ar/2dmarker-ar)
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)