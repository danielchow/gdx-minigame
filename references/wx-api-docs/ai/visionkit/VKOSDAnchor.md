> Source: https://developers.weixin.qq.com/minigame/dev/api/ai/visionkit/VKOSDAnchor.html
# VKOSDAnchor
基础库 2.32.1 开始支持，低版本需做[兼容处理](../../../guide/runtime/client-lib/compatibility.html)。

OSD anchor
## 属性
### number id
唯一标识
### number type
类型

**type 的合法值**
 | 值 | 说明 | 最低版本 |
| --- | --- | --- |
| 2 | OSD
### number markerId
marker id
### Object size
相对视窗的尺寸，取值范围为 [0, 1]，0 为左/上边缘，1 为右/下边缘
 | 属性 | 类型 | 说明 |
| --- | --- | --- |
| width | number | 宽度 |
| height | number | 高度
### string path
图片路径
### Object origin
相对视窗的位置信息，取值范围为 [0, 1]，0 为左/上边缘，1 为右/下边缘
 | 属性 | 类型 | 说明 |
| --- | --- | --- |
| x | number | 横坐标 |
| y | number | 纵坐标
## 示例代码
[单样本检测(OSD)能力使用参考](https://github.com/wechat-miniprogram/miniprogram-demo/tree/master/miniprogram/packageAPI/pages/ar/osd-ar)
