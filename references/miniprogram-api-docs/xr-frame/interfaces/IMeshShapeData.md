> Source: https://developers.weixin.qq.com/miniprogram/dev/api/xr-frame/interfaces/IMeshShapeData.html

[xr-frame](./../) / [Exports](./../modules.html) / IMeshShapeData

## Interface: IMeshShapeData

**`see`** [MeshShape](./../classes/MeshShape.html)

## # Hierarchy

-
[`IShapeData`](./IShapeData.html)

↳ **`IMeshShapeData`**


## # Table of contents

### # Properties

- [autoFit](./IMeshShapeData.html#autoFit)
 - [center](./IMeshShapeData.html#center)
 - [convex](./IMeshShapeData.html#convex)
 - [disabled](./IMeshShapeData.html#disabled)


## # Properties

### # autoFit

• `Optional` **autoFit**: `boolean`

轮廓是否自动贴合[Mesh组件](./../classes/Mesh.html)或[GLTF组件](./../classes/GLTF.html)的大小。
如果当前元素下不存在Mesh组件和GLTF组件则不生效。

[MeshShape](./../classes/MeshShape.html)永远会开启这项。

**`default`** false

#### # Inherited from

[IShapeData](./IShapeData.html).[autoFit](./IShapeData.html#autoFit)

### # center

• `Optional` **center**: [`number`, `number`, `number`]

轮廓中心相对元素[Transform](./../classes/Transform.html)中心的偏移量。

**`default`** [0, 0, 0]

#### # Inherited from

[IShapeData](./IShapeData.html).[center](./IShapeData.html#center)

### # convex

• `Optional` **convex**: `boolean`

是否使用凸多边形来包围Mesh。
*如果元素有`shape-interact`属性，则会强制开启。*

**`default`** false

### # disabled

• `Optional` **disabled**: `boolean`

是否禁用shape。

**`default`** false

#### # Inherited from

[IShapeData](./IShapeData.html).[disabled](./IShapeData.html#disabled)
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)