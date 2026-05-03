> Source: https://developers.weixin.qq.com/miniprogram/dev/api/xr-frame/interfaces/IShapeData.html

[xr-frame](./../) / [Exports](./../modules.html) / IShapeData

## Interface: IShapeData

## # Hierarchy

-
**`IShapeData`**

↳ [`ISphereShapeData`](./ISphereShapeData.html)

↳ [`IMeshShapeData`](./IMeshShapeData.html)

↳ [`ICapsuleShapeData`](./ICapsuleShapeData.html)

↳ [`ICubeShapeData`](./ICubeShapeData.html)


## # Table of contents

### # Properties

- [autoFit](./IShapeData.html#autoFit)
 - [center](./IShapeData.html#center)
 - [disabled](./IShapeData.html#disabled)


## # Properties

### # autoFit

• `Optional` **autoFit**: `boolean`

轮廓是否自动贴合[Mesh组件](./../classes/Mesh.html)或[GLTF组件](./../classes/GLTF.html)的大小。
如果当前元素下不存在Mesh组件和GLTF组件则不生效。

[MeshShape](./../classes/MeshShape.html)永远会开启这项。

**`default`** false

### # center

• `Optional` **center**: [`number`, `number`, `number`]

轮廓中心相对元素[Transform](./../classes/Transform.html)中心的偏移量。

**`default`** [0, 0, 0]

### # disabled

• `Optional` **disabled**: `boolean`

是否禁用shape。

**`default`** false
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)