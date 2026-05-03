> Source: https://developers.weixin.qq.com/miniprogram/dev/api/ad/InterstitialAd.offError.html

## InterstitialAd.offError(function listener)

**小程序插件**：不支持

## # 功能描述

移除插屏错误事件的监听函数

## # 参数

### # function listener

onError 传入的监听函数。不传此参数则移除所有监听函数。

## # 示例代码

```js
const listener = function (res) { console.log(res) }

InterstitialAd.onError(listener)
InterstitialAd.offError(listener) // 需传入与监听时同一个的函数对象
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)