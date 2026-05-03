> Source: https://developers.weixin.qq.com/miniprogram/dev/api/media/recorder/RecorderManager.html

## RecorderManager

全局唯一的录音管理器

## # 方法

### # RecorderManager.start(Object object)

开始录音

### # RecorderManager.pause()

暂停录音

### # RecorderManager.resume()

继续录音

### # RecorderManager.stop()

停止录音

### # RecorderManager.onStart(function listener)

监听录音开始事件

### # RecorderManager.onResume(function listener)

监听录音继续事件

### # RecorderManager.onPause(function listener)

监听录音暂停事件

### # RecorderManager.onStop(function listener)

监听录音结束事件

### # RecorderManager.onFrameRecorded(function listener)

监听已录制完指定帧大小的文件事件。如果设置了 frameSize，则会回调此事件。

### # RecorderManager.onError(function listener)

监听录音错误事件

### # RecorderManager.onInterruptionBegin(function listener)

监听录音因为受到系统占用而被中断开始事件。以下场景会触发此事件：微信语音聊天、微信视频聊天。此事件触发后，录音会被暂停。pause 事件在此事件后触发

### # RecorderManager.onInterruptionEnd(function listener)

监听录音中断结束事件。在收到 interruptionBegin 事件之后，小程序内所有录音会暂停，收到此事件之后才可再次录音成功。

## # 示例代码

```js
const recorderManager = wx.getRecorderManager()

recorderManager.onStart(() => {
  console.log('recorder start')
})
recorderManager.onPause(() => {
  console.log('recorder pause')
})
recorderManager.onStop((res) => {
  console.log('recorder stop', res)
  const { tempFilePath } = res
})
recorderManager.onFrameRecorded((res) => {
  const { frameBuffer } = res
  console.log('frameBuffer.byteLength', frameBuffer.byteLength)
})

const options = {
  duration: 10000,
  sampleRate: 44100,
  numberOfChannels: 1,
  encodeBitRate: 192000,
  format: 'aac',
  frameSize: 50
}

recorderManager.start(options)
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)