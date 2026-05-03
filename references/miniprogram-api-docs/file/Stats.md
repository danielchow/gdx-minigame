> Source: https://developers.weixin.qq.com/miniprogram/dev/api/file/Stats.html

## Stats

相关文档: [文件系统](../../framework/ability/file-system.html)

描述文件状态的对象

## # 属性

### # number mode

文件的类型和存取的权限，对应 POSIX stat.st_mode

### # number size

文件大小，单位：B，对应 POSIX stat.st_size

### # number lastAccessedTime

文件最近一次被存取或被执行的时间，UNIX 时间戳，对应 POSIX stat.st_atime

### # number lastModifiedTime

文件最后一次被修改的时间，UNIX 时间戳，对应 POSIX stat.st_mtime

## # 方法

### # boolean Stats.isDirectory()

判断当前文件是否一个目录

### # boolean Stats.isFile()

判断当前文件是否一个普通文件
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)