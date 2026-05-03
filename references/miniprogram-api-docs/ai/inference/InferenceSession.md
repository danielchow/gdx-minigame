> Source: https://developers.weixin.qq.com/miniprogram/dev/api/ai/inference/InferenceSession.html

## InferenceSession

基础库 2.30.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

推理 Session 实例，通过[wx.createInferenceSession](wx.createInferenceSession.html) 接口获取该实例。使用前可参考[AI指南文档](https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/inference/tutorial.html)

## # 方法

### # InferenceSession.onLoad(function callback)

监听模型加载完成事件

### # InferenceSession.offLoad(function callback)

取消监听模型加载完成事件

### # InferenceSession.onError(function callback)

监听模型加载失败事件

### # InferenceSession.offError(function callback)

取消监听模型加载失败事件

### # Promise InferenceSession.run(Object tensors)

运行推断。需要在 session.onLoad 回调后使用。接口参数为 Tensors 对象，返回 Promise。一个 InferenceSession 被创建完成后可以重复多次调用 InferenceSession.run(), 直到调用 session.destroy() 进行销毁。

### # InferenceSession.destroy()

销毁 InferenceSession 实例
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)