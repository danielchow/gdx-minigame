> Source: https://developers.weixin.qq.com/miniprogram/dev/api/payment/GlobalPayment.abort.html

## Promise GlobalPayment.abort()

基础库 3.7.3 开始支持，低版本需做[兼容处理](../../framework/compatibility.html)。

**小程序插件**：不支持

## # 功能描述

用户选择TPG的支付方式，界面会进入加载的Toast，等待开发者前往TPG完成预下单后携带预支付信息和交易单号调用 requestGlobalPayment，若开发者在
TPG预下单未成功或出现异常情况，可调用该接口主动终止TPG支付流程，界面加载的Toast将会隐藏，提示用户下单失败。

## # 返回值

### # Promise

object { errno: number, errMsg: string, requestId: string // 每个payment对象唯一 }
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)