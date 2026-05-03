> Source: https://developers.weixin.qq.com/miniprogram/dev/api/device/contact/wx.chooseContact.html

## wx.chooseContact(Object object)

基础库 2.8.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**以 [Promise 风格](../../../framework/app-service/api.html#%E5%BC%82%E6%AD%A5-API-%E8%BF%94%E5%9B%9E-Promise) 调用**：不支持

**小程序插件**：不支持

**微信 鸿蒙 OS 版**：支持

## # 功能描述

拉起手机通讯录，选择联系人。

## # 参数

### # Object object
 | 属性 | 类型 | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| success | function |  | 否 | 接口调用成功的回调函数 |
| fail | function |  | 否 | 接口调用失败的回调函数 |
| complete | function |  | 否 | 接口调用结束的回调函数（调用成功、失败都会执行） |
#### # object.success 回调函数

##### # 参数
 [#](#Object-object-2) Object object | 属性 | 类型 | 说明 |
| --- | --- | --- |
| phoneNumber | string | 手机号 |
| displayName | string | 联系人姓名 |
| phoneNumberList | string | 选定联系人的所有手机号（部分 Android 系统只能选联系人而不能选特定手机号） | The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)