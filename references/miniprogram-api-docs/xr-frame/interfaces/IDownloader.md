> Source: https://developers.weixin.qq.com/miniprogram/dev/api/xr-frame/interfaces/IDownloader.html

[xr-frame](./../) / [Exports](./../modules.html) / IDownloader

## Interface: IDownloader

下载器。

## # Table of contents

### # Properties

- [REAL_DOWNLOADER](./IDownloader.html#REAL_DOWNLOADER)
 - [inWX](./IDownloader.html#inWX)


### # Methods

- [LOAD](./IDownloader.html#LOAD)


## # Properties

### # REAL_DOWNLOADER

• **REAL_DOWNLOADER**: [`IRealDownloader`](./IRealDownloader.html)

### # inWX

• **inWX**: `boolean`

## # Methods

### # LOAD

▸ **LOAD**(`options`): `void`

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