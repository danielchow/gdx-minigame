> Source: https://developers.weixin.qq.com/minigame/dev/api/network/request/RequestTask.offChunkReceived.html
# RequestTask.offChunkReceived(function listener)
基础库 2.20.1 开始支持，低版本需做[兼容处理](../../../guide/runtime/client-lib/compatibility.html)。

相关文档: [网络使用说明](../../../guide/base-ability/network.html)、[局域网通信]((mDNS))、[移动解析HttpDNS](../../../guide/base-ability/HTTPDNS.html)
## 功能描述
移除 Transfer-Encoding Chunk Received 事件的监听函数
## 参数
### function listener
onChunkReceived 传入的监听函数。不传此参数则移除所有监听函数。
## 示例代码
```js
const listener = function (res) { console.log(res) }

RequestTask.onChunkReceived(listener)
RequestTask.offChunkReceived(listener) // 需传入与监听时同一个的函数对象
```
