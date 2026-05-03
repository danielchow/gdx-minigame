> Source: https://developers.weixin.qq.com/miniprogram/dev/api/xr-frame/interfaces/IGLTFData.html

[xr-frame](./../) / [Exports](./../modules.html) / IGLTFData

## Interface: IGLTFData

**`see`** [GLTF](./../classes/GLTF.html)

## # Table of contents

### # Properties

- [castShadow](./IGLTFData.html#castShadow)
 - [model](./IGLTFData.html#model)
 - [neverCull](./IGLTFData.html#neverCull)
 - [receiveShadow](./IGLTFData.html#receiveShadow)
 - [states](./IGLTFData.html#states)


## # Properties

### # castShadow

• `Optional` **castShadow**: `boolean`

是否投射阴影，默认false。

### # model

• **model**: [`GLTFModel`](./../classes/GLTFModel.html)

已加载完毕的GLTF模型。

### # neverCull

• `Optional` **neverCull**: `boolean`

是否不参与剔除，默认false(即参与剔除)。

### # receiveShadow

• `Optional` **receiveShadow**: `boolean`

是否接受阴影，默认false。

### # states

• `Optional` **states**: [`string`, `string`][]

修改GLTF的默认renderStates。
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)