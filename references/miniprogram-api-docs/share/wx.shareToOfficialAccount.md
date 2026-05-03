> Source: https://developers.weixin.qq.com/miniprogram/dev/api/share/wx.shareToOfficialAccount.html

## wx.shareToOfficialAccount(Object object)

基础库 3.9.2 开始支持，低版本需做[兼容处理](../../framework/compatibility.html)。

**以 [Promise 风格](../../framework/app-service/api.html#%E5%BC%82%E6%AD%A5-API-%E8%BF%94%E5%9B%9E-Promise) 调用**：不支持

**需要页面权限**：当前是插件页面时，宿主小程序不能调用该接口，反之亦然

**小程序插件**：不支持

**微信 鸿蒙 OS 版**：支持

## # 功能描述

支持拉起贴图发表页，用户可将图片与文字内容发表为贴图。

## # 参数

### # Object object
 | 属性 | 类型 | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| title | string |  | 是 | 贴图的标题 |
| content | string |  | 否 | 贴图的正文 |
| tags | Array.<string> |  | 否 | 贴图的标签 |
| images | Array.<string> |  | 否 | 贴图的图片，必须为本地路径或临时路径 |
| path | string |  | 否 | 开发者自定义小程序路径 |
| success | function |  | 否 | 接口调用成功的回调函数 |
| fail | function |  | 否 | 接口调用失败的回调函数 |
| complete | function |  | 否 | 接口调用结束的回调函数（调用成功、失败都会执行） |
## # 示例代码

```javascript
wx.shareToOfficialAccount({
  title: '标题',
  content: '正文',
  tags: ['标签1', '标签2'],
  success: (res) => {
    // 贴图发表成功时触发
    // 注：因平台隐私限制，无法拿到发表后的贴图链接
    console.log(res)
  },
  fail: (err) => {
    // 用户主动退出贴图发表页时触发
    console.log(err)
  },
  complete: (res) => {
    // 统计接口总共调用次数
    console.log(res)
  },
})
```

## # 推荐图标

推荐使用贴图品牌图标作为该功能按钮，可使用下列高清素材：




 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)