> Source: https://developers.weixin.qq.com/minigame/dev/api/network/websocket/SocketTask.onClose.html
# SocketTask.onClose(function listener)
**微信 Windows 版**：支持

**微信 Mac 版**：支持

**微信 鸿蒙 OS 版**：支持

相关文档: [网络使用说明](../../../guide/base-ability/network.html)、[局域网通信]((mDNS))
## 功能描述
监听 WebSocket 连接关闭事件
## 参数
### function listener
WebSocket 连接关闭事件的监听函数
#### 参数
##### Object res | 属性 | 类型 | 说明 |
| --- | --- | --- |
| code | number | 一个数字值表示关闭连接的状态号，表示连接被关闭的原因。 |
| reason | string | 一个可读的字符串，表示连接被关闭的原因。 |
