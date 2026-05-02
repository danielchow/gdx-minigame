> Source: https://developers.weixin.qq.com/minigame/dev/api/ai/visionkit/VKOCRAnchor.html
# VKOCRAnchor
基础库 2.32.1 开始支持，低版本需做[兼容处理](../../../guide/runtime/client-lib/compatibility.html)。

OCR anchor
## 属性
### number id
唯一标识
### number type
类型

**type 的合法值**
 | 值 | 说明 | 最低版本 |
| --- | --- | --- |
| 6 | OCR
### string text
识别的文字结果
## 示例代码
[静态图像OCR检测能力使用参考](https://github.com/wechat-miniprogram/miniprogram-demo/tree/master/miniprogram/packageAPI/pages/ar/photo-ocr-detect)

[实时摄像头OCR检测能力使用参考](https://github.com/wechat-miniprogram/miniprogram-demo/tree/master/miniprogram/packageAPI/pages/ar/ocr-detect)
