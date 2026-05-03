> Source: https://developers.weixin.qq.com/miniprogram/dev/api/open-api/setting/AuthSetting.html

## AuthSetting

用户授权设置信息，详情参考[权限](../../../framework/open-ability/authorize.html)

## # 属性

### # boolean scope.userInfo

是否授权用户信息，对应接口 [wx.getUserInfo](../user-info/wx.getUserInfo.html)

### # boolean scope.userLocation

是否授权精确地理位置，对应接口 [wx.getLocation](../../location/wx.getLocation.html), [wx.chooseLocation](../../location/wx.chooseLocation.html)

### # boolean scope.userFuzzyLocation

是否授权模糊地理位置，对应接口 [wx.getFuzzyLocation](../../location/wx.getFuzzyLocation.html)

### # boolean scope.address

是否授权通讯地址，已取消此项授权，会默认返回true

### # boolean scope.invoiceTitle

是否授权发票抬头，已取消此项授权，会默认返回true

### # boolean scope.invoice

是否授权获取发票，已取消此项授权，会默认返回true

### # boolean scope.werun

是否授权微信运动步数，对应接口 [wx.getWeRunData](../werun/wx.getWeRunData.html)

### # boolean scope.record

是否授权录音功能，对应接口 [wx.getRecorderManager](../../media/recorder/wx.getRecorderManager.html)

### # boolean scope.writePhotosAlbum

是否授权保存到相册 [wx.saveImageToPhotosAlbum](../../media/image/wx.saveImageToPhotosAlbum.html), [wx.saveVideoToPhotosAlbum](../../media/video/wx.saveVideoToPhotosAlbum.html)

### # boolean scope.camera

是否授权摄像头，对应[[camera](../../../component/camera.html)]((camera)) 组件

### # boolean scope.bluetooth

是否授权蓝牙，对应接口 [wx.openBluetoothAdapter](../../device/bluetooth/wx.openBluetoothAdapter.html)、[wx.createBLEPeripheralServer](../../device/bluetooth-peripheral/wx.createBLEPeripheralServer.html)

### # boolean scope.addPhoneContact

是否添加通讯录联系人，对应接口 [wx.addPhoneContact](../../device/contact/wx.addPhoneContact.html)

### # boolean scope.addPhoneCalendar

是否授权系统日历，对应接口 [wx.addPhoneRepeatCalendar](../../device/calendar/wx.addPhoneRepeatCalendar.html)、[wx.addPhoneCalendar](../../device/calendar/wx.addPhoneCalendar.html)
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)