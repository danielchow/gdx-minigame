> Source: https://developers.weixin.qq.com/miniprogram/dev/api/base/performance/EntryList.html

## EntryList

基础库 2.11.0 开始支持，低版本需做[兼容处理](../../../framework/compatibility.html)。

EntryList 对象

## # 方法

### # Array.<PerformanceEntry> EntryList.getEntries()

该方法返回当前列表中的所有性能数据

### # Array.<PerformanceEntry> EntryList.getEntriesByType(string entryType)

获取当前列表中所有类型为 [entryType] 的性能数据

### # Array.<PerformanceEntry> EntryList.getEntriesByName(string name, string entryType)

获取当前列表中所有名称为 [name] 且类型为 [entryType] 的性能数据
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)