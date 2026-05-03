> Source: https://developers.weixin.qq.com/miniprogram/dev/api/media/camera/CameraContext.startRecord.html

## CameraContext.startRecord(Object object)

**以 [Promise 风格](../../../framework/app-service/api.html#%E5%BC%82%E6%AD%A5-API-%E8%BF%94%E5%9B%9E-Promise) 调用**：不支持

**小程序插件**：支持

**微信 鸿蒙 OS 版**：支持

相关文档: [camera 组件介绍](../../../component/camera.html)

## # 功能描述

开始录像

## # 参数

### # Object object
 | 属性 | 类型 | 默认值 | 必填 | 说明 | 最低版本 |
| --- | --- | --- | --- | --- | --- |
| timeoutCallback | function |  | 否 | 超过录制时长上限时会结束录像并触发此回调，录像异常退出时也会触发此回调 |  |
| timeout | number | 30 | 否 | 录制时长上限，单位为秒，最长不能超过 5 分钟 | 2.22.0 |
| selfieMirror | boolean | true | 否 | 是否开启镜像 | 2.22.0 |
| success | function |  | 否 | 接口调用成功的回调函数 |  |
| fail | function |  | 否 | 接口调用失败的回调函数 |  |
| complete | function |  | 否 | 接口调用结束的回调函数（调用成功、失败都会执行） |  |
#### # object.timeoutCallback 回调函数

##### # 参数
 [#](#Object-res) Object res | 属性 | 类型 | 说明 |
| --- | --- | --- |
| tempThumbPath | string | 封面图片文件的临时路径 (本地路径) |
| tempVideoPath | string | 视频的文件的临时路径 (本地路径) | The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)