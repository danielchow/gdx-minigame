> Source: https://developers.weixin.qq.com/minigame/dev/api/device/bluetooth/wx.onBluetoothAdapterStateChange.html
# wx.onBluetoothAdapterStateChange(function listener)
基础库 2.9.2 开始支持，低版本需做[兼容处理](../../../guide/runtime/client-lib/compatibility.html)。

**微信 鸿蒙 OS 版**：支持

相关文档: [蓝牙介绍](../../../guide/device/bluetooth.html)
## 功能描述
监听蓝牙适配器状态变化事件
## 参数
### function listener
蓝牙适配器状态变化事件的监听函数
#### 参数
##### Object res | 属性 | 类型 | 说明 |
| --- | --- | --- |
| available | boolean | 蓝牙适配器是否可用 |
| discovering | boolean | 蓝牙适配器是否处于搜索状态
## 示例代码
[在开发者工具中预览效果](https://developers.weixin.qq.com/s/pQU51zmz7a3K)

```js
wx.onBluetoothAdapterStateChange(function (res) {
  console.log('adapterState changed, now is', res)
})
```
