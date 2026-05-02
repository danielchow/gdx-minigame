> Source: https://developers.weixin.qq.com/minigame/dev/api/network/websocket/wx.connectSocket.html
# SocketTask wx.connectSocket(Object object)
推荐使用 [SocketTask](SocketTask.html) 的方式去管理 webSocket 链接，每一条链路的生命周期都更加可控，同时存在多个 webSocket 的链接的情况下使用 wx 前缀的方法可能会带来一些和预期不一致的情况。

**以 [Promise 风格](../../../game-engine/worker/api.html#异步-API-返回-Promise) 调用**：不支持

**微信 Windows 版**：支持

**微信 Mac 版**：支持

**微信 鸿蒙 OS 版**：支持

相关文档: [网络使用说明](../../../guide/base-ability/network.html)、[局域网通信](errormDNS))
## 功能描述
创建一个 WebSocket 连接。使用前请注意阅读[相关说明](../../../guide/base-ability/network.html)。
## 参数
### Object object | 属性 | 类型 | 默认值 | 必填 | 说明 | 最低版本 |
| --- | --- | --- | --- | --- | --- |
| url | string |  | 是 | 开发者服务器 wss 接口地址 |  |
| header | Object |  | 否 | HTTP Header，Header 中不能设置 Referer |  |
| protocols | Array.<string> |  | 否 | 子协议数组 | 1.4.0 |
| tcpNoDelay | boolean | false | 否 | 建立 TCP 连接的时候的 TCP_NODELAY 设置 | 2.4.0 |
| perMessageDeflate | boolean | false | 否 | 是否开启压缩扩展 | 2.8.0 |
| timeout | number |  | 否 | 超时时间，单位为毫秒 | 2.10.0 |
| forceCellularNetwork | boolean | false | 否 | 强制使用蜂窝网络发送请求 | 2.29.0 |
| success | function |  | 否 | 接口调用成功的回调函数 |  |
| fail | function |  | 否 | 接口调用失败的回调函数 |  |
| complete | function |  | 否 | 接口调用结束的回调函数（调用成功、失败都会执行）
## 返回值
### SocketTask
基础库 1.7.0 开始支持，低版本需做[兼容处理](../../../guide/runtime/client-lib/compatibility.html)。

WebSocket 任务
## 并发数
- 1.7.0 及以上版本，最多可以同时存在 5 个 WebSocket 连接。
 - 1.7.0 以下版本，一个小程序同时只能有一个 WebSocket 连接，如果当前已存在一个 WebSocket 连接，会自动关闭该连接，并重新创建一个 WebSocket 连接。
## 示例代码
```js
wx.connectSocket({
  url: 'wss://example.qq.com',
  header:{
    'content-type': 'application/json'
  }
})
```
