> Source: https://developers.weixin.qq.com/miniprogram/dev/api/media/map/MapContext.moveToLocation.html

## MapContext.moveToLocation(Object object)

基础库 1.2.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

**以 [Promise 风格](../../../framework/app-service/api.html#%E5%BC%82%E6%AD%A5-API-%E8%BF%94%E5%9B%9E-Promise) 调用**：不支持

**[用户授权](../../../framework/open-ability/authorize.html)**：需要 scope.userLocation

**小程序插件**：支持

相关文档: [map](../../../component/map.html)

## # 功能描述

将地图中心移置当前定位点，此时需设置地图组件 show-location 为true。['2.8.0'](../../../framework/compatibility.html) 起支持将地图中心移动到指定位置。

## # 参数

### # Object object
 | 属性 | 类型 | 默认值 | 必填 | 说明 | 最低版本 |
| --- | --- | --- | --- | --- | --- |
| longitude | number |  | 否 | 经度 | 2.8.0 |
| latitude | number |  | 否 | 纬度 | 2.8.0 |
| success | function |  | 否 | 接口调用成功的回调函数 |  |
| fail | function |  | 否 | 接口调用失败的回调函数 |  |
| complete | function |  | 否 | 接口调用结束的回调函数（调用成功、失败都会执行） |  | The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)