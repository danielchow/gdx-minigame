> Source: https://developers.weixin.qq.com/miniprogram/dev/api/xr-frame/interfaces/IARTrackerRawData.html

[xr-frame](./../) / [Exports](./../modules.html) / IARTrackerRawData

## Interface: IARTrackerRawData

`Face`/`Body`/`Hand`模式下，[ARTracker](./../classes/ARTracker.html)存储的原始数据类型。

## # Table of contents

### # Properties

- [angle](./IARTrackerRawData.html#angle)
 - [confidence](./IARTrackerRawData.html#confidence)
 - [gesture](./IARTrackerRawData.html#gesture)
 - [origin](./IARTrackerRawData.html#origin)
 - [points](./IARTrackerRawData.html#points)
 - [points3d](./IARTrackerRawData.html#points3d)
 - [score](./IARTrackerRawData.html#score)
 - [size](./IARTrackerRawData.html#size)


## # Properties

### # angle

• `Optional` **angle**: `Object`

在`Face`模式下，人脸旋转角度。

#### # Type declaration
 | Name | Type |
| --- | --- |
| `pitch` | `number` |
| `roll` | `number` |
| `yaw` | `number` |
| `z_score` | `number` |
### # confidence

• **confidence**: `number`[]

关键点置信度。

### # gesture

• `Optional` **gesture**: `number`

在`Hand`模式下，手势分类，正常`0~18`，无效为`-1`。

### # origin

• **origin**: `Object`

原点，屏幕空间。

#### # Type declaration
 | Name | Type |
| --- | --- |
| `x` | `number` |
| `y` | `number` |
### # points

• **points**: { `x`: `number` ; `y`: `number`  }[]

关键点，屏幕空间。

### # points3d

• **points3d**: { `x`: `number` ; `y`: `number` ; `z`: `number`  }[]

支持3D时，3D关键点，世界空间。

### # score

• **score**: `number`

置信度。

### # size

• **size**: `Object`

尺寸，屏幕空间。

#### # Type declaration
 | Name | Type |
| --- | --- |
| `height` | `number` |
| `width` | `number` | The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)