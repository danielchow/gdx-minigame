> Source: https://developers.weixin.qq.com/miniprogram/dev/api/xr-frame/interfaces/ICubeTextureLoaderOptions.html

[xr-frame](./../) / [Exports](./../modules.html) / ICubeTextureLoaderOptions

## Interface: ICubeTextureLoaderOptions

[CubeTextureLoader](./../classes/CubeTextureLoader.html)可接受的自定义参数`schema`。

## # Table of contents

### # Properties

- [anisoLevel](./ICubeTextureLoaderOptions.html#anisoLevel)
 - [faces](./ICubeTextureLoaderOptions.html#faces)
 - [generateMipmaps](./ICubeTextureLoaderOptions.html#generateMipmaps)
 - [magFilter](./ICubeTextureLoaderOptions.html#magFilter)
 - [minFilter](./ICubeTextureLoaderOptions.html#minFilter)
 - [wrapU](./ICubeTextureLoaderOptions.html#wrapU)
 - [wrapV](./ICubeTextureLoaderOptions.html#wrapV)
 - [wrapW](./ICubeTextureLoaderOptions.html#wrapW)


## # Properties

### # anisoLevel

• **anisoLevel**: `number`

各向异性系数。

**`default`** 1

### # faces

• **faces**: `string`[]

顺序为 left right top bottom front back。

### # generateMipmaps

• `Optional` **generateMipmaps**: `boolean`

是否要生成mipmaps。

**`default`** false

### # magFilter

• `Optional` **magFilter**: `number`

magFilter，值为数字，见[EFilterMode](./../enums/EFilterMode.html)。
默认值依据纹理是否POT而定。

### # minFilter

• `Optional` **minFilter**: `number`

minFilter，值为数字，见[EFilterMode](./../enums/EFilterMode.html)。
默认值依据纹理是否POT而定。

### # wrapU

• `Optional` **wrapU**: `number`

wrapU，值为数字，见[EWrapMode](./../enums/EWrapMode.html)。

**`default`** 2

### # wrapV

• `Optional` **wrapV**: `number`

wrapV，值为数字，见[EWrapMode](./../enums/EWrapMode.html)。

**`default`** 2

### # wrapW

• `Optional` **wrapW**: `number`

wrapW，值为数字，见[EWrapMode](./../enums/EWrapMode.html)。

**`default`** 2
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)