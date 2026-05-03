> Source: https://developers.weixin.qq.com/miniprogram/dev/api/media/camera/CameraContext.onCameraFrame.html

## CameraFrameListener CameraContext.onCameraFrame(function callback)

基础库 2.7.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**小程序插件**：不支持

相关文档: [camera 组件介绍](../../../component/camera.html)

## # 功能描述

获取 Camera 实时帧数据

## # 参数

### # function callback

回调函数

#### # 参数

##### # Object res
 | 属性 | 类型 | 说明 |
| --- | --- | --- |
| width | number | 图像数据矩形的宽度 |
| height | number | 图像数据矩形的高度 |
| data | ArrayBuffer | 图像像素点数据，一维数组，每四项表示一个像素点的 rgba |
## # 返回值

### # CameraFrameListener

注： 使用该接口需同时在 [camera](../../../component/camera.html) 组件属性中指定 frame-size。

## # 示例代码

```js
const context = wx.createCameraContext()
const listener = context.onCameraFrame((frame) => {
  console.log(frame.data instanceof ArrayBuffer, frame.width, frame.height)
})
listener.start()
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)