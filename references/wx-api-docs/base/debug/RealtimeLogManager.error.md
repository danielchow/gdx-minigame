> Source: https://developers.weixin.qq.com/minigame/dev/api/base/debug/RealtimeLogManager.error.html
# RealtimeLogManager.error()
基础库 2.14.4 开始支持，低版本需做[兼容处理](../../../guide/runtime/client-lib/compatibility.html)。

相关文档: [实时日志](../../../guide/runtime/debug/realtimelog/index.html)
## 功能描述
写 error 日志，暂不支持在插件使用
## 参数
### Object|Array.<any>|number|string ...args
日志内容，可以有任意多个。每次调用的参数的总大小不超过5Kb
