> Source: https://developers.weixin.qq.com/miniprogram/dev/api/navigate/wx.exitMiniProgram.html

## wx.exitMiniProgram(Object object)

基础库 2.17.3 开始支持，低版本需做[兼容处理](../../framework/compatibility.html)。

**以 [Promise 风格](../../framework/app-service/api.html#%E5%BC%82%E6%AD%A5-API-%E8%BF%94%E5%9B%9E-Promise) 调用**：支持

**小程序插件**：支持，需要小程序基础库版本不低于 [2.30.1](../../framework/compatibility.html)

**微信 Windows 版**：支持

**微信 Mac 版**：支持

**微信 鸿蒙 OS 版**：支持

**限制**：仅在点击行为时调用

## # 功能描述

退出当前小程序

## # 参数

### # Object object
 | 属性 | 类型 | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| success | function |  | 否 | 接口调用成功的回调函数 |
| fail | function |  | 否 | 接口调用失败的回调函数 |
| complete | function |  | 否 | 接口调用结束的回调函数（调用成功、失败都会执行） | The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)