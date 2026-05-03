> Source: https://developers.weixin.qq.com/miniprogram/dev/api/ui/worklet/combine-animation/worklet.delay.html

## AnimationObject worklet.delay(number delayMS, AnimationObject delayedAnimation)

**小程序插件**：不支持

相关文档: [worklet 动画](../../../../framework/runtime/skyline/worklet.html)

## # 功能描述

延迟执行动画。

## # 参数

### # number delayMS

动画开始前等待的时间，单位：毫秒。

### # AnimationObject delayedAnimation

动画对象。

## # 返回值

### # AnimationObject

返回 `AnimationObject` 类型值，可直接赋值给 `SharedValue`。

## # 示例代码

```javascript
const { shared, repeat, timing } = wx.worklet
const offset = shared(0)
offset.value = delay(1000, timing(70));
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)