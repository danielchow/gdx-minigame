> Source: https://developers.weixin.qq.com/miniprogram/dev/api/media/editor/EditorContext.insertCustomBlock.html

## EditorContext.insertCustomBlock(Object object)

基础库 3.7.11 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**以 [Promise 风格](../../../framework/app-service/api.html#%E5%BC%82%E6%AD%A5-API-%E8%BF%94%E5%9B%9E-Promise) 调用**：不支持

**小程序插件**：支持

相关文档: [editor 组件](../../../component/editor.html) [editor-portal组件](../../../component/editor-portal.html)

## # 功能描述

插入自定义区块

## # 参数

### # Object object
 | 属性 | 类型 | 默认值 | 必填 | 说明 |
| --- | --- | --- | --- | --- |
| nowrap | boolean | false | 否 | 插入自定义块后是否自动换行，默认换行 |
| success | function |  | 否 | 接口调用成功的回调函数 |
| fail | function |  | 否 | 接口调用失败的回调函数 |
| complete | function |  | 否 | 接口调用结束的回调函数（调用成功、失败都会执行） |
#### # object.success 回调函数

##### # 参数
 [#](#Object-res) Object res | 属性 | 类型 | 说明 |
| --- | --- | --- |
| blockId | string | 自定义区块标识符，需结合 editor-portal 组件一起使用。 | The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)