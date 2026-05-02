> Source: https://developers.weixin.qq.com/minigame/dev/api/storage/wx.removeStorageSync.html
# wx.removeStorageSync(string key)
**以 [Promise 风格](../../game-engine/worker/api.html#%E5%BC%82%E6%AD%A5-API-%E8%BF%94%E5%9B%9E-Promise) 调用**：支持

**微信 Windows 版**：支持

**微信 Mac 版**：支持

**微信 鸿蒙 OS 版**：支持

相关文档: [存储策略](../../guide/base-ability/storage.html)
## 功能描述
[wx.removeStorage](wx.removeStorage.html) 的同步版本
## 参数
### string key
本地缓存中指定的 key
## 示例代码
```js
wx.removeStorage({
  key: 'key',
  success (res) {
    console.log(res)
  }
})
```

```js
try {
  wx.removeStorageSync('key')
} catch (e) {
  // Do something when catch error
}
```
