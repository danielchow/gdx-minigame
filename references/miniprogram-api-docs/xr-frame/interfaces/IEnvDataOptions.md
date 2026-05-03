> Source: https://developers.weixin.qq.com/miniprogram/dev/api/xr-frame/interfaces/IEnvDataOptions.html

[xr-frame](./../) / [Exports](./../modules.html) / IEnvDataOptions

## Interface: IEnvDataOptions

`EnvData`的参数接口。

## # Table of contents

### # Properties

- [diffuse](./IEnvDataOptions.html#diffuse)
 - [skybox](./IEnvDataOptions.html#skybox)
 - [specular](./IEnvDataOptions.html#specular)


## # Properties

### # diffuse

• `Optional` **diffuse**: `Object`

环境漫反射部分。

#### # Type declaration
 | Name | Type | Description |
| --- | --- | --- |
| `coefficients` | `Float32Array` | 球谐系数SH9。 |
### # skybox

• `Optional` **skybox**: `Object`

天空盒。

#### # Type declaration
 | Name | Type | Description |
| --- | --- | --- |
| `half` | `boolean` | 是否只使用贴图的上半部分，一般在和`specular`共用贴图的时候为`true`。 |
| `map` | `default` | 贴图。 |
### # specular

• `Optional` **specular**: `Object`

环境高光反射部分。

#### # Type declaration
 | Name | Type | Description |
| --- | --- | --- |
| `map` | `default` | 贴图。 |
| `mipmapCount?` | `number` | 使用的mipmap级数。 |
| `mipmaps` | `boolean` | 是否使用mipmap。 |
| `rgbd` | `boolean` | 是否使用`rgbd`编码来。 |
| `type` | `"2D"` | 贴图类型，目前只支持2D。 | The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)