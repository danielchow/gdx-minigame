> Source: https://developers.weixin.qq.com/miniprogram/dev/api/xr-frame/interfaces/ICubeShapeData.html

[xr-frame](./../) / [Exports](./../modules.html) / ICubeShapeData

## Interface: ICubeShapeData

**`see`** [CubeShape](./../classes/CubeShape.html)

## # Hierarchy

-
[`IShapeData`](./IShapeData.html)

↳ **`ICubeShapeData`**


## # Table of contents

### # Properties

- [autoFit](./ICubeShapeData.html#autoFit)
 - [center](./ICubeShapeData.html#center)
 - [disabled](./ICubeShapeData.html#disabled)
 - [size](./ICubeShapeData.html#size)


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

### # disabled

• `Optional` **disabled**: `boolean`

是否禁用shape。

**`default`** false

#### # Inherited from

[IShapeData](./IShapeData.html).[disabled](./IShapeData.html#disabled)

### # size

• `Optional` **size**: [`number`, `number`, `number`]

长方体沿x,y,z轴的长度。

**`default`** [1, 1, 1]
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)