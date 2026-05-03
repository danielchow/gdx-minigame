> Source: https://developers.weixin.qq.com/miniprogram/dev/api/ui/worklet/tool-function/worklet.runOnUI.html

## function worklet.runOnUI(function fn)

**小程序插件**：不支持

相关文档: [worklet 动画](../../../../framework/runtime/skyline/worklet.html)

## # 功能描述

在 UI 线程执行 worklet 函数。

## # 参数

### # function fn

worklet 类型函数。

## # 返回值

### # function

`runOnUI` 为高阶函数，返回一个函数，执行时运行在 `UI` 线程。

## # 示例代码

```javascript
function someWorklet(greeting) {
  'worklet';
  console.log('hello', greeting); // print: [ui] hello Skyline
}

wx.worklet.runOnUI(someWorklet)('Skyline')
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)