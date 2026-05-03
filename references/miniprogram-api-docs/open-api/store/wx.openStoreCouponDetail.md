> Source: https://developers.weixin.qq.com/miniprogram/dev/api/open-api/store/wx.openStoreCouponDetail.html

## wx.openStoreCouponDetail(Object object)

基础库 3.8.5 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**以 [Promise 风格](../../../framework/app-service/api.html#%E5%BC%82%E6%AD%A5-API-%E8%BF%94%E5%9B%9E-Promise) 调用**：不支持

**小程序插件**：不支持

相关文档: [微信小店指引](https://developers.weixin.qq.com/doc/store/API/basics/component.html)

## # 功能描述

打开微信小店优惠券详情页

## # 参数

### # Object object
 | 属性 | 类型 | 默认值 | 必填 | 说明 | 最低版本 |
| --- | --- | --- | --- | --- | --- |
| couponId | string |  | 是 | 优惠券id，可以通过小店后台获取 |  |
| shopAppid | string |  | 是 | 小店appid，可以通过小店后台获取 |  |
| promoterShareLink | string |  | 是 | 推客参数，可以通过接口获取。 | 3.8.11 |
| success | function |  | 否 | 接口调用成功的回调函数 |  |
| fail | function |  | 否 | 接口调用失败的回调函数 |  |
| complete | function |  | 否 | 接口调用结束的回调函数（调用成功、失败都会执行） |  |
#### # object.fail 回调函数

##### # 参数
 [#](#Object-res) Object res | 属性 | 类型 | 说明 |
| --- | --- | --- |
| message | string | 错误信息 |
| code | number | 错误码 | The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)