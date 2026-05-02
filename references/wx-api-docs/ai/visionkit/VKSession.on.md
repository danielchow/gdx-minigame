> Source: https://developers.weixin.qq.com/minigame/dev/api/ai/visionkit/VKSession.on.html
# VKSession.on(string eventName, function fn)
基础库 2.32.1 开始支持，低版本需做[兼容处理](../../../guide/runtime/client-lib/compatibility.html)。
## 功能描述
监听会话事件。
## 参数
### string eventName
事件名称

**eventName 的合法值**
 | 值 | 说明 | 最低版本 |
| --- | --- | --- |
| resize | 相机尺寸变化事件，回调参数为相机尺寸 |  |
| addAnchors | 增加 anchor 事件，回调参数为 VKPlaneAnchor/VKMarkerAnchor/VKOSDAnchor 列表（只有v2版本支持） 或 VKFaceAnchor/VKOCRAnchor/VKHandAnchor/VKBodyAnchor列表（v1、v2都支持） | 2.22.0 |
| updateAnchors | 更新 anchor 事件，回调参数为 VKPlaneAnchor/VKMarkerAnchor/VKOSDAnchor 列表（只有v2版本支持） 或 VKFaceAnchor/VKOCRAnchor/VKHandAnchor/VKBodyAnchor列表（v1、v2都支持） | 2.22.0 |
| removeAnchors | 删除 anchor 事件，回调参数为 VKPlaneAnchor/VKMarkerAnchor/VKOSDAnchor 列表（只有v2版本支持） 或 VKFaceAnchor/VKOCRAnchor/VKHandAnchor/VKBodyAnchor 列表（v1、v2都支持） | 2.22.0
### function fn
事件监听函数
