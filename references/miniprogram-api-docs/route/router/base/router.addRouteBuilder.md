> Source: https://developers.weixin.qq.com/miniprogram/dev/api/route/router/base/router.addRouteBuilder.html

## router.addRouteBuilder(string routeType, function routeBuilder)

**小程序插件**：不支持

相关文档: [自定义路由](../../../../framework/runtime/skyline/custom-route.html)

## # 功能描述

添加自定义路由配置

## # 参数

### # string routeType

路由类型

### # function routeBuilder

[路由动画定义函数]((CustomRouteBuilder))

## # 自定义路由示例

[在开发者工具中预览效果](https://developers.weixin.qq.com/s/y1IbQpmA7wGZ)

```js
// 定义自定义效果，从右侧推入
const slideRouteBuilder = (customRouteContext) => {
  const { primaryAnimation } = customRouteContext
  const handlePrimaryAnimation = () => {
    'worklet'
    const transX = windowWidth * (1 - primaryAnimation.value)
	   return {
		   transform: `translateX(${transX}px)`,
	   }
  }
  return {
    handlePrimaryAnimation
  }
}

wx.router.addRouteBuilder('slide', slideRouteBuilder)

// 使用自定义路由
wx.navigateTo({
  url: 'xxx',
  routeType: 'slide'
})
```
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)