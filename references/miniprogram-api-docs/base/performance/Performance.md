> Source: https://developers.weixin.qq.com/miniprogram/dev/api/base/performance/Performance.html

## Performance

基础库 2.11.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

Performance 对象，用于获取性能数据及创建性能监听器

## # 方法

### # Array.<PerformanceEntry> Performance.getEntries()

该方法返回当前缓冲区中的所有性能数据

### # Array.<PerformanceEntry> Performance.getEntriesByType(string entryType)

获取当前缓冲区中所有类型为 [entryType] 的性能数据

### # Array.<PerformanceEntry> Performance.getEntriesByName(string name, string entryType)

获取当前缓冲区中所有名称为 [name] 且类型为 [entryType] 的性能数据

### # PerformanceObserver Performance.createObserver(function callback)

创建全局性能事件监听器

### # Performance.setBufferSize(number size)

设置缓冲区大小，默认缓冲 30 条性能数据
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)