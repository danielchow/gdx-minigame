> Source: https://developers.weixin.qq.com/miniprogram/dev/api/network/udp/UDPSocket.onMessage.html

## UDPSocket.onMessage(function listener)

**小程序插件**：支持，需要小程序基础库版本不低于 [2.11.1](../../../framework/compatibility.html)

**微信 Windows 版**：支持

**微信 Mac 版**：支持

**微信 鸿蒙 OS 版**：支持

相关文档: [网络使用说明](../../../framework/ability/network.html)、[局域网通信](../../../framework/ability/mDNS.html)

## # 功能描述

监听收到消息的事件

## # 参数

### # function listener

收到消息的事件的监听函数

#### # 参数

##### # Object res
 |  | 属性 | 类型 | 说明 |
| --- | --- | --- | --- |
|  | message | ArrayBuffer | 收到的消息。消息长度需要小于4096。 |
|  | remoteInfo | Object | 发送端地址信息 |
|  |  | 结构属性 | 类型 | 说明 |
|  | address | string | 发送消息的 socket 的地址 |
|  | family | string | 使用的协议族，为 IPv4 或者 IPv6 |
|  | port | number | 端口号 |
|  | size | number | message 的大小，单位：字节 |  localInfo Object 接收端地址信息，2.18.0 起支持  |  | 结构属性 | 类型 | 说明 |
| --- | --- | --- | --- |
|  | address | string | 接收消息的 socket 的地址 |
|  | family | string | 使用的协议族，为 IPv4 或者 IPv6 |
|  | port | number | 端口号 | The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)