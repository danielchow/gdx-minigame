> Source: https://developers.weixin.qq.com/miniprogram/dev/api/media/video/wx.saveVideoToPhotosAlbum.html

## wx.saveVideoToPhotosAlbum(Object object)

基础库 1.2.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**以 [Promise 风格](../../../framework/app-service/api.html#%E5%BC%82%E6%AD%A5-API-%E8%BF%94%E5%9B%9E-Promise) 调用**：支持

**[用户授权](../../../framework/open-ability/authorize.html)**：需要 scope.writePhotosAlbum

**小程序插件**：支持，需要小程序基础库版本不低于 [1.9.6](../../../framework/compatibility.html)

**微信 Windows 版**：支持

**微信 Mac 版**：支持

**微信 鸿蒙 OS 版**：支持

## # 功能描述

保存视频到系统相册。支持mp4视频格式。

## # 参数

### # Object object
 | 属性 | 类型 | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| filePath | string |  | 是 | 视频文件路径，可以是临时文件路径也可以是永久文件路径 (本地路径) |
| success | function |  | 否 | 接口调用成功的回调函数 |
| fail | function |  | 否 | 接口调用失败的回调函数 |
| complete | function |  | 否 | 接口调用结束的回调函数（调用成功、失败都会执行） |
## # 示例代码

```js
wx.saveVideoToPhotosAlbum({
  filePath: 'wxfile://xxx',
  success (res) {
    console.log(res.errMsg)
  }
})
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)