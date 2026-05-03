> Source: https://developers.weixin.qq.com/miniprogram/dev/api/media/voip/wx.onVoIPChatInterrupted.html

## wx.onVoIPChatInterrupted(function listener)

基础库 2.7.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**小程序插件**：支持，需要小程序基础库版本不低于 [2.9.1](../../../framework/compatibility.html)

**微信 Windows 版**：支持

**微信 Mac 版**：支持

相关文档: [多人音视频对话](../../../framework/open-ability/voip-chat.html)

## # 功能描述

监听被动断开实时语音通话事件。包括小游戏切入后端时断开

## # 参数

### # function listener

被动断开实时语音通话事件的监听函数

#### # 参数

##### # Object res
 | 属性 | 类型 | 说明 |
| --- | --- | --- |
| errCode | Number | 错误码 |
| errMsg | String | 调用结果（错误原因） | The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)