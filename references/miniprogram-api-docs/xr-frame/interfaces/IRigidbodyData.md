> Source: https://developers.weixin.qq.com/miniprogram/dev/api/xr-frame/interfaces/IRigidbodyData.html

[xr-frame](./../) / [Exports](./../modules.html) / IRigidbodyData

## Interface: IRigidbodyData

## # Table of contents

### # Properties

- [constraintsMask](./IRigidbodyData.html#constraintsMask)
 - [disabled](./IRigidbodyData.html#disabled)
 - [kinematic](./IRigidbodyData.html#kinematic)
 - [mass](./IRigidbodyData.html#mass)
 - [useGravity](./IRigidbodyData.html#useGravity)


## # Properties

### # constraintsMask

• `Optional` **constraintsMask**: `number`

限制刚体在某个轴上的位移和旋转。
具体值参考{@link RigidbodyConstraints}

### # disabled

• `Optional` **disabled**: `boolean`

是否禁用刚体。

**`default`** false

### # kinematic

• `Optional` **kinematic**: `boolean`

是否为*运动学(Kinematic)* 刚体。
设置为*运动学*刚体后，除非手动调用[movePosition](./../classes/Rigidbody.html#movePosition)，否则物体不会在*物理模拟*阶段发生位移或旋转。可以理解为，刚体的行为完全在用户的控制之下。

**`default`** false

### # mass

• `Optional` **mass**: `number`

物体的质量。

**`limit`** mass > 0

**`default`** 1

### # useGravity

• `Optional` **useGravity**: `boolean`

刚体是否受重力影响。

**`default`** true
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)