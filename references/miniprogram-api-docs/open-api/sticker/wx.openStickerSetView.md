> Source: https://developers.weixin.qq.com/miniprogram/dev/api/open-api/sticker/wx.openStickerSetView.html

## wx.openStickerSetView(Object object)

基础库 3.0.1 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**以 [Promise 风格](../../../framework/app-service/api.html#%E5%BC%82%E6%AD%A5-API-%E8%BF%94%E5%9B%9E-Promise) 调用**：不支持

**小程序插件**：不支持

**微信 鸿蒙 OS 版**：支持

## # 功能描述

打开表情专辑

## # 参数

### # Object object
 | 属性 | 类型 | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| url | string |  | 是 | 表情专辑链接，可前往表情开放平台，在详情页中的「小程序跳转链接」入口复制 |
| success | function |  | 否 | 接口调用成功的回调函数 |
| fail | function |  | 否 | 接口调用失败的回调函数 |
| complete | function |  | 否 | 接口调用结束的回调函数（调用成功、失败都会执行） |
## # 示例代码

```js
wx.openStickerSetView({
  url: '',
  success(res) {}
})
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)