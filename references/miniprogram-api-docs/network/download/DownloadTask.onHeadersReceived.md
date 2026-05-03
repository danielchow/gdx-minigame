> Source: https://developers.weixin.qq.com/miniprogram/dev/api/network/download/DownloadTask.onHeadersReceived.html

## DownloadTask.onHeadersReceived(function listener)

基础库 2.1.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**小程序插件**：支持

相关文档: [网络使用说明](../../../framework/ability/network.html)、[局域网通信](../../../framework/ability/mDNS.html)

## # 功能描述

监听 HTTP Response Header 事件。会比请求完成事件更早

## # 参数

### # function listener

HTTP Response Header 事件的监听函数

#### # 参数

##### # Object res
 | 属性 | 类型 | 说明 |
| --- | --- | --- |
| header | Object | 开发者服务器返回的 HTTP Response Header | The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)