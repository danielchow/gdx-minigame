> Source: https://developers.weixin.qq.com/miniprogram/dev/api/media/camera/CameraContext.html

## CameraContext

相关文档: [camera 组件介绍](../../../component/camera.html)

CameraContext 实例，可通过 [wx.createCameraContext](wx.createCameraContext.html) 获取。

[CameraContext](CameraContext.html) 与页面内唯一的 [camera](../../../component/camera.html) 组件绑定，操作对应的 [camera](../../../component/camera.html) 组件。

## # 方法

### # CameraFrameListener CameraContext.onCameraFrame(onCameraFrameCallback callback)

获取 Camera 实时帧数据

### # CameraContext.takePhoto(Object object)

拍摄照片

### # CameraContext.setZoom(Object object)

设置缩放级别

### # CameraContext.startRecord(Object object)

开始录像

### # CameraContext.stopRecord(Object object)

结束录像

## # 示例代码

[在开发者工具中预览效果](https://developers.weixin.qq.com/s/VBZ3Jim26zYu)
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)