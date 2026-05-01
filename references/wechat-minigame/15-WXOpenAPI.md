# 微信开放接口集成

Source: https://developers.weixin.qq.com/minigame/dev/guide/game-engine/common-adaptation/Design/WXOpenAPI.html

本文档介绍如何在 Emscripten 项目中集成微信小游戏开放接口，实现登录、分享、支付等平台能力。

## 概述

微信开放接口模块将微信小游戏的 JavaScript API 封装为 C++ 接口，让游戏可以直接使用微信平台提供的各种能力。

### 功能特性

| 模块 | 功能 |
|------|------|
| 登录认证 | 微信登录、获取用户信息 |
| 社交分享 | 分享到好友、朋友圈、群 |
| 数据存储 | 云端数据存储与同步 |
| 广告 | 激励视频、Banner广告 |
| 支付 | 游戏内支付 |
| 系统信息 | 设备信息、网络状态 |
| 小程序跳转 | 跳转其他小程序 |

## 快速开始

### CMakeLists.txt 配置

```cmake
set_target_properties(mygame PROPERTIES
  LINK_FLAGS "\
    -s WASM=1 \
    -s EXPORTED_RUNTIME_METHODS='[\"ccall\", \"cwrap\", \"stringToUTF8\", \"UTF8ToString\", \"lengthBytesUTF8\", \"allocateUTF8\"]' \
    --js-library ${WXGAMESDK_DIR}/jslib/SDK-Call-JS.jslib \
    -Wl,--whole-archive ${WXGAMESDK_DIR}/lib/libwxgamesdk.a -Wl,--no-whole-archive"
)
```

### 小游戏端配置

转换后，在小游戏的 game.js 中添加：

```javascript
import "./wx-wasm-sdk/index";  // 在 Module 生成之前导入
```

## API 参考

### 头文件

```cpp
#include "wx.h"
```

### 登录认证

```cpp
namespace wxwasmsdk {
  struct LoginOption {
    int timeout;
    std::function<void(LoginSuccessCallbackResult&)> success;
    std::function<void(GeneralCallbackResult&)> fail;
    std::function<void(GeneralCallbackResult&)> complete;
  };

  struct LoginSuccessCallbackResult {
    std::string code;  // 用于换取 openid 的临时凭证
  };

  void Login(LoginOption& option);
  void CheckSession(CheckSessionOption& option);
}
```

### 系统信息

```cpp
namespace wxwasmsdk {
  struct AppBaseInfo {
    std::string SDKVersion;
    std::string version;
    std::string language;
    std::string theme;
    bool enableDebug;
    std::string host;
  };

  AppBaseInfo GetAppBaseInfo();
  void GetSystemInfo(GetSystemInfoOption& option);
  SystemInfo GetSystemInfoSync();
}
```

### 事件监听

```cpp
namespace wxwasmsdk {
  void OnShow(OnShowCallback& callback);
  void OnHide(OnHideCallback& callback);
  void OnCopyUrl(OnCopyUrlCallback& callback);
}
```

## 使用示例

### 微信登录

```cpp
#include "wx.h"

void WeChatLogin() {
  wxwasmsdk::LoginOption opt;
  opt.timeout = 10000;
  opt.success = [](wxwasmsdk::LoginSuccessCallbackResult& res) {
    std::cout << "登录成功！code: " << res.code << std::endl;
    SendCodeToServer(res.code);
  };
  opt.fail = [](wxwasmsdk::GeneralCallbackResult& res) {
    std::cout << "登录失败: " << res.errMsg << std::endl;
  };
  wxwasmsdk::Login(opt);
}
```

### 获取系统信息

```cpp
void GetDeviceInfo() {
  auto appBaseInfo = wxwasmsdk::GetAppBaseInfo();
  std::cout << "SDK 版本: " << appBaseInfo.SDKVersion << std::endl;
  std::cout << "微信版本: " << appBaseInfo.version << std::endl;

  wxwasmsdk::GetSystemInfoOption opt;
  opt.success = [](wxwasmsdk::GetSystemInfoSuccessCallbackResult& res) {
    std::cout << "设备品牌: " << res.brand << std::endl;
    std::cout << "屏幕: " << res.screenWidth << "x" << res.screenHeight << std::endl;
  };
  wxwasmsdk::GetSystemInfo(opt);
}
```

### 分享功能

```cpp
void SetupShare() {
  // 设置分享菜单
  wxwasmsdk::ShowShareMenuOption opt;
  opt.withShareTicket = true;
  opt.menus = {"shareAppMessage", "shareTimeline"};
  wxwasmsdk::ShowShareMenu(opt);
}

void ShareToFriend() {
  wxwasmsdk::ShareAppMessageOption opt;
  opt.title = "邀请好友一起玩";
  opt.imageUrl = "https://cdn.example.com/invite.png";
  opt.query = "action=invite&userId=12345";
  wxwasmsdk::ShareAppMessage(opt);
}
```

### 激励视频广告

```cpp
void InitAd() {
  wxwasmsdk::CreateRewardedVideoAdOption opt;
  opt.adUnitId = "your-ad-unit-id";
  auto* ad = wxwasmsdk::CreateRewardedVideoAd(opt);

  ad->OnClose([](wxwasmsdk::RewardedVideoAdOnCloseListenerResult& res) {
    if (res.isEnded) {
      std::cout << "用户完整观看广告，发放奖励" << std::endl;
      GiveReward();
    }
  });
}
```

### 云存档

```cpp
void SaveToCloud(const std::string& key, const std::string& value) {
  wxwasmsdk::SetUserCloudStorageOption opt;
  opt.KVDataList.push_back({key, value});
  wxwasmsdk::SetUserCloudStorage(opt);
}

void LoadFromCloud(const std::vector<std::string>& keys) {
  wxwasmsdk::GetUserCloudStorageOption opt;
  opt.keyList = keys;
  opt.success = [](wxwasmsdk::GetUserCloudStorageSuccessCallbackResult& res) {
    for (const auto& item : res.KVDataList) {
      std::cout << item.key << ": " << item.value << std::endl;
    }
  };
  wxwasmsdk::GetUserCloudStorage(opt);
}
```

### 虚拟支付

```cpp
void BuyItem(const std::string& itemId, int price) {
  wxwasmsdk::RequestMidasPaymentOption opt;
  opt.mode = "game";
  opt.env = 0;
  opt.offerId = "your-offer-id";
  opt.currencyType = "CNY";
  opt.buyQuantity = price;
  opt.success = [&](wxwasmsdk::GeneralCallbackResult& res) {
    GiveItem(itemId);
  };
  wxwasmsdk::RequestMidasPayment(opt);
}
```

## 常见问题

**Q: 登录时报错"未开通XX权限"？**
A: 检查小游戏是否已完成微信认证，并在后台开通相应权限。

**Q: 分享图片不显示？**
A: 确保图片 URL 可访问，且图片尺寸符合要求（建议 5:4 比例）。

**Q: 支付失败？**
A: 检查米大师配置是否正确，offerId 是否正确。

## 注意事项

- **真机测试**：部分功能只能在真机上测试
- **审核合规**：使用用户信息前需要获取用户授权
- **版本兼容**：注意 API 的最低基础库版本要求
- **错误处理**：始终处理 fail 回调

## 下一步

- 了解 [文件系统适配](./11-FileSystemAdapter.md)
- 了解 [网络通信模块](./10-HttpAdapter.md)
- 了解 [SDK 编译与集成](./09-SDKBuild.md)
