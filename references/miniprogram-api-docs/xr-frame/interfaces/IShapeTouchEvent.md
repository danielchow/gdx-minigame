> Source: https://developers.weixin.qq.com/miniprogram/dev/api/xr-frame/interfaces/IShapeTouchEvent.html

[xr-frame](./../) / [Exports](./../modules.html) / IShapeTouchEvent

## Interface: IShapeTouchEvent

`touch-shape`和`untouch-shape`事件的回调参数。

## # Hierarchy

-
**`IShapeTouchEvent`**

↳ [`IShapeDragEvent`](./IShapeDragEvent.html)


## # Table of contents

### # Properties

- [camera](./IShapeTouchEvent.html#camera)
 - [dir](./IShapeTouchEvent.html#dir)
 - [force](./IShapeTouchEvent.html#force)
 - [origin](./IShapeTouchEvent.html#origin)
 - [shape](./IShapeTouchEvent.html#shape)
 - [target](./IShapeTouchEvent.html#target)
 - [x](./IShapeTouchEvent.html#x)
 - [y](./IShapeTouchEvent.html#y)


## # Properties

### # camera

• **camera**: [`Camera`](./../classes/Camera.html)

渲染*被选中的[轮廓](./../classes/Shape.html)*的相机。

### # dir

• **dir**: [`number`, `number`, `number`]

从[camera](./IShapeTouchEvent.html#camera)投射出的射线的单位向量。

### # force

• **force**: `number`

**`unimplemented`**

### # origin

• **origin**: [`number`, `number`, `number`]

[camera](./IShapeTouchEvent.html#camera)在三维场景中的位置。

### # shape

• **shape**: [`Shape`](./../classes/Shape.html)<`any`>

被选中的[轮廓](./../classes/Shape.html)。

### # target

• **target**: [`Element`](./../classes/Element.html)

*被选中的[轮廓](./../classes/Shape.html)*所在的元素。

### # x

• **x**: `number`

点击位置在二维canvas中的x坐标。

### # y

• **y**: `number`

点击位置在二维canvas中的y坐标。
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)