> Source: https://developers.weixin.qq.com/miniprogram/dev/api/xr-frame/interfaces/IKeyframeAnimationInfo.html

[xr-frame](./../) / [Exports](./../modules.html) / IKeyframeAnimationInfo

## Interface: IKeyframeAnimationInfo

`Keyframe`动画数据的动画部分。

## # Table of contents

### # Properties

- [delay](./IKeyframeAnimationInfo.html#delay)
 - [direction](./IKeyframeAnimationInfo.html#direction)
 - [duration](./IKeyframeAnimationInfo.html#duration)
 - [ease](./IKeyframeAnimationInfo.html#ease)
 - [easeParams](./IKeyframeAnimationInfo.html#easeParams)
 - [keyframe](./IKeyframeAnimationInfo.html#keyframe)
 - [loop](./IKeyframeAnimationInfo.html#loop)


## # Properties

### # delay

• `Optional` **delay**: `number`

播放延迟。

### # direction

• `Optional` **direction**: [`TDirection`](./../modules.html#TDirection)

播放方向。

### # duration

• **duration**: `number`

动画长度(s)。

### # ease

• **ease**: `string`

动画插值方式，详见[noneParamsEaseFuncs](./../modules.html#noneParamsEaseFuncs)和[useParamsEaseFuncs](./../modules.html#useParamsEaseFuncs)。

### # easeParams

• `Optional` **easeParams**: `number`[]

如果是可以接受参数的插值方式，指定参数。

### # keyframe

• **keyframe**: `string`

指定动画使用的Keyframe。

### # loop

• `Optional` **loop**: `number`

循环次数，`0`为不循环，`-1`为永远循环。
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)