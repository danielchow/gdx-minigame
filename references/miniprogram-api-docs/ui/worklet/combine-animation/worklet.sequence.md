> Source: https://developers.weixin.qq.com/miniprogram/dev/api/ui/worklet/combine-animation/worklet.sequence.html

## AnimationObject worklet.sequence(AnimationObject animationN)

**小程序插件**：不支持

相关文档: [worklet 动画](../../../../framework/runtime/skyline/worklet.html)

## # 功能描述

组合动画序列，依次执行传入的动画。

## # 参数

### # AnimationObject animationN

动画对象

## # 返回值

### # AnimationObject

返回 AnimationObject 类型值，可直接赋值给 SharedValue。

## # 示例代码

```javascript
const { shared, sequence, timing, spring } = wx.worklet
const offset = shared(0)
offset.value = sequence(timing(100), spring(0))
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)