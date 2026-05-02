> Source: https://developers.weixin.qq.com/minigame/dev/api/media/voip/wx.updateVoIPChatMuteConfig.html
# wx.updateVoIPChatMuteConfig(Object object)
基础库 2.7.0 开始支持，低版本需做[兼容处理](../../../guide/runtime/client-lib/compatibility.html)。

**以 [Promise 风格](../../../game-engine/worker/api.html#异步-API-返回-Promise) 调用**：支持

**微信 Windows 版**：支持

**微信 Mac 版**：支持

**微信 鸿蒙 OS 版**：支持

相关文档: [多人音视频对话](../../../guide/open-ability/voip-chat.html)
## 功能描述
更新实时语音静音设置
## 参数
### Object object |  | 属性 | 类型 | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- | --- | --- |
|  | muteConfig | Object |  | 是 | 静音设置 |
|  |  | 结构属性 | 类型 | 默认值 | 必填 | 说明 |
|  | muteMicrophone | Boolean | false | 否 | 是否静音麦克风 |
|  | muteEarphone | Boolean | false | 否 | 是否静音耳机 |  success function  否 接口调用成功的回调函数  fail function  否 接口调用失败的回调函数  complete function  否 接口调用结束的回调函数（调用成功、失败都会执行）
