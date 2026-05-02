> Source: https://developers.weixin.qq.com/minigame/dev/api/network/websocket/SocketTask.send.html
# SocketTask.send(Object object)
**以 [Promise 风格](../../../game-engine/worker/api.html#%E5%BC%82%E6%AD%A5-API-%E8%BF%94%E5%9B%9E-Promise) 调用**：不支持

**微信 Windows 版**：支持

**微信 Mac 版**：支持

**微信 鸿蒙 OS 版**：支持

相关文档: [网络使用说明](../../../guide/base-ability/network.html)、[局域网通信]((mDNS))
## 功能描述
通过 WebSocket 连接发送数据
## 参数
### Object object | 属性 | 类型 | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| data | string/ArrayBuffer |  | 是 | 需要发送的内容 |
| success | function |  | 否 | 接口调用成功的回调函数 |
| fail | function |  | 否 | 接口调用失败的回调函数 |
| complete | function |  | 否 | 接口调用结束的回调函数（调用成功、失败都会执行） |
