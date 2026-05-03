> Source: https://developers.weixin.qq.com/miniprogram/dev/api/media/video-processing/MediaContainer.html

## MediaContainer

基础库 2.9.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

可通过 [wx.createMediaContainer](wx.createMediaContainer.html) 创建。

[MediaContainer](MediaContainer.html) 音视频处理容器，可以进行音频混音等操作

## # 方法

### # MediaContainer.extractDataSource(Object object)

将传入的视频源分离轨道。不会自动将轨道添加到待合成的容器里。

### # MediaContainer.addTrack(MediaTrack track)

将音频或视频轨道添加到容器

### # MediaContainer.removeTrack(MediaTrack track)

将音频或视频轨道从容器中移除

### # MediaContainer.export()

将容器内的轨道合并并导出视频文件

### # MediaContainer.destroy()

将容器销毁，释放资源
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)