> Source: https://developers.weixin.qq.com/miniprogram/dev/api/storage/wx.clearStorageSync.html

## wx.clearStorageSync()

**以 [Promise 风格](../../framework/app-service/api.html#%E5%BC%82%E6%AD%A5-API-%E8%BF%94%E5%9B%9E-Promise) 调用**：支持

**小程序插件**：不支持

**微信 Windows 版**：支持

**微信 Mac 版**：支持

**微信 鸿蒙 OS 版**：支持

相关文档: [存储策略](../../framework/ability/storage.html)

## # 功能描述

[wx.clearStorage](wx.clearStorage.html) 的同步版本

## # 示例代码

```js
wx.clearStorage()
```

```js
try {
  wx.clearStorageSync()
} catch(e) {
  // Do something when catch error
}
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)