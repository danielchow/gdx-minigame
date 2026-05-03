> Source: https://developers.weixin.qq.com/miniprogram/dev/api/xr-frame/interfaces/IParticleData.html

[xr-frame](./../) / [Exports](./../modules.html) / IParticleData

## Interface: IParticleData

[Particle](./../classes/Particle.html)组件数据接口。

## # Table of contents

### # Properties

- [angle](./IParticleData.html#angle)
 - [angularSpeed](./IParticleData.html#angularSpeed)
 - [atlas](./IParticleData.html#atlas)
 - [atlasFrames](./IParticleData.html#atlasFrames)
 - [atlasLoop](./IParticleData.html#atlasLoop)
 - [atlasRandom](./IParticleData.html#atlasRandom)
 - [atlasSpeed](./IParticleData.html#atlasSpeed)
 - [burstCount](./IParticleData.html#burstCount)
 - [burstCycle](./IParticleData.html#burstCycle)
 - [burstInterval](./IParticleData.html#burstInterval)
 - [burstTime](./IParticleData.html#burstTime)
 - [capacity](./IParticleData.html#capacity)
 - [colorChange](./IParticleData.html#colorChange)
 - [delay](./IParticleData.html#delay)
 - [emitRate](./IParticleData.html#emitRate)
 - [emitterProps](./IParticleData.html#emitterProps)
 - [emitterType](./IParticleData.html#emitterType)
 - [endColor](./IParticleData.html#endColor)
 - [gravity](./IParticleData.html#gravity)
 - [lifeTime](./IParticleData.html#lifeTime)
 - [mesh](./IParticleData.html#mesh)
 - [neverCull](./IParticleData.html#neverCull)
 - [prewarmCycles](./IParticleData.html#prewarmCycles)
 - [renderMode](./IParticleData.html#renderMode)
 - [scaleX](./IParticleData.html#scaleX)
 - [scaleY](./IParticleData.html#scaleY)
 - [size](./IParticleData.html#size)
 - [sizeChange](./IParticleData.html#sizeChange)
 - [speed](./IParticleData.html#speed)
 - [speedChange](./IParticleData.html#speedChange)
 - [speedDampen](./IParticleData.html#speedDampen)
 - [startColor](./IParticleData.html#startColor)
 - [startColor2](./IParticleData.html#startColor2)
 - [states](./IParticleData.html#states)
 - [stopDuration](./IParticleData.html#stopDuration)
 - [texture](./IParticleData.html#texture)
 - [uniforms](./IParticleData.html#uniforms)


## # Properties

### # angle

• `Optional` **angle**: `number`[]

初始角度。

### # angularSpeed

• `Optional` **angularSpeed**: `number`[]

角速度。

### # atlas

• `Optional` **atlas**: [`Atlas`](./../classes/Atlas.html)

动画图集信息。

### # atlasFrames

• `Optional` **atlasFrames**: `string`[]

指定图集帧名。

### # atlasLoop

• `Optional` **atlasLoop**: `boolean`

是否循环播放图集。

### # atlasRandom

• `Optional` **atlasRandom**: `boolean`

是否随机播放图集。

### # atlasSpeed

• `Optional` **atlasSpeed**: `number`

图集切换速度。

### # burstCount

• `Optional` **burstCount**: `number`

### # burstCycle

• `Optional` **burstCycle**: `number`

### # burstInterval

• `Optional` **burstInterval**: `number`

### # burstTime

• `Optional` **burstTime**: `number`

### # capacity

• `Optional` **capacity**: `number`

最大粒子数目。

### # colorChange

• `Optional` **colorChange**: [`string`, `string`][]

### # delay

• `Optional` **delay**: `number`

粒子系统启动延时秒数。

### # emitRate

• `Optional` **emitRate**: `number`

每秒粒子发射数。

### # emitterProps

• `Optional` **emitterProps**: [`string`, `string`][]

发射器属性配置。

### # emitterType

• `Optional` **emitterType**: `string`

发射器类型。

### # endColor

• `Optional` **endColor**: `number`[]

粒子结束时颜色。

### # gravity

• `Optional` **gravity**: `number`

y轴方向上的每秒位移。

### # lifeTime

• `Optional` **lifeTime**: `number`[]

生命周期时长。

### # mesh

• `Optional` **mesh**: [`Geometry`](./../classes/Geometry.html)

网格信息。

### # neverCull

• `Optional` **neverCull**: `boolean`

### # prewarmCycles

• `Optional` **prewarmCycles**: `number`

粒子预渲染周期数。

### # renderMode

• `Optional` **renderMode**: `string`

渲染模式。

### # scaleX

• `Optional` **scaleX**: `number`[]

粒子在x轴方向上的大小尺度。

### # scaleY

• `Optional` **scaleY**: `number`[]

粒子在y轴方向上的大小尺度。

### # size

• `Optional` **size**: `number`[]

初始大小。

### # sizeChange

• `Optional` **sizeChange**: [`string`, `string`][]

### # speed

• `Optional` **speed**: `number`[]

速度。

### # speedChange

• `Optional` **speedChange**: [`string`, `string`][]

### # speedDampen

• `Optional` **speedDampen**: `number`

速度阻尼系数。

### # startColor

• `Optional` **startColor**: `number`[]

粒子初始颜色左区间。

### # startColor2

• `Optional` **startColor2**: `number`[]

粒子初始颜色右区间。

### # states

• `Optional` **states**: [`string`, `string`][]

### # stopDuration

• `Optional` **stopDuration**: `number`

粒子系统生命周期时长。

### # texture

• `Optional` **texture**: `default`

纹理信息。

### # uniforms

• `Optional` **uniforms**: [`string`, `string`][]
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)