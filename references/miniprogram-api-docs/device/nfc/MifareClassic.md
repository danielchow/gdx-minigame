> Source: https://developers.weixin.qq.com/miniprogram/dev/api/device/nfc/MifareClassic.html

## MifareClassic

基础库 2.11.2 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

相关文档: [近场通信 (NFC)](../../../framework/device/nfc.html)

MifareClassic 标签

## # 方法

### # MifareClassic.connect()

连接 NFC 标签

### # MifareClassic.close()

断开连接

### # MifareClassic.setTimeout(Object object)

设置超时时间

### # MifareClassic.isConnected()

检查是否已连接

### # MifareClassic.getMaxTransceiveLength()

获取最大传输长度

### # MifareClassic.transceive(Object object)

发送数据

对于MifareClassic的分块读写

- 指令 0x30 + 块号 可以用于读取某个块的数据
 - 指令 0xA0 + 块号 + 待写入数据 可以用于往某个块写入数据

 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)