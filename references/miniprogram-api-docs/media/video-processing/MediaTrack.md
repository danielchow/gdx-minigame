> Source: https://developers.weixin.qq.com/miniprogram/dev/api/media/video-processing/MediaTrack.html

## MediaTrack

基础库 2.9.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

可通过 [MediaContainer.extractDataSource](MediaContainer.extractDataSource.html) 返回。

[MediaTrack](MediaTrack.html) 音频或视频轨道，可以对轨道进行一些操作

## # 属性

### # string kind

轨道类型，只读

**kind 的合法值**
 | 值 | 说明 | 最低版本 |
| --- | --- | --- |
| audio | 音频轨道 |  |
| video | 视频轨道 |  |
### # number duration

轨道长度，只读

### # number volume

音量，音频轨道下有效，可写
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)