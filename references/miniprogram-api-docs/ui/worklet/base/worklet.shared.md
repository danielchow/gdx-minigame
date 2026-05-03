> Source: https://developers.weixin.qq.com/miniprogram/dev/api/ui/worklet/base/worklet.shared.html

## SharedValue worklet.shared(any initialValue)

**小程序插件**：不支持

相关文档: [worklet 动画](../../../../framework/runtime/skyline/worklet.html)

## # 功能描述

创建共享变量 `SharedValue`，用于跨线程共享数据和驱动动画。

## # 参数

### # any initialValue

初始值，可通过 `.value` 属性进行读取和修改。类型可以是 `number | string | bool | null | undefined | Object | Array | Function`。

## # 返回值

### # SharedValue

返回 SharedValue 类型值，可被 worklet 函数捕获。

## # 示例代码

```javascript
const offset = wx.worklet.shared(0)
const someWorkletFn = () => {
 'worklet'
 console.log('offset: ', offset.value)
}
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)