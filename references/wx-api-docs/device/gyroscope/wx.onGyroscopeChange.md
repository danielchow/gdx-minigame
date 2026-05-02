> Source: https://developers.weixin.qq.com/minigame/dev/api/device/gyroscope/wx.onGyroscopeChange.html
# wx.onGyroscopeChange(function listener)
基础库 2.3.0 开始支持，低版本需做[兼容处理](../../../guide/runtime/client-lib/compatibility.html)。
## 功能描述
监听陀螺仪数据变化事件。频率根据 [wx.startGyroscope()](wx.startGyroscope.html) 的 interval 参数。可以使用 [wx.stopGyroscope()](wx.stopGyroscope.html) 停止监听。
## 参数
### function listener
陀螺仪数据变化事件的监听函数
#### 参数
##### Object res | 属性 | 类型 | 说明 |
| --- | --- | --- |
| x | number | x 轴的角速度 |
| y | number | y 轴的角速度 |
| z | number | z 轴的角速度 |
