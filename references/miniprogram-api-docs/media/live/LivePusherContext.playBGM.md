> Source: https://developers.weixin.qq.com/miniprogram/dev/api/media/live/LivePusherContext.playBGM.html

## LivePusherContext.playBGM(Object object)

基础库 2.4.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**以 [Promise 风格](../../../framework/app-service/api.html#%E5%BC%82%E6%AD%A5-API-%E8%BF%94%E5%9B%9E-Promise) 调用**：不支持

**小程序插件**：支持

相关文档: [live-pusher 组件](../../../component/live-pusher.html)

## # 功能描述

播放背景音

## # 参数

### # Object object
 | 属性 | 类型 | 默认值 | 必填 | 说明 | 最低版本 |
| --- | --- | --- | --- | --- | --- |
| url | string |  | 是 | 加入背景混音的资源地址 |  |
| startTimeMs | number | 0 | 否 | BGM开始播时间点，单位ms，若入参为负或超过文件长度，则默认从文件开头进行播放 | 2.31.0 |
| endTimeMs | number | 0 | 否 | BGM结束播放时间点，单位ms，0代表播放至文件结尾，若入参为负或超过文件长度，则默认播放至文件结尾 | 2.31.0 |
| success | function |  | 否 | 接口调用成功的回调函数 |  |
| fail | function |  | 否 | 接口调用失败的回调函数 |  |
| complete | function |  | 否 | 接口调用结束的回调函数（调用成功、失败都会执行） |  | The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)