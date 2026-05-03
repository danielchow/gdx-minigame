> Source: https://developers.weixin.qq.com/miniprogram/dev/api/ai/inference/Tensors.html

## Tensors

基础库 2.30.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

Tensors 是 key-value 形式的对象，对象的 key 会作为 input/output name，对象的 value 则是 Tensor。 Tensor 结构如下。

## # 属性

### # Tensor key

Tensor，每个 Tensor 包含 shape、data、type 字段。

```js
session.run({
  input1: {
    type: 'float32',
    data: new Float32Array(3 * 224 * 224).buffer,
    shape: [1, 3, 224, 224] // NCHW 顺序
  },
  input2: {
    type: 'uint8',
    data: new Uint8Array(224 * 224).buffer,
    shape: [1, 1, 224, 224]
  },
}).then(res => {
  console.log(res.output0)
  // output0 结构如下：
  // {
  //   type: 'uint8',
  //   data: new Uint8Array(224 * 224).buffer,
  //   shape: [1, 1, 224, 224]
  // }
})
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)