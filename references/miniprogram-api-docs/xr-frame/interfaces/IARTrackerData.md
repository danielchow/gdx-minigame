> Source: https://developers.weixin.qq.com/miniprogram/dev/api/xr-frame/interfaces/IARTrackerData.html

[xr-frame](./../) / [Exports](./../modules.html) / IARTrackerData

## Interface: IARTrackerData

[ARTracker](./../classes/ARTracker.html)组件数据接口。

## # Table of contents

### # Properties

- [autoSync](./IARTrackerData.html#autoSync)
 - [image](./IARTrackerData.html#image)
 - [mode](./IARTrackerData.html#mode)
 - [src](./IARTrackerData.html#src)


## # Properties

### # autoSync

• `Optional` **autoSync**: `number`[]

在`Face`模式下，给定一个**特征点索引**列表，详见官网对应文档。
系统会自动同步位置和缩放到`ARTracker`下对应的顺序的子节点。
`-1`代表不同步位置，只同步缩放。

### # image

• `Optional` **image**: [`IImage`](./IImage.html)

要追踪的图片资源，优先使用。
`xml`中数据为`image`类型。

### # mode

• **mode**: [`TTrackMode`](./../modules.html#TTrackMode)

跟踪模式，必须在[ARSystem](./../classes/ARSystem.html)已开启的模式列表中。
`xml`中数据为`string`类型。

### # src

• `Optional` **src**: `string`

要追踪的图片地址，如果`image`没有定义，则使用这个。
`xml`中数据为`string`类型。
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)