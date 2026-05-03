> Source: https://developers.weixin.qq.com/miniprogram/dev/api/open-api/privacy/wx.openPrivacyContract.html

## wx.openPrivacyContract(Object object)

基础库 2.32.3 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**以 [Promise 风格](../../../framework/app-service/api.html#%E5%BC%82%E6%AD%A5-API-%E8%BF%94%E5%9B%9E-Promise) 调用**：不支持

**小程序插件**：不支持

**微信 Windows 版**：支持

**微信 Mac 版**：支持

**微信 鸿蒙 OS 版**：支持

## # 功能描述

跳转至隐私协议页面。隐私合规开发指南详情可见[《小程序隐私协议开发指南》](https://developers.weixin.qq.com/miniprogram/dev/framework/user-privacy/PrivacyAuthorize.html)

## # 参数

### # Object object
 | 属性 | 类型 | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| success | function |  | 否 | 接口调用成功的回调函数 |
| fail | function |  | 否 | 接口调用失败的回调函数 |
| complete | function |  | 否 | 接口调用结束的回调函数（调用成功、失败都会执行） |
## # 具体说明：

-
- 一定要调用 wx.openPrivacyContract 接口吗？


- 不是。开发者也可以选择在小程序页面内自行展示完整的隐私协议。但推荐使用该接口。


## # 示例代码

```js
wx.openPrivacyContract({
  success: () => {}, // 打开成功
  fail: () => {}, // 打开失败
  complete: () => {}
})
```

## # 完整示例demo

demo1: 演示使用 `wx.getPrivacySetting` 和 `<button open-type="agreePrivacyAuthorization">` 在首页处理隐私弹窗逻辑
[https://developers.weixin.qq.com/s/gi71sGm67hK0](https://developers.weixin.qq.com/s/gi71sGm67hK0)

demo2: 演示使用 `wx.onNeedPrivacyAuthorization` 和 `<button open-type="agreePrivacyAuthorization">` 在多个页面处理隐私弹窗逻辑，同时演示了如何处理多个隐私接口同时调用。
[https://developers.weixin.qq.com/s/hndZUOmA7gKn](https://developers.weixin.qq.com/s/hndZUOmA7gKn)

demo3: 演示 `wx.onNeedPrivacyAuthorization`、`wx.requirePrivacyAuthorize`、`<button open-type="agreePrivacyAuthorization">` 和 `<input type="nickname">` 组件如何结合使用
[https://developers.weixin.qq.com/s/jX7xWGmA7UKa](https://developers.weixin.qq.com/s/jX7xWGmA7UKa)

demo4: 演示使用 `wx.onNeedPrivacyAuthorization` 和 `<button open-type="agreePrivacyAuthorization">` 在多个 tabBar 页面处理隐私弹窗逻辑。
[https://developers.weixin.qq.com/s/g6BWZGmt7XK9](https://developers.weixin.qq.com/s/g6BWZGmt7XK9)
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)