> Source: https://developers.weixin.qq.com/miniprogram/dev/api/device/keyboard/wx.onKeyboardHeightChange.html

## wx.onKeyboardHeightChange(function listener)

基础库 2.7.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**小程序插件**：不支持

**微信 鸿蒙 OS 版**：支持

## # 功能描述

监听键盘高度变化事件

## # 参数

### # function listener

键盘高度变化事件的监听函数

#### # 参数

##### # Object res
 | 属性 | 类型 | 说明 |
| --- | --- | --- |
| height | number | 键盘高度 |
## # 示例代码

```js
wx.onKeyboardHeightChange(res => {
  console.log(res.height)
})
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)