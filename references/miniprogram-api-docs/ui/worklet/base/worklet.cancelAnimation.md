> Source: https://developers.weixin.qq.com/miniprogram/dev/api/ui/worklet/base/worklet.cancelAnimation.html

## worklet.cancelAnimation(SharedValue SharedValue)

**小程序插件**：不支持

相关文档: [worklet 动画](../../../../framework/runtime/skyline/worklet.html)

## # 功能描述

取消由 `SharedValue` 驱动的动画。

## # 参数

### # SharedValue SharedValue

共享变量。

## # 示例代码

```javascript
const { shared, timing, cancelAnimation } = wx.worklet
const offset = shared(0);
offset.value = timing(100);
cancelAnimation(offset)
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)