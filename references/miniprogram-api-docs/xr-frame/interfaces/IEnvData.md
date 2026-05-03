> Source: https://developers.weixin.qq.com/miniprogram/dev/api/xr-frame/interfaces/IEnvData.html

[xr-frame](./../) / [Exports](./../modules.html) / IEnvData

## Interface: IEnvData

[Env](./../classes/Env.html)组件数据接口。

## # Table of contents

### # Properties

- [diffuseExp](./IEnvData.html#diffuseExp)
 - [envData](./IEnvData.html#envData)
 - [isSky2D](./IEnvData.html#isSky2D)
 - [rotation](./IEnvData.html#rotation)
 - [skyMap](./IEnvData.html#skyMap)
 - [specularExp](./IEnvData.html#specularExp)


## # Properties

### # diffuseExp

• **diffuseExp**: `number`

漫反射部分曝光。
`xml`中的数据类型为`number`，默认为`1`。

### # envData

• `Optional` **envData**: [`EnvData`](./../classes/EnvData.html)

要使用的环境数据资源。
`xml`中的数据类型为`env-data`资源。

### # isSky2D

• `Optional` **isSky2D**: `boolean`

是否用2D模式渲染天空盒，此时必须为`skyMap`必须**不**为`CubeTexture`。

### # rotation

• **rotation**: `number`

环境旋转角度。
`xml`中的数据类型为`number`，默认为`0`。

### # skyMap

• `Optional` **skyMap**: `default` | [`ITextureWrapper`](./ITextureWrapper.html)

可以用于覆盖`envData`中的`skybox`。
`xml`中的数据类型为`texture`资源。

### # specularExp

• **specularExp**: `number`

镜面反射部分曝光。
`xml`中的数据类型为`number`，默认为`1`。
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)