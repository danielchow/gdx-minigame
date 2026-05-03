> Source: https://developers.weixin.qq.com/miniprogram/dev/api/xr-frame/interfaces/IVertexLayoutOptions.html

[xr-frame](./../) / [Exports](./../modules.html) / IVertexLayoutOptions

## Interface: IVertexLayoutOptions

顶点布局解构初始化参数。

## # Table of contents

### # Properties

- [attributes](./IVertexLayoutOptions.html#attributes)
 - [step](./IVertexLayoutOptions.html#step)
 - [stepRate](./IVertexLayoutOptions.html#stepRate)
 - [stride](./IVertexLayoutOptions.html#stride)


## # Properties

### # attributes

• **attributes**: { `format`: [`EVertexFormat`](./../enums/EVertexFormat.html) ; `name`: `string` ; `offset`: `number` ; `usage`: [`EVertexLayoutUsage`](./../enums/EVertexLayoutUsage.html)  }[]

顶点属性列表。

### # step

• `Optional` **step**: [`EVertexStep`](./../enums/EVertexStep.html)

步进类型。

**`default`** EVertexStep.PER_VERTEX

### # stepRate

• `Optional` **stepRate**: `number`

步进单位。

**`default`** 1

### # stride

• `Optional` **stride**: `number`

步长，不设定会自动计算。
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)