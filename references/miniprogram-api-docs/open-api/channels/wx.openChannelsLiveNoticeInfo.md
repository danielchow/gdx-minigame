> Source: https://developers.weixin.qq.com/miniprogram/dev/api/open-api/channels/wx.openChannelsLiveNoticeInfo.html

## wx.openChannelsLiveNoticeInfo(Object object)

基础库 3.13.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**以 [Promise 风格](../../../framework/app-service/api.html#%E5%BC%82%E6%AD%A5-API-%E8%BF%94%E5%9B%9E-Promise) 调用**：不支持

**小程序插件**：不支持

相关文档: [视频号直播](../../../framework/open-ability/channels-live.html)

## # 功能描述

打开视频号直播预告半屏

## # 参数

### # Object object
 | 属性 | 类型 | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| finderUserName | string |  | 是 | 视频号 id，以"sph"开头的id，可在视频号助手获取 |
| success | function |  | 否 | 接口调用成功的回调函数 |
| fail | function |  | 否 | 接口调用失败的回调函数 |
| complete | function |  | 否 | 接口调用结束的回调函数（调用成功、失败都会执行） | The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)