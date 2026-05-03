> Source: https://developers.weixin.qq.com/miniprogram/dev/api/base/app/app-event/wx.onAudioInterruptionEnd.html

## wx.onAudioInterruptionEnd(function listener)

基础库 2.6.2 开始支持，低版本需做[兼容处理](../../../../framework/compatibility.html)。

**小程序插件**：支持，需要小程序基础库版本不低于 [2.15.0](../../../../framework/compatibility.html)

**微信 Windows 版**：支持

**微信 Mac 版**：支持

## # 功能描述

监听音频中断结束事件。在收到 onAudioInterruptionBegin 事件之后，小程序内所有音频会暂停，收到此事件之后才可再次播放成功

## # 参数

### # function listener

音频中断结束事件的监听函数
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)