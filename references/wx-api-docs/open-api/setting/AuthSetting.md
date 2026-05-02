> Source: https://developers.weixin.qq.com/minigame/dev/api/open-api/setting/AuthSetting.html
# AuthSetting
用户授权设置信息，详情参考[权限](../../../guide/base-ability/authorize.html)
## 属性
### boolean scope.userInfo
是否授权用户信息，对应接口 [wx.getUserInfo](../user-info/wx.getUserInfo.html)
### boolean scope.userLocation
是否授权精确地理位置，对应接口 [wx.getLocation](../../location/wx.getLocation.html)。将废弃，请使用 scope.userFuzzyLocation 代替
### boolean scope.userFuzzyLocation
是否授权模糊地理位置，对应接口 [wx.getFuzzyLocation](../../location/wx.getFuzzyLocation.html)
### boolean scope.werun
是否授权微信运动步数，对应接口 [wx.getWeRunData](../werun/wx.getWeRunData.html)
### boolean scope.record
是否授权录音功能，对应接口 [wx.getRecorderManager](../../media/recorder/wx.getRecorderManager.html)
### boolean scope.writePhotosAlbum
是否授权保存到相册，对应接口 [wx.saveImageToPhotosAlbum](../../media/image/wx.saveImageToPhotosAlbum.html)
### boolean scope.WxFriendInteraction
是否授权使用你的微信朋友信息，对应开放数据域内的 [wx.getFriendCloudStorage](../data/wx.getFriendCloudStorage.html) 、[wx.getGroupCloudStorage](../data/wx.getGroupCloudStorage.html) 、[wx.getGroupInfo](../data/wx.getGroupInfo.html) 、[wx.getPotentialFriendList](../data/wx.getPotentialFriendList.html) 、[wx.getUserCloudStorageKeys](../data/wx.getUserCloudStorageKeys.html) 、[wx.getUserInfo](../data/OpenDataContext-wx.getUserInfo.html)  、[GameServerManager.getFriendsStateData](../../game-server-manager/GameServerManager.getFriendsStateData.html) 接口，以及主域内的 [wx.getUserInteractiveStorage](../data/wx.getUserInteractiveStorage.html) 接口。
