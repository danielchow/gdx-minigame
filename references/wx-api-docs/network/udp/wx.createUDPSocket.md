> Source: https://developers.weixin.qq.com/minigame/dev/api/network/udp/wx.createUDPSocket.html
# UDPSocket wx.createUDPSocket(string type)
基础库 2.7.0 开始支持，低版本需做[兼容处理](../../../guide/runtime/client-lib/compatibility.html)。

**微信 Windows 版**：支持

**微信 Mac 版**：支持

**微信 鸿蒙 OS 版**：支持

相关文档: [网络使用说明](../../../guide/base-ability/network.html)、[局域网通信]((mDNS))
## 功能描述
创建一个 UDP Socket 实例。使用前请注意阅读[相关说明](../../../guide/base-ability/network.html)。
## 参数
### string type
基础库 2.18.0 开始支持，低版本需做[兼容处理](../../../guide/runtime/client-lib/compatibility.html)。

套接字族，必须是 udp4 或 udp6，默认是 udp4
## 返回值
### UDPSocket
一个 UDP Socket 实例
