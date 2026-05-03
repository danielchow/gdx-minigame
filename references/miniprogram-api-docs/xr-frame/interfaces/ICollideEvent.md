> Source: https://developers.weixin.qq.com/miniprogram/dev/api/xr-frame/interfaces/ICollideEvent.html

[xr-frame](./../) / [Exports](./../modules.html) / ICollideEvent

## Interface: ICollideEvent

物理碰撞事件（collide-begin等）的信息。

**`readonly`**

## # Table of contents

### # Properties

- [contacts](./ICollideEvent.html#contacts)
 - [impulse](./ICollideEvent.html#impulse)
 - [relativeVelocity](./ICollideEvent.html#relativeVelocity)
 - [shape](./ICollideEvent.html#shape)


## # Properties

### # contacts

• `Readonly` **contacts**: [`IContactPoint`](./IContactPoint.html)[]

本次碰撞的接触点。

### # impulse

• `Readonly` **impulse**: `Vector3_READONLY`

从碰撞到分离所用的冲量之和。

### # relativeVelocity

• `Readonly` **relativeVelocity**: `Vector3_READONLY`

两个刚体的相对线性碰撞速度。

### # shape

• `Readonly` **shape**: [`Shape`](./../classes/Shape.html)<`any`>

发生碰撞的另一个轮廓。
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)