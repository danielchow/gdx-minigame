> Source: https://developers.weixin.qq.com/miniprogram/dev/api/route/EventChannel.html

## EventChannel

基础库 2.7.3 开始支持，低版本需做[兼容处理](../../framework/compatibility.html)。

页面间事件通信通道

## # 方法

### # EventChannel.emit(string eventName, any args)

触发一个事件

### # EventChannel.on(string eventName, EventCallback fn)

持续监听一个事件

### # EventChannel.once(string eventName, EventCallback fn)

监听一个事件一次，触发后失效

### # EventChannel.off(string eventName, EventCallback fn)

取消监听一个事件。给出第二个参数时，只取消给出的监听函数，否则取消所有监听函数
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)