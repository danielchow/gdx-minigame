> Source: https://developers.weixin.qq.com/miniprogram/dev/api/media/audio/WebAudioContext.createDynamicsCompressor.html

## DynamicsCompressorNode WebAudioContext.createDynamicsCompressor()

**小程序插件**：不支持

## # 功能描述

创建一个DynamicsCompressorNode

## # 返回值

### # DynamicsCompressorNode

## # 示例代码

示例代码

```javascript
let audioCtx = wx.createWebAudioContext()
let compressor = audioCtx.createDynamicsCompressor()

compressor.threshold.value = -50
compressor.knee.value = 40
compressor.ratio.value = 12
compressor.attack.value = 0
compressor.release.value = 0.25
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)