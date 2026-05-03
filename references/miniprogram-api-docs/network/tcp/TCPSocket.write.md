> Source: https://developers.weixin.qq.com/miniprogram/dev/api/network/tcp/TCPSocket.write.html

## TCPSocket.write(string|ArrayBuffer data)

**小程序插件**：不支持

**微信 Windows 版**：支持

**微信 Mac 版**：支持

**微信 鸿蒙 OS 版**：支持

相关文档: [网络使用说明](../../../framework/ability/network.html)

## # 功能描述

在 socket 上发送数据

## # 参数

### # string|ArrayBuffer data

要发送的数据

## # 示例代码

```javascript
const tcp = wx.createTCPSocket()
  tcp.write('hello, how are you')
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)