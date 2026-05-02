> Source: https://developers.weixin.qq.com/minigame/dev/api/base/update/UpdateManager.onCheckForUpdate.html
# UpdateManager.onCheckForUpdate(function listener) ## # 功能描述
监听向微信后台请求检查更新结果事件。微信在小程序每次启动（包括热启动）时自动检查更新，不需由开发者主动触发。
## 参数
### function listener
向微信后台请求检查更新结果事件的监听函数
#### 参数
##### Object res | 属性 | 类型 | 说明 |
| --- | --- | --- |
| hasUpdate | boolean | 是否有新版本
## 示例代码
[示例代码](UpdateManager.html#%E7%A4%BA%E4%BE%8B%E4%BB%A3%E7%A0%81)
