> Source: https://developers.weixin.qq.com/miniprogram/dev/api/media/audio/WebAudioContext.close.html

## Promise WebAudioContext.close()
 **
**小程序插件**：不支持

## # 功能描述

关闭WebAudioContext

## # 返回值

### # Promise

## # 注意事项

同步关闭对应的WebAudio上下文。close后会立即释放当前上下文的资源，不要在close后再次访问state属性。**

```js
const audioCtx = wx.createWebAudioContext()
audioCtx.close().then(() => {
  console.log(audioCtx.state) // bad case：不应该在close后再访问state
})
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)