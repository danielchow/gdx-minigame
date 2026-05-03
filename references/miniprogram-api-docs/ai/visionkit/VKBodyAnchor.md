> Source: https://developers.weixin.qq.com/miniprogram/dev/api/ai/visionkit/VKBodyAnchor.html

## VKBodyAnchor

基础库 2.28.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

人体 anchor

## # 属性

### # number id

唯一标识

### # number type

类型

**type 的合法值**
 | 值 | 说明 | 最低版本 |
| --- | --- | --- |
| 5 | 人体 |  |
### # number detectId

识别序号

### # Object size

相对视窗的尺寸，取值范围为 [0, 1]，0 为左/上边缘，1 为右/下边缘
 | 属性 | 类型 | 说明 |
| --- | --- | --- |
| width | number | 宽度 |
| height | number | 高度 |
### # Object origin

相对视窗的位置信息，取值范围为 [0, 1]，0 为左/上边缘，1 为右/下边缘
 | 属性 | 类型 | 说明 |
| --- | --- | --- |
| x | number | 横坐标 |
| y | number | 纵坐标 |
### # Array.<number> confidence

关键点的置信度

### # Array.<Object> points

关键点
 | 属性 | 类型 | 说明 |
| --- | --- | --- |
| x | number | 横坐标 |
| y | number | 纵坐标 |
### # number score

总体置信值

## # 示例代码

[静态图像body检测能力使用参考](https://github.com/wechat-miniprogram/miniprogram-demo/tree/master/miniprogram/packageAPI/pages/ar/photo-body-detect)

[实时摄像头body检测能力使用参考](https://github.com/wechat-miniprogram/miniprogram-demo/tree/master/miniprogram/packageAPI/pages/ar/body-detect)
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)