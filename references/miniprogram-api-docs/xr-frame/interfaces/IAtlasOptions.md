> Source: https://developers.weixin.qq.com/miniprogram/dev/api/xr-frame/interfaces/IAtlasOptions.html

[xr-frame](./../) / [Exports](./../modules.html) / IAtlasOptions

## Interface: IAtlasOptions

`Atlas`的初始化参数类型。

## # Table of contents

### # Properties

- [frames](./IAtlasOptions.html#frames)
 - [image](./IAtlasOptions.html#image)
 - [meta](./IAtlasOptions.html#meta)
 - [texture](./IAtlasOptions.html#texture)


## # Properties

### # frames

• **frames**: `Object`

帧定义，若不指定`uv`则会自动按比例计算。

#### # Index signature

▪ [key: `string`]: { `frame`: { `h`: `number` ; `w`: `number` ; `x`: `number` ; `y`: `number`  }  }

### # image

• `Optional` **image**: [`IImage`](./IImage.html)

图片。

### # meta

• **meta**: `Object`

原信息，主要定义图片尺寸。

#### # Type declaration
 | Name | Type |
| --- | --- |
| `size` | { `h`: `number` ; `w`: `number`  } |
| `size.h` | `number` |
| `size.w` | `number` |
### # texture

• `Optional` **texture**: `default`

也可以直接传入一张纹理。
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)