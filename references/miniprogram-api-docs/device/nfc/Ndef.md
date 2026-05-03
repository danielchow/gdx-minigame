> Source: https://developers.weixin.qq.com/miniprogram/dev/api/device/nfc/Ndef.html

## Ndef

基础库 2.11.2 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

相关文档: [近场通信 (NFC)](../../../framework/device/nfc.html)

Ndef 标签

## # 方法

### # Ndef.connect()

连接 NFC 标签

### # Ndef.close()

断开连接

### # Ndef.setTimeout(Object object)

设置超时时间

### # Ndef.isConnected()

检查是否已连接

### # Ndef.onNdefMessage(function callback)

监听 Ndef 消息

### # Ndef.offNdefMessage(function callback)

取消监听 Ndef 消息

### # Ndef.writeNdefMessage(Object object)

重写 Ndef 标签内容
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)