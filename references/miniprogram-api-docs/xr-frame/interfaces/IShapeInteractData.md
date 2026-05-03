> Source: https://developers.weixin.qq.com/miniprogram/dev/api/xr-frame/interfaces/IShapeInteractData.html

[xr-frame](./../) / [Exports](./../modules.html) / IShapeInteractData

## Interface: IShapeInteractData

## # Table of contents

### # Properties

- [bounciness](./IShapeInteractData.html#bounciness)
 - [collide](./IShapeInteractData.html#collide)
 - [disabled](./IShapeInteractData.html#disabled)
 - [dynamicFriction](./IShapeInteractData.html#dynamicFriction)
 - [staticFriction](./IShapeInteractData.html#staticFriction)


## # Properties

### # bounciness

• `Optional` **bounciness**: `number`

弹性系数，决定碰撞时的能量损失比例。

弹性系数 = 1时，碰撞无能量损失。

**`limit`** 0 <= bounciness <= 1

**`default`** 0

### # collide

• `Optional` **collide**: `boolean`

是否能与其他Shape发生物理碰撞。

**`default`** false

### # disabled

• `Optional` **disabled**: `boolean`

是否禁用Shape间交互。

**`default`** false

### # dynamicFriction

• `Optional` **dynamicFriction**: `number`

动摩擦系数。

**`limit`** 0 <= dynamicFriction <= 1

**`default`** 0.6

### # staticFriction

• `Optional` **staticFriction**: `number`

静摩擦系数

**`limit`** 0 <= staticFriction <= 1

**`default`** 0.6
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)