> Source: https://developers.weixin.qq.com/miniprogram/dev/api/xr-frame/interfaces/IViewAction.html

[xr-frame](./../) / [Exports](./../modules.html) / IViewAction

## Interface: IViewAction

对一个View进行清屏的操作。

## # Table of contents

### # Properties

- [clearColor](./IViewAction.html#clearColor)
 - [clearDepth](./IViewAction.html#clearDepth)
 - [clearStencil](./IViewAction.html#clearStencil)
 - [colorAction](./IViewAction.html#colorAction)
 - [depthAction](./IViewAction.html#depthAction)
 - [stencilAction](./IViewAction.html#stencilAction)


## # Properties

### # clearColor

• `Optional` **clearColor**: [`number`, `number`, `number`, `number`]

用于清屏的颜色值。

**`default`** [0,0,0,0]

### # clearDepth

• `Optional` **clearDepth**: `number`

用于清屏的深度值。

**`default`** 1

### # clearStencil

• `Optional` **clearStencil**: `number`

用于清屏的模板值。

**`default`** 0

### # colorAction

• `Optional` **colorAction**: [`ELoadAction`](./../enums/ELoadAction.html)

颜色操作。

### # depthAction

• `Optional` **depthAction**: [`ELoadAction`](./../enums/ELoadAction.html)

深度操作。

### # stencilAction

• `Optional` **stencilAction**: [`ELoadAction`](./../enums/ELoadAction.html)

模板操作。
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)