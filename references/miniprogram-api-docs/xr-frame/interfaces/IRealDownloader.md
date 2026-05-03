> Source: https://developers.weixin.qq.com/miniprogram/dev/api/xr-frame/interfaces/IRealDownloader.html

[xr-frame](./../) / [Exports](./../modules.html) / IRealDownloader

## Interface: IRealDownloader

外部需要注入的下载器接口。

## # Table of contents

### # Methods

- [load](./IRealDownloader.html#load)


## # Methods

### # load

▸ **load**(`options`): `void`

#### # Parameters
 | Name | Type |
| --- | --- |
| `options` | `Object` |
| `options.onError` | (`error`: `Error`) => `void` |
| `options.onLoad` | (`res`: { `data`: `ArrayBuffer` ; `filePath`: `string`  }) => `void` |
| `options.encoding` | `"binary"` | `"utf-8"` |
| `options.src` | `string` |
#### # Returns

`void`
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)