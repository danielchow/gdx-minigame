> Source: https://developers.weixin.qq.com/miniprogram/dev/api/xr-frame/interfaces/ITextureLoaderOptions.html

[xr-frame](./../) / [Exports](./../modules.html) / ITextureLoaderOptions

## Interface: ITextureLoaderOptions

[TextureLoader](./../classes/TextureLoader.html)可接受的自定义参数`schema`。

## # Table of contents

### # Properties

- [anisoLevel](./ITextureLoaderOptions.html#anisoLevel)
 - [generateMipmaps](./ITextureLoaderOptions.html#generateMipmaps)
 - [magFilter](./ITextureLoaderOptions.html#magFilter)
 - [minFilter](./ITextureLoaderOptions.html#minFilter)
 - [wrapU](./ITextureLoaderOptions.html#wrapU)
 - [wrapV](./ITextureLoaderOptions.html#wrapV)


## # Properties

### # anisoLevel

• `Optional` **anisoLevel**: `number`

各向异性系数。

**`default`** 1

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
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)