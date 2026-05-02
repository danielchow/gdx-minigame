> Source: https://developers.weixin.qq.com/minigame/dev/api/worker/Worker.onError.html
# Worker.onError(function listener)
相关文档: [多线程 Worker](../../guide/base-ability/workers.html)
## 功能描述
监听 Worker 线程错误事件。当 Worker 线程中发生脚本错误时会触发此事件。
## 参数
### function listener
Worker 线程错误事件的监听函数
#### 参数
##### Object res | 属性 | 类型 | 说明 |
| --- | --- | --- |
| error | Object | 错误对象
## 示例代码
```js
const worker = wx.createWorker('workers/request/index.js')

worker.onError(function (error) {
  console.error('Worker 错误:', error)
})
```
