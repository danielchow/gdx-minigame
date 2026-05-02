> Source: https://developers.weixin.qq.com/minigame/dev/api/ai/visionkit/VKSession.setDepthOccRange.html
# VKSession.setDepthOccRange(number threshold)
基础库 3.0.0 开始支持，低版本需做[兼容处理](../../../guide/runtime/client-lib/compatibility.html)。
## 功能描述
更新 深度遮挡 Occ范围，要求调 [wx.createVKSession](wx.createVKSession.html) 时传入 {track: {depth: {mode: 2} } }
## 参数
### number threshold
阈值
