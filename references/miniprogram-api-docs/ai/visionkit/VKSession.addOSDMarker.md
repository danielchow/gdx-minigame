> Source: https://developers.weixin.qq.com/miniprogram/dev/api/ai/visionkit/VKSession.addOSDMarker.html

## number VKSession.addOSDMarker(string path)

基础库 2.24.5 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**小程序插件**：支持，需要小程序基础库版本不低于 [2.24.5](../../../framework/compatibility.html)

## # 功能描述

添加一个 OSD marker（one-shot detection marker），要求调 [wx.createVKSession](wx.createVKSession.html) 时传入的 track.OSD 为 true

## # 参数

### # string path

图片路径，目前只支持本地用户图片

## # 返回值

### # number

marker id

## # 使用提示

注意事项：

- 使用 addOSDMarker 接口之前，需要在 createVKSession 的时候声明开启 OSD 跟踪。即 wx.createVKSession({ track: { OSD: true } })
 - 可以添加多个 OSDMarker 图片，但不能重复添加相同的 OSDMarker 图片。


对传入的图片有如下要求：

- 格式：jpg 格式彩色图片
 - 分辨率：尺寸不低于 240x240
 - 宽高比：在 1:1 ~ 16:9 之间，要求尽量方正，避免狭长的图片
 - 质量：目标物体需要占画面主体，避免大面积留白，避免大面积文字，不能含其他物体。


示例：




建议：

- 具有丰富的细节，避免纯色且形状特点不鲜明的物体，例如：


- 避免模糊，最好采用高清图片

 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)