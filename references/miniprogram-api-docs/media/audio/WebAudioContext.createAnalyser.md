> Source: https://developers.weixin.qq.com/miniprogram/dev/api/media/audio/WebAudioContext.createAnalyser.html

## AnalyserNode WebAudioContext.createAnalyser()

基础库 2.22.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**小程序插件**：不支持

## # 功能描述

创建一个 AnalyserNode 。可以用来获取音频时间和频率数据，以及实现数据可视化。

## # 返回值

### # AnalyserNode

## # 示例代码

示例代码

```javascript
const audioCtx = wx.createWebAudioContext();
const analyser = audioCtx.createAnalyser();
analyser.fftSize = 2048;
const bufferLength = analyser.fftSize;
const dataArray = new Uint8Array(bufferLength);
analyser.getByteTimeDomainData(dataArray);
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)