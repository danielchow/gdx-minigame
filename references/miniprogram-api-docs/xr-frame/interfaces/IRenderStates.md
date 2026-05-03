> Source: https://developers.weixin.qq.com/miniprogram/dev/api/xr-frame/interfaces/IRenderStates.html

[xr-frame](./../) / [Exports](./../modules.html) / IRenderStates

## Interface: IRenderStates

支持定制的渲染状态。

大部分状态会定制的开发者应该看名字就懂，就不详细说明了。

## # Table of contents

### # Properties

- [blendDst](./IRenderStates.html#blendDst)
 - [blendDstAlpha](./IRenderStates.html#blendDstAlpha)
 - [blendDstRGB](./IRenderStates.html#blendDstRGB)
 - [blendFunc](./IRenderStates.html#blendFunc)
 - [blendOn](./IRenderStates.html#blendOn)
 - [blendSrc](./IRenderStates.html#blendSrc)
 - [blendSrcAlpha](./IRenderStates.html#blendSrcAlpha)
 - [blendSrcRGB](./IRenderStates.html#blendSrcRGB)
 - [colorWrite](./IRenderStates.html#colorWrite)
 - [cullFace](./IRenderStates.html#cullFace)
 - [cullOn](./IRenderStates.html#cullOn)
 - [depthTestComp](./IRenderStates.html#depthTestComp)
 - [depthTestOn](./IRenderStates.html#depthTestOn)
 - [depthWrite](./IRenderStates.html#depthWrite)
 - [primitiveType](./IRenderStates.html#primitiveType)
 - [renderQueue](./IRenderStates.html#renderQueue)
 - [stencilComp](./IRenderStates.html#stencilComp)
 - [stencilFail](./IRenderStates.html#stencilFail)
 - [stencilPass](./IRenderStates.html#stencilPass)
 - [stencilReadMask](./IRenderStates.html#stencilReadMask)
 - [stencilRef](./IRenderStates.html#stencilRef)
 - [stencilTestOn](./IRenderStates.html#stencilTestOn)
 - [stencilWriteMask](./IRenderStates.html#stencilWriteMask)
 - [stencilZFail](./IRenderStates.html#stencilZFail)


## # Properties

### # blendDst

• `Optional` **blendDst**: [`EBlendFactor`](./../enums/EBlendFactor.html)

不要使用，使用`blendDstRGB`。

### # blendDstAlpha

• `Optional` **blendDstAlpha**: [`EBlendFactor`](./../enums/EBlendFactor.html)

### # blendDstRGB

• `Optional` **blendDstRGB**: [`EBlendFactor`](./../enums/EBlendFactor.html)

### # blendFunc

• `Optional` **blendFunc**: [`EBlendEquation`](./../enums/EBlendEquation.html)

### # blendOn

• `Optional` **blendOn**: `boolean`

### # blendSrc

• `Optional` **blendSrc**: [`EBlendFactor`](./../enums/EBlendFactor.html)

不要使用，使用`blendSrcRGB`。

### # blendSrcAlpha

• `Optional` **blendSrcAlpha**: [`EBlendFactor`](./../enums/EBlendFactor.html)

### # blendSrcRGB

• `Optional` **blendSrcRGB**: [`EBlendFactor`](./../enums/EBlendFactor.html)

### # colorWrite

• `Optional` **colorWrite**: `number`

在基础库版本`v2.31.1`以上支持。

### # cullFace

• `Optional` **cullFace**: [`ECullMode`](./../enums/ECullMode.html)

### # cullOn

• `Optional` **cullOn**: `boolean`

### # depthTestComp

• `Optional` **depthTestComp**: [`ECompareFunc`](./../enums/ECompareFunc.html)

### # depthTestOn

• `Optional` **depthTestOn**: `boolean`

### # depthWrite

• `Optional` **depthWrite**: `boolean`

### # primitiveType

• `Optional` **primitiveType**: [`EPrimitiveType`](./../enums/EPrimitiveType.html)

### # renderQueue

• `Optional` **renderQueue**: `number`

渲染队列，大于等于`2500`为透明物体，否则为非透明物体。

### # stencilComp

• `Optional` **stencilComp**: [`ECompareFunc`](./../enums/ECompareFunc.html)

### # stencilFail

• `Optional` **stencilFail**: [`EStencilOp`](./../enums/EStencilOp.html)

### # stencilPass

• `Optional` **stencilPass**: [`EStencilOp`](./../enums/EStencilOp.html)

### # stencilReadMask

• `Optional` **stencilReadMask**: `number`

### # stencilRef

• `Optional` **stencilRef**: `number`

### # stencilTestOn

• `Optional` **stencilTestOn**: `boolean`

### # stencilWriteMask

• `Optional` **stencilWriteMask**: `number`

### # stencilZFail

• `Optional` **stencilZFail**: [`EStencilOp`](./../enums/EStencilOp.html)
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)