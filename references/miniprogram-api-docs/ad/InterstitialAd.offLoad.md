> Source: https://developers.weixin.qq.com/miniprogram/dev/api/ad/InterstitialAd.offLoad.html

## InterstitialAd.offLoad(function listener)

**小程序插件**：不支持

## # 功能描述

移除插屏广告加载事件的监听函数

## # 参数

### # function listener

onLoad 传入的监听函数。不传此参数则移除所有监听函数。

## # 示例代码

```js
const listener = function (res) { console.log(res) }

InterstitialAd.onLoad(listener)
InterstitialAd.offLoad(listener) // 需传入与监听时同一个的函数对象
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)