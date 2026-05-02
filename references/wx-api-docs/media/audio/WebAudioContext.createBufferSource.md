> Source: https://developers.weixin.qq.com/minigame/dev/api/media/audio/WebAudioContext.createBufferSource.html
# BufferSourceNode WebAudioContext.createBufferSource() ## # 功能描述
创建一个BufferSourceNode实例，通过AudioBuffer对象来播放音频数据。
## 返回值
### BufferSourceNode
## 示例代码
```javascript
const audioCtx = wx.createWebAudioContext()

const loadAudio = (url) => {
  return new Promise((resolve) => {
    wx.request({
      url,
      responseType: 'arraybuffer',
      success: res => {
        console.log('res.data', res.data)
        audioCtx.decodeAudioData(res.data, buffer => {
          resolve(buffer)
        }, err => {
          console.error('decodeAudioData fail', err)
          reject()
        })
      },
      fail: res => {
        console.error('request fail', res)
        reject()
      }
    })
  })
}

const play = () => {
  loadAudio('xxx-test.mp3').then(buffer => {
    let source = audioCtx.createBufferSource()
    source.buffer = buffer
    source.connect(audioCtx.destination)
    source.start()
  }).catch(() => {
    console.log('fail')
  })
}

play()
```
