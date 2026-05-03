> Source: https://developers.weixin.qq.com/miniprogram/dev/api/xr-frame/interfaces/IShapeDragEvent.html

[xr-frame](./../) / [Exports](./../modules.html) / IShapeDragEvent

## Interface: IShapeDragEvent

`drag-shape`事件的回调参数。

## # Hierarchy

-
[`IShapeTouchEvent`](./IShapeTouchEvent.html)

↳ **`IShapeDragEvent`**


## # Table of contents

### # Properties

- [camera](./IShapeDragEvent.html#camera)
 - [deltaX](./IShapeDragEvent.html#deltaX)
 - [deltaY](./IShapeDragEvent.html#deltaY)
 - [dir](./IShapeDragEvent.html#dir)
 - [force](./IShapeDragEvent.html#force)
 - [origin](./IShapeDragEvent.html#origin)
 - [shape](./IShapeDragEvent.html#shape)
 - [target](./IShapeDragEvent.html#target)
 - [x](./IShapeDragEvent.html#x)
 - [y](./IShapeDragEvent.html#y)


## # Properties

### # camera

• **camera**: [`Camera`](./../classes/Camera.html)

渲染*被选中的[轮廓](./../classes/Shape.html)*的相机。

#### # Inherited from

[IShapeTouchEvent](./IShapeTouchEvent.html).[camera](./IShapeTouchEvent.html#camera)

### # deltaX

• **deltaX**: `number`

点击位置在二维canvas中的x坐标的变化量。

### # deltaY

• **deltaY**: `number`

点击位置在二维canvas中的y坐标的变化量。

### # dir

• **dir**: [`number`, `number`, `number`]

从[camera](./IShapeDragEvent.html#camera)投射出的射线的单位向量。

#### # Inherited from

[IShapeTouchEvent](./IShapeTouchEvent.html).[dir](./IShapeTouchEvent.html#dir)

### # force

• **force**: `number`

**`unimplemented`**

#### # Inherited from

[IShapeTouchEvent](./IShapeTouchEvent.html).[force](./IShapeTouchEvent.html#force)

### # origin

• **origin**: [`number`, `number`, `number`]

[camera](./IShapeDragEvent.html#camera)在三维场景中的位置。

#### # Inherited from

[IShapeTouchEvent](./IShapeTouchEvent.html).[origin](./IShapeTouchEvent.html#origin)

### # shape

• **shape**: [`Shape`](./../classes/Shape.html)<`any`>

被选中的[轮廓](./../classes/Shape.html)。

#### # Inherited from

[IShapeTouchEvent](./IShapeTouchEvent.html).[shape](./IShapeTouchEvent.html#shape)

### # target

• **target**: [`Element`](./../classes/Element.html)

*被选中的[轮廓](./../classes/Shape.html)*所在的元素。

#### # Inherited from

[IShapeTouchEvent](./IShapeTouchEvent.html).[target](./IShapeTouchEvent.html#target)

### # x

• **x**: `number`

点击位置在二维canvas中的x坐标。

#### # Inherited from

[IShapeTouchEvent](./IShapeTouchEvent.html).[x](./IShapeTouchEvent.html#x)

### # y

• **y**: `number`

点击位置在二维canvas中的y坐标。

#### # Inherited from

[IShapeTouchEvent](./IShapeTouchEvent.html).[y](./IShapeTouchEvent.html#y)
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)