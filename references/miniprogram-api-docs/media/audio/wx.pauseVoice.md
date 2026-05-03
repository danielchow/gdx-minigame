> Source: https://developers.weixin.qq.com/miniprogram/dev/api/media/audio/wx.pauseVoice.html

## wx.pauseVoice(Object object)

从基础库 [1.6.0](../../../framework/compatibility.html) 开始，本接口停止维护，请使用 [wx.createInnerAudioContext](wx.createInnerAudioContext.html) 代替

**以 [Promise 风格](../../../framework/app-service/api.html#异步-API-返回-Promise) 调用**：支持

**小程序插件**：支持，需要小程序基础库版本不低于 [1.9.6](../../../framework/compatibility.html)

## # 功能描述

暂停正在播放的语音。再次调用 [wx.playVoice](wx.playVoice.html) 播放同一个文件时，会从暂停处开始播放。如果想从头开始播放，需要先调用 [wx.stopVoice](wx.stopVoice.html)。

## # 参数

### # Object object
 | 属性 | 类型 | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| success | function |  | 否 | 接口调用成功的回调函数 |
| fail | function |  | 否 | 接口调用失败的回调函数 |
| complete | function |  | 否 | 接口调用结束的回调函数（调用成功、失败都会执行） |
## # 示例代码

```js
wx.startRecord({
  success (res) {
    const tempFilePath = res.tempFilePath
    wx.playVoice({
      filePath: tempFilePath
    })

    setTimeout(() => { wx.pauseVoice() }, 5000)
  }
})
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)