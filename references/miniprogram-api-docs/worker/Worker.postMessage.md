> Source: https://developers.weixin.qq.com/miniprogram/dev/api/worker/Worker.postMessage.html

## Worker.postMessage(Object message)

**小程序插件**：不支持

相关文档: [多线程 Worker](../../framework/workers.html)

## # 功能描述

向主线程/Worker 线程发送的消息。

## # 参数

### # Object message

需要发送的消息。

## # 示例代码

worker 线程中

```js
worker.postMessage({
  msg: 'hello from worker'
})
```

主线程中

```js
const worker = wx.createWorker('workers/request/index.js')

worker.postMessage({
  msg: 'hello from main'
})
```

## # 提醒

在基础库版本2.20.2之前，postMessage仅支持传递可序列化的key-value对象。
在基础库2.20.2之后，postMessage支持传递任意类型的数据。
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)