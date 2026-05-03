> Source: https://developers.weixin.qq.com/miniprogram/dev/api/xr-frame/interfaces/ILoaderOptionsSchema.html

[xr-frame](./../) / [Exports](./../modules.html) / ILoaderOptionsSchema

## Interface: ILoaderOptionsSchema

指定继承自[AssetLoader](./../classes/AssetLoader.html)的自定义资源加载器，可以接受的的额外配置的`schema`。
在基础库版本**v2.29.2**以上导出。

比如使用[CubeTextureLoader](./../classes/CubeTextureLoader.html)加载资源时：

```xml
<xr-asset-load
  type="cube-texture" asset-id="sky-cube" src="/assets/textures/skybox/"
  options="faces: right.jpg left.jpg top.jpg bottom.jpg front.jpg back.jpg"
/>
```

对应的`schema`接口为：

```ts
export interface ICubeTextureLoaderOptions {
  // left right top bottom front back
  faces: string[];
}
```ts

对应的`schema`为：
```ts
schema = {
  faces: {type: 'array'}
};
```

## # Indexable

▪ [key: `string`]: { `defaultValue?`: `any` ; `type`: `string`  }
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)