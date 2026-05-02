> Source: https://developers.weixin.qq.com/minigame/dev/api/device/mouse-event/wx.offMouseDown.html
# wx.offMouseDown(function listener) ## # 功能描述
移除鼠标按键按下事件的监听函数
## 参数
### function listener
onMouseDown 传入的监听函数。不传此参数则移除所有监听函数。
## 示例代码
```js
const listener = function (res) { console.log(res) }

wx.onMouseDown(listener)
wx.offMouseDown(listener) // 需传入与监听时同一个的函数对象
```
