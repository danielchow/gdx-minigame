> Source: https://developers.weixin.qq.com/miniprogram/dev/api/ui/worklet/combine-animation/worklet.repeat.html

## AnimationObject worklet.repeat(AnimationObject animation, number numberOfReps, boolean reverse, function callback)

**小程序插件**：不支持

相关文档: [worklet 动画](../../../../framework/runtime/skyline/worklet.html)

## # 功能描述

重复执行动画。

## # 参数

### # AnimationObject animation

动画对象

### # number numberOfReps

重复次数。为负值时一直循环，直到被取消动画。

### # boolean reverse

反向运行动画，每周期结束动画由尾到头运行。该字段仅对 timing 和 spring 返回的动画对象生效。

### # function callback

动画完成回调。动画被取消时，返回 fasle，正常完成时返回 true。

## # 返回值

### # AnimationObject

返回 `AnimationObject` 类型值，可直接赋值给 `SharedValue`。

## # 示例代码

```javascript
const { shared, repeat, timing } = wx.worklet
const offset = shared(0)
offset.value = repeat(timing(70), 2, true);
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)