> Source: https://developers.weixin.qq.com/miniprogram/dev/api/wxml/MediaQueryObserver.observe.html

## MediaQueryObserver.observe(Object descriptor, function callback)

**小程序插件**：支持

## # 功能描述

开始监听页面 media query 变化情况

## # 参数

### # Object descriptor

media query 描述符
 | 属性 | 类型 | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| minWidth | number |  | 否 | 页面最小宽度（ px 为单位） |
| maxWidth | number |  | 否 | 页面最大宽度（ px 为单位） |
| width | number |  | 否 | 页面宽度（ px 为单位） |
| minHeight | number |  | 否 | 页面最小高度（ px 为单位） |
| maxHeight | number |  | 否 | 页面最大高度（ px 为单位） |
| height | number |  | 否 | 页面高度（ px 为单位） |
| orientation | string |  | 否 | 屏幕方向（ `landscape` 或 `portrait` ） |
### # function callback

监听 media query 状态变化的回调函数

#### # 参数

##### # Object res
 | 属性 | 类型 | 说明 |
| --- | --- | --- |
| matches | boolean | 页面的当前状态是否满足所指定的 media query | The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)