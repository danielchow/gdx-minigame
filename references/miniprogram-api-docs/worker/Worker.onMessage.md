> Source: https://developers.weixin.qq.com/miniprogram/dev/api/worker/Worker.onMessage.html

## Worker.onMessage(function listener)

**小程序插件**：不支持

相关文档: [多线程 Worker](../../framework/workers.html)

## # 功能描述

监听主线程/Worker 线程向当前线程发送的消息的事件。

## # 参数

### # function listener

主线程/Worker 线程向当前线程发送的消息的事件的监听函数

#### # 参数

##### # Object res
 | 属性 | 类型 | 说明 |
| --- | --- | --- |
| message | Object | 主线程/Worker 线程向当前线程发送的消息 | The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)