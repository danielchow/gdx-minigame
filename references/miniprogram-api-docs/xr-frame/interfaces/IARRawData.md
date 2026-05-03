> Source: https://developers.weixin.qq.com/miniprogram/dev/api/xr-frame/interfaces/IARRawData.html

[xr-frame](./../) / [Exports](./../modules.html) / IARRawData

## Interface: IARRawData

AR追踪原始数据。

## # Table of contents

### # Properties

- [height](./IARRawData.html#height)
 - [intrinsics](./IARRawData.html#intrinsics)
 - [timestamp](./IARRawData.html#timestamp)
 - [uvBuffer](./IARRawData.html#uvBuffer)
 - [viewMatrix](./IARRawData.html#viewMatrix)
 - [width](./IARRawData.html#width)
 - [yBuffer](./IARRawData.html#yBuffer)


## # Properties

### # height

• **height**: `number`

当前相机帧画面高度。

### # intrinsics

• **intrinsics**: `Float32Array`

当前相机帧内参矩阵。

### # timestamp

• **timestamp**: `number`

该帧生成时间，单位是纳秒(ns)。
在版本`v2.30.1`之后支持。

### # uvBuffer

• **uvBuffer**: `ArrayBuffer`

当前相机帧画面`uv`通道，yuv420。

### # viewMatrix

• **viewMatrix**: `Float32Array`

当前相机帧视图矩阵。

### # width

• **width**: `number`

当前相机帧画面宽度。

### # yBuffer

• **yBuffer**: `ArrayBuffer`

当前相机帧画面`y`通道，yuv420。
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)