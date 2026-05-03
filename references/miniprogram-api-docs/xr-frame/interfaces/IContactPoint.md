> Source: https://developers.weixin.qq.com/miniprogram/dev/api/xr-frame/interfaces/IContactPoint.html

[xr-frame](./../) / [Exports](./../modules.html) / IContactPoint

## Interface: IContactPoint

物理事件返回的[碰撞信息](./ICollideEvent.html)中的碰撞点。

## # Table of contents

### # Properties

- [normal](./IContactPoint.html#normal)
 - [otherShape](./IContactPoint.html#otherShape)
 - [point](./IContactPoint.html#point)
 - [separation](./IContactPoint.html#separation)
 - [thisShape](./IContactPoint.html#thisShape)


## # Properties

### # normal

• `Readonly` **normal**: `Vector3_READONLY`

碰撞平面的法线。

### # otherShape

• `Readonly` **otherShape**: [`Shape`](./../classes/Shape.html)<`any`>

另一个轮廓。

### # point

• `Readonly` **point**: `Vector3_READONLY`

碰撞点的位置。

### # separation

• `Readonly` **separation**: `number`

在该碰撞点处，两个物体的距离。

不一定是0或小于0，因为只要两个物体的距离小于{@link Collider.contactOffset}之和，就会判定为碰撞。

### # thisShape

• `Readonly` **thisShape**: [`Shape`](./../classes/Shape.html)<`any`>

接收碰撞事件的轮廓。
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)