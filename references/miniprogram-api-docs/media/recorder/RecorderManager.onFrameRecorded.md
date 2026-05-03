> Source: https://developers.weixin.qq.com/miniprogram/dev/api/media/recorder/RecorderManager.onFrameRecorded.html

## RecorderManager.onFrameRecorded(function listener)

**小程序插件**：支持

**微信 Windows 版**：支持

**微信 Mac 版**：支持

**微信 鸿蒙 OS 版**：支持

## # 功能描述

监听已录制完指定帧大小的文件事件。如果设置了 frameSize，则会回调此事件。

## # 参数

### # function listener

已录制完指定帧大小的文件事件的监听函数

#### # 参数

##### # Object res
 | 属性 | 类型 | 说明 |
| --- | --- | --- |
| frameBuffer | ArrayBuffer | 录音分片数据 |
| isLastFrame | boolean | 当前帧是否正常录音结束前的最后一帧 | The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)