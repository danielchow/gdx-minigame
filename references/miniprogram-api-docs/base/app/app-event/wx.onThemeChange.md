> Source: https://developers.weixin.qq.com/miniprogram/dev/api/base/app/app-event/wx.onThemeChange.html

## wx.onThemeChange(function listener)

基础库 2.11.0 开始支持，低版本需做[兼容处理](../../../../framework/compatibility.html)。

**小程序插件**：不支持

相关文档: [DarkMode 适配指南](../../../../framework/ability/darkmode.html)

## # 功能描述

监听系统主题改变事件。该事件与 [`App.onThemeChange`](../../../../reference/api/App.html#onThemeChange-Object-object) 的回调时机一致。

## # 参数

### # function listener

系统主题改变事件的监听函数

#### # 参数

##### # Object res
 |  | 属性 | 类型 | 说明 |
| --- | --- | --- | --- |
|  | theme | string | 系统当前的主题，取值为`light`或`dark` |
|  | 合法值 | 说明 |
| dark | 深色主题 |
| light | 浅色主题 |
## # 注意

- 只有在全局配置"darkmode": true时才会触发此事件。

 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)