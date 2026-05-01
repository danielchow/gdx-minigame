# UE HTTP 集成指南

Source: https://developers.weixin.qq.com/minigame/dev/guide/game-engine/common-adaptation/Design/UEHttpIntegration.html

本文档介绍 Unreal Engine (UE) HTTP 模块在微信小游戏环境下的适配方案、主要修改点以及使用示例。

## 概述

UE 引擎使用 FHttpModule 进行 HTTP 请求。在微信小游戏环境下，我们对 UE 的 HTTP 模块进行了适配，使其能够：
- 正确发起 HTTP 请求并处理响应
- 支持文件下载模式，将下载文件自动缓存到本地
- 支持请求取消功能
- 修复异步回调问题

### 源代码文件

| 文件 | 说明 | 原始路径 |
|------|------|----------|
| HTML5HTTP.h | HTTP 请求类头文件 | Engine/Platforms/HTML5/Source/Runtime/Online/HTTP/Private/ |
| HTML5HTTP.cpp | HTTP 请求类实现文件 | Engine/Platforms/HTML5/Source/Runtime/Online/HTTP/Private/ |
| HTML5JavaScriptFx.h | JavaScript 接口声明头文件 | Engine/Platforms/HTML5/Source/Runtime/HTML5/HTML5JS/Public/ |
| HTML5JavaScriptFx.js | JavaScript 层适配文件 | Engine/Platforms/HTML5/Source/Runtime/HTML5/HTML5JS/Private/ |

> 将上述文件复制到 UE 引擎源码的对应位置，替换原有文件即可完成 HTTP 模块的微信小游戏适配。

## 引擎修改点

### 1. C++ 层修改

**新增请求 ID 管理：**

```cpp
class FHTML5HttpRequest : public FHttpRequestImpl {
private:
  uint64 RequestId;
  static FCriticalSection RequestIdLock;
  static uint64 NextRequestId;
  static uint64 GenerateRequestId();
};
```

**取消请求改进：**

```cpp
void FHTML5HttpRequest::CancelRequest() {
  UE_CancelHTTPDataRequest(RequestId);  // 调用 JS 层取消请求
  bCanceled = true;
}
```

### 2. JavaScript 层修改

**新增请求取消函数：**

```javascript
UE_CancelHTTPDataRequest: function (id) {
  var GlobalModule = typeof wx !== "undefined" ? GameGlobal.WXGameKit.gameInstance.Module : Module;
  if(GlobalModule._HTTPDataRequestMap && GlobalModule._HTTPDataRequestMap[id]){
    let xhr = GlobalModule._HTTPDataRequestMap[id];
    delete GlobalModule._HTTPDataRequestMap[id];
    xhr.abort();
  }
},
```

**HTTP 请求适配：**

```javascript
// 微信环境使用 WXGameKit.network.XMLHttpRequest
if(typeof wx !== "undefined"){
  var xhr = new WXGameKit.network.XMLHttpRequest();
} else {
  var xhr = new XMLHttpRequest();
}
```

## 文件下载与缓存机制

### 核心概念

| 响应模式 | 条件 | 响应内容 |
|----------|------|----------|
| 二进制数据模式 | 未设置下载文件头或不满足缓存规则 | 下载内容的二进制数据 |
| 文件路径模式 | 设置了下载文件头且满足缓存规则 | 缓存文件的本地路径 |

### 设置下载文件头

```cpp
Request->SetHeader(TEXT("wechatminigame-downloadfile"), TEXT("1"));
```

### 配置缓存规则

在 handlers.js 中配置：

```javascript
WXGameKit.fs.setAssetCacheableHandler((path) => {
  const cacheableFileIdentifier = ["StreamingAssets"];
  const excludeFileIdentifier = ["json"];
  if (cacheableFileIdentifier.some(identifier =>
    path.includes(identifier) &&
    excludeFileIdentifier.every(excludeIdentifier => !path.includes(excludeIdentifier))
  )) {
    return true;
  }
  return false;
});
```

### 缓存文件路径

缓存路径格式：`/CustomWritablePath/__GAME_FILE_CACHE/[原始URL路径]`

## 使用示例

### 基本 HTTP GET 请求

```cpp
void MyClass::SendGetRequest() {
  TSharedRef<IHttpRequest, ESPMode::ThreadSafe> Request = FHttpModule::Get().CreateRequest();
  Request->SetURL(TEXT("https://api.example.com/data"));
  Request->SetVerb(TEXT("GET"));
  Request->SetHeader(TEXT("Content-Type"), TEXT("application/json"));
  Request->OnProcessRequestComplete().BindUObject(this, &MyClass::OnResponseReceived);
  Request->ProcessRequest();
}
```

### POST 请求

```cpp
void MyClass::SendPostRequest() {
  TSharedRef<IHttpRequest, ESPMode::ThreadSafe> Request = FHttpModule::Get().CreateRequest();
  Request->SetURL(TEXT("https://api.example.com/users"));
  Request->SetVerb(TEXT("POST"));
  Request->SetHeader(TEXT("Content-Type"), TEXT("application/json"));
  Request->SetContentAsString(TEXT("{\"name\":\"Tom\",\"age\":25}"));
  Request->OnProcessRequestComplete().BindUObject(this, &MyClass::OnResponseReceived);
  Request->ProcessRequest();
}
```

### 文件下载（缓存路径模式）

```cpp
void MyClass::DownloadFileWithCache() {
  TSharedRef<IHttpRequest, ESPMode::ThreadSafe> Request = FHttpModule::Get().CreateRequest();
  Request->SetURL(TEXT("https://cdn.example.com/StreamingAssets/audio/bgm.mp3"));
  Request->SetVerb(TEXT("GET"));
  Request->SetHeader(TEXT("wechatminigame-downloadfile"), TEXT("1"));

  Request->OnProcessRequestComplete().BindLambda(
    [](FHttpRequestPtr Request, FHttpResponsePtr Response, bool bSuccess) {
      if (bSuccess && Response.IsValid()) {
        FString ResponseStr = Response->GetContentAsString();
        if (ResponseStr.Contains(TEXT("__GAME_FILE_CACHE"))) {
          // 文件已缓存到本地路径，可用 fopen/fread 直接读取
          ReadCachedFile(ResponseStr);
        }
      }
    });
  Request->ProcessRequest();
}
```

### 跳过缓存清理

```cpp
Request->SetHeader(TEXT("wechatminigame-downloadfile"), TEXT("1"));
Request->SetHeader(TEXT("wechatminigame-skipclean"), TEXT("1"));
```

### 缓存管理

在 handlers.js 中配置可清理规则：

```javascript
WXGameKit.fs.setErasableHandler((info) => {
  if (GameGlobal.WXGameKit.gameInstance.IDBFS.isOpen(
    WXGameKit.fs.resolveCachePath(info.path))) {
    return false;
  }
  if (WXGameKit.fs.isWXAssetBundle(info.path)) {
    return false;
  }
  return true;
});
```

## 注意事项

- **域名配置**：生产环境需要在微信公众平台配置合法域名
- **缓存空间**：微信小游戏有存储空间限制，需要合理规划缓存策略
- **文件访问**：只有满足缓存规则的文件才会返回路径，其他情况返回二进制数据
- **路径前缀**：缓存文件路径固定以 `/CustomWritablePath/__GAME_FILE_CACHE/` 开头
- **请求取消**：支持通过 `Request->CancelRequest()` 取消进行中的请求

## 下一步

- 了解 [HTTP 客户端适配](./10-HttpAdapter.md)
- 了解 [文件系统适配](./11-FileSystemAdapter.md)
- 了解 [TCP/UDP Socket 适配](./12-SocketAdapter.md)
- 了解 [WebSocket 适配](./13-WebSocketAdapter.md)
