> Source: https://developers.weixin.qq.com/miniprogram/dev/api/xr-frame/interfaces/IMeshData.html

[xr-frame](./../) / [Exports](./../modules.html) / IMeshData

## Interface: IMeshData

`Mesh`数据接口。

## # Table of contents

### # Properties

- [castShadow](./IMeshData.html#castShadow)
 - [envData](./IMeshData.html#envData)
 - [geometry](./IMeshData.html#geometry)
 - [material](./IMeshData.html#material)
 - [neverCull](./IMeshData.html#neverCull)
 - [receiveShadow](./IMeshData.html#receiveShadow)
 - [states](./IMeshData.html#states)
 - [uniforms](./IMeshData.html#uniforms)


## # Properties

### # castShadow

• `Optional` **castShadow**: `boolean`

在主光源产生阴影开启阴影时，是否要产生阴影。
`xml`中的数据类型为`boolean`，默认为`false`。

### # envData

• `Optional` **envData**: [`EnvData`](./../classes/EnvData.html)

用于覆盖`material`中的，全局的、材质维度的环境数据。

- `xml`中同[IAssetMaterialData.envData](./IAssetMaterialData.html#envData)。


### # geometry

• **geometry**: [`Geometry`](./../classes/Geometry.html)

渲染使用的几何数据。
`xml`中的数据类型为`geometry`资源。

### # material

• `Optional` **material**: [`Material`](./../classes/Material.html)

渲染使用的材质数据。
`xml`中的数据类型为`material`资源。

### # neverCull

• `Optional` **neverCull**: `boolean`

是否强制不被剔除。
`xml`中的数据类型为`boolean`，默认为`false`。

### # receiveShadow

• `Optional` **receiveShadow**: `boolean`

在主光源产生阴影开启阴影时，是否要接受阴影。
`xml`中的数据类型为`boolean`，默认为`false`。

### # states

• `Optional` **states**: [`string`, `string`][]

覆盖`material`中的默认`states`，如果覆盖了，则会先创建一个材质副本。
`xml`中同[IAssetMaterialData.states](./IAssetMaterialData.html#states)。

### # uniforms

• `Optional` **uniforms**: [`string`, `string`][]

覆盖`material`中的默认`uniforms`，如果覆盖了，则会先创建一个材质副本。
`xml`中同[IAssetMaterialData.uniforms](./IAssetMaterialData.html#uniforms)。
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)