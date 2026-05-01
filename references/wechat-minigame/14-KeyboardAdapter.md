# 键盘适配

Source: https://developers.weixin.qq.com/minigame/dev/guide/game-engine/common-adaptation/Design/KeyboardAdapter.html

本文档介绍如何在 Emscripten 项目中集成微信小游戏虚拟键盘模块。

## 概述

本模块提供完整的虚拟键盘实现，将 C++ 的键盘操作映射到微信小游戏的 wx.showKeyboard、wx.hideKeyboard、wx.updateKeyboard 等 API。

### 功能特性

| 功能 | 说明 |
|------|------|
| 单行输入 | 标准文本输入 |
| 多行输入 | 支持换行的多行文本输入 |
| 确认按钮类型 | 发送、搜索、下一个、前往、完成 |
| 键盘类型 | 文本键盘、数字键盘（客户端8.0.57+） |
| 输入实时回调 | 用户输入时实时通知 |
| 确认回调 | 用户点击确认按钮时通知 |
| 关闭回调 | 键盘关闭时通知 |
| 确认保持 | 点击确认后保持键盘打开（连续输入场景） |
| 动态更新 | 支持在键盘显示时更新输入内容 |

## 快速开始

### CMakeLists.txt 配置

```cmake
set(EXPORTED_FUNCS "[\
  \"_KeyboardOnInput\", \
  \"_KeyboardOnConfirm\", \
  \"_KeyboardOnComplete\", \
  \"_main\", \"_free\", \"_malloc\"]")

set_target_properties(mygame PROPERTIES
  LINK_FLAGS "\
    -s WASM=1 \
    -s EXPORTED_RUNTIME_METHODS='[\"ccall\", \"cwrap\", \"stringToUTF8\", \"UTF8ToString\", \"lengthBytesUTF8\", \"allocateUTF8\"]' \
    -s EXPORTED_FUNCTIONS='${EXPORTED_FUNCS}' \
    --js-library ${WXGAMESDK_DIR}/jslib/libwxkeyboard_open.jslib \
    -Wl,--whole-archive ${WXGAMESDK_DIR}/lib/libwxgamesdk.a -Wl,--no-whole-archive"
)
```

## API 参考

### 头文件

```cpp
#include "keyboard/wx_keyboard_interface.h"
```

### 枚举类型

**KeyboardConfirmType** - 确认按钮类型：

| 值 | 说明 |
|----|------|
| Send | 发送 |
| Search | 搜索 |
| Next | 下一个 |
| Go | 前往 |
| Done | 完成（默认） |

**KeyboardType** - 键盘类型：

| 值 | 说明 |
|----|------|
| Text | 文本键盘（默认） |
| Number | 数字键盘（客户端8.0.57+） |

**KeyboardEventType** - 键盘事件类型：

| 值 | 说明 |
|----|------|
| InputChanged | 键盘输入内容变化 |
| Confirmed | 键盘确认 |
| Closed | 键盘关闭 |

### 核心接口

```cpp
// 创建键盘配置对象
WXGameSDK::KeyboardConfigInterface* CreateKeyboard(int* keyboard_id);

// 释放键盘配置对象
int ReleaseKeyboard(int keyboard_id);

// 显示微信键盘
int ShowKeyboard(int keyboard_id);

// 隐藏微信键盘
int HideKeyboard();

// 更新键盘输入内容（仅在键盘显示时有效）
int UpdateKeyboardValue(int keyboard_id, const std::string& text);

// 获取当前键盘输入内容
std::string GetKeyboardValue(int keyboard_id);
```

### 配置接口 (KeyboardConfigInterface)

| 方法 | 说明 |
|------|------|
| SetConfirmType(type) | 设置确认按钮类型 |
| SetDefaultValue(text) | 设置默认输入内容 |
| SetMaxLength(length) | 设置最大输入长度（默认255） |
| SetMultiple(enable) | 设置是否多行输入 |
| SetConfirmHold(enable) | 设置确认后是否保持键盘打开 |
| SetKeyboardType(type) | 设置键盘类型（文本/数字） |
| SetInputCallback(callback, user_data) | 设置输入变化回调 |
| SetConfirmCallback(callback, user_data) | 设置确认回调 |
| SetCompleteCallback(callback, user_data) | 设置键盘关闭回调 |

### 回调函数签名

```cpp
// 输入变化/确认/关闭回调
std::function<void(const std::string& text, void* user_data)>
```

## 使用示例

### 基本使用

```cpp
#include "keyboard/wx_keyboard_interface.h"

using namespace WXGameSDK;

// 创建键盘配置
int keyboard_id = -1;
KeyboardConfigInterface* keyboard = CreateKeyboard(&keyboard_id);
if (!keyboard) return;

// 配置键盘
keyboard->SetDefaultValue("请输入内容");
keyboard->SetMaxLength(100);
keyboard->SetConfirmType(KeyboardConfirmType::Done);

// 设置回调
keyboard->SetInputCallback([](const std::string& text, void* user_data) {
  printf("输入变化: %s\n", text.c_str());
}, nullptr);

keyboard->SetConfirmCallback([](const std::string& text, void* user_data) {
  printf("用户确认: %s\n", text.c_str());
}, nullptr);

keyboard->SetCompleteCallback([](const std::string& text, void* user_data) {
  printf("键盘关闭: %s\n", text.c_str());
}, nullptr);

// 显示键盘
ShowKeyboard(keyboard_id);

// 使用完毕后释放
ReleaseKeyboard(keyboard_id);
```

### 多行输入

```cpp
keyboard->SetMultiple(true);
keyboard->SetDefaultValue("第一行\n第二行");
keyboard->SetMaxLength(500);
keyboard->SetConfirmType(KeyboardConfirmType::Send);
```

### 搜索模式（确认后保持键盘）

```cpp
keyboard->SetConfirmHold(true);
keyboard->SetConfirmType(KeyboardConfirmType::Search);
keyboard->SetConfirmCallback([](const std::string& text, void* user_data) {
  printf("执行搜索: %s\n", text.c_str());
  // 键盘仍然打开，用户可继续输入
}, nullptr);
```

### 动态更新键盘内容

```cpp
UpdateKeyboardValue(keyboard_id, "新的内容");
std::string current = GetKeyboardValue(keyboard_id);
```

## 注意事项

- **生命周期管理**：创建的键盘配置对象需要在不使用时调用 ReleaseKeyboard 释放
- **回调线程**：所有回调都在主线程（JS 线程）中执行
- **同一时间只能显示一个键盘**：调用 ShowKeyboard 时已有键盘显示，会先关闭旧键盘
- **回调中禁止操作**：在回调函数中不能调用 ShowKeyboard、HideKeyboard、ReleaseKeyboard
- **最大实例数**：最多同时存在 5 个键盘配置实例

## 下一步

- 了解 [HTTP 客户端适配](./10-HttpAdapter.md)
- 了解 [文件系统适配](./11-FileSystemAdapter.md)
- 了解 [WebSocket 适配](./13-WebSocketAdapter.md)
- 了解 [微信开放接口](./15-WXOpenAPI.md)
