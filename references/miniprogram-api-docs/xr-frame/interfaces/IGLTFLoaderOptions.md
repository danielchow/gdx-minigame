> Source: https://developers.weixin.qq.com/miniprogram/dev/api/xr-frame/interfaces/IGLTFLoaderOptions.html

[xr-frame](./../) / [Exports](./../modules.html) / IGLTFLoaderOptions

## Interface: IGLTFLoaderOptions

## # Table of contents

### # Properties

- [ignoreError](./IGLTFLoaderOptions.html#ignoreError)
 - [preserveRaw](./IGLTFLoaderOptions.html#preserveRaw)


## # Properties

### # ignoreError

• **ignoreError**: `number`[]

*(基础库2.31.1及之后)*
可以忽略xr-frame对GLTF模型的某一些限制，来强行渲染有问题的GLTF模型。
ErrorCode会在渲染模型失败后，由console报出。
填写-1则忽略所有限制。

### # preserveRaw

• **preserveRaw**: `boolean`

*(基础库2.32.1及之后)*
开启了之后会在GLTFModel中保留原始json。
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)