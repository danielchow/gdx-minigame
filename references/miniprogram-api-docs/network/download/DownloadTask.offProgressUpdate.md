> Source: https://developers.weixin.qq.com/miniprogram/dev/api/network/download/DownloadTask.offProgressUpdate.html

## DownloadTask.offProgressUpdate(function listener)

基础库 2.1.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**小程序插件**：支持

相关文档: [网络使用说明](../../../framework/ability/network.html)、[局域网通信](../../../framework/ability/mDNS.html)

## # 功能描述

移除下载进度变化事件的监听函数

## # 参数

### # function listener

onProgressUpdate 传入的监听函数。不传此参数则移除所有监听函数。

## # 示例代码

```js
const listener = function (res) { console.log(res) }

DownloadTask.onProgressUpdate(listener)
DownloadTask.offProgressUpdate(listener) // 需传入与监听时同一个的函数对象
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)