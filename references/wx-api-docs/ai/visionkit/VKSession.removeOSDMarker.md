> Source: https://developers.weixin.qq.com/minigame/dev/api/ai/visionkit/VKSession.removeOSDMarker.html
# VKSession.removeOSDMarker(number markerId)
基础库 2.32.1 开始支持，低版本需做[兼容处理](../../../guide/runtime/client-lib/compatibility.html)。
## 功能描述
删除一个 OSD marker，要求调 [wx.createVKSession](wx.createVKSession.html) 时传入的 track.OSD 为 true
## 参数
### number markerId
marker id
