> Source: https://developers.weixin.qq.com/minigame/dev/api/base/app/life-cycle/wx.getLaunchOptionsSync.html
# Object wx.getLaunchOptionsSync()
**微信 Windows 版**：支持

**微信 Mac 版**：支持

**微信 鸿蒙 OS 版**：支持
## 功能描述
获取小游戏冷启动时的参数。热启动参数通过 [wx.onShow](wx.onShow.html) 或 [wx.getEnterOptionsSync](wx.getEnterOptionsSync.html) 接口获取。
## 返回值
### Object
启动参数
 |  | 属性 | 类型 | 说明 |
| --- | --- | --- | --- |
|  | scene | number | 启动小游戏的场景值 |
|  | query | Record.<string, string> | 启动小游戏的 query 参数 |
|  | shareTicket | string | shareTicket，详见获取更多转发信息 |
|  | referrerInfo | object | 来源信息。从另一个小程序、公众号或 App 进入小程序时返回。否则返回 `{}`。(参见后文注意) |
|  |  | 结构属性 | 类型 | 说明 |
|  | appId | string | 来源小程序、公众号或 App 的 appId |
|  | extraData | object | 来源小程序传过来的数据，scene=1037或1038时支持 |  hostExtraData Object 宿主传递的数据，第三方 app 中运行小游戏时返回  |  | 结构属性 | 类型 | 说明 |
| --- | --- | --- | --- |
|  | host_scene | string | 宿主app对应的场景值 |  chatType number 从微信群聊/单聊打开小程序时，chatType 表示具体微信群聊/单聊类型  | 合法值 | 说明 |
| --- | --- |
| 1 | 微信联系人单聊 |
| 2 | 企业微信联系人单聊 |
| 3 | 普通微信群聊 |
| 4 | 企业微信互通群聊
## 返回有效 referrerInfo 的场景 | 场景值 | 场景 | appId含义 |
| --- | --- | --- |
| 1020 | 公众号 profile 页相关小程序列表 | 来源公众号 |
| 1035 | 公众号自定义菜单 | 来源公众号 |
| 1036 | App 分享消息卡片 | 来源App |
| 1037 | 小程序打开小程序 | 来源小程序 |
| 1038 | 从另一个小程序返回 | 来源小程序 |
| 1043 | 公众号模板消息 | 来源公众号
## 注意
部分版本在无`referrerInfo`的时候会返回 `undefined`，
建议使用 `options.referrerInfo && options.referrerInfo.appId` 进行判断。
