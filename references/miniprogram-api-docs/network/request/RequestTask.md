> Source: https://developers.weixin.qq.com/miniprogram/dev/api/network/request/RequestTask.html

## RequestTask

基础库 1.4.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

相关文档: [网络使用说明](../../../framework/ability/network.html)、[局域网通信](../../../framework/ability/mDNS.html)、[移动解析HttpDNS](../../../framework/ability/HTTPDNS.html)

网络请求任务对象

## # 方法

### # RequestTask.abort()

中断请求任务

### # RequestTask.onHeadersReceived(function listener)

监听 HTTP Response Header 事件。会比请求完成事件更早

### # RequestTask.offHeadersReceived(function listener)

移除 HTTP Response Header 事件的监听函数

### # RequestTask.onChunkReceived(function listener)

监听 Transfer-Encoding Chunk Received 事件。当接收到新的chunk时触发。

### # RequestTask.offChunkReceived(function listener)

移除 Transfer-Encoding Chunk Received 事件的监听函数

## # 示例代码

```js
const requestTask = wx.request({
  url: 'test.php', //仅为示例，并非真实的接口地址
  data: {
    x: '' ,
    y: ''
  },
  header: {
    'content-type': 'application/json'
  },
  success (res) {
    console.log(res.data)
  }
})
requestTask.abort() // 取消请求任务
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)