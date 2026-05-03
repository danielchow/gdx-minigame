> Source: https://developers.weixin.qq.com/miniprogram/dev/api/media/audio/AudioBuffer.copyFromChannel.html

## AudioBuffer.copyFromChannel()

**小程序插件**：不支持

**微信 Windows 版**：支持

**微信 Mac 版**：支持

## # 功能描述

从AudioBuffer的指定频道复制到数组终端。

## # 示例代码

示例代码

```javascript
const audioCtx = wx.createWebAudioContext()
wx.request({
  url: 'xx.mp3', // 音频 url
  responseType: 'arraybuffer',
  success: res => {
    audioCtx.decodeAudioData(res.data, audioBuffer => {
      const channels = audioBuffer.numberOfChannels;
      const frameCount = audioCtx.sampleRate * 2.0;
      const anotherArray = new Float32Array(frameCount);
      const rate = audioBuffer.sampleRate
      const startOffset = 0
      const endOffset = rate * 3;
      const newAudioBuffer = audioCtx.createBuffer(channels,endOffset - startOffset,rate)
      const offset = 0

      for (let channel = 0; channel < channels; channel++) {
        audioBuffer.copyFromChannel(anotherArray, channel, startOffset);
        console.log('copyFromChannel 成功')
        newAudioBuffer.copyToChannel(anotherArray, channel, offset);
      }
    }, err => {
      console.error('decodeAudioData fail', err)
    })
  }
})
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)