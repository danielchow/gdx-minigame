> Source: https://developers.weixin.qq.com/miniprogram/dev/api/xr-frame/interfaces/IEventBridge.html

[xr-frame](./../) / [Exports](./../modules.html) / IEventBridge

## Interface: IEventBridge

## # Table of contents

### # Methods

- [bindEntitiesToBones](./IEventBridge.html#bindEntitiesToBones)
 - [bindEntityToBone](./IEventBridge.html#bindEntityToBone)
 - [entityAddChild](./IEventBridge.html#entityAddChild)
 - [entityAddChildAtIndex](./IEventBridge.html#entityAddChildAtIndex)
 - [entityClear](./IEventBridge.html#entityClear)
 - [entityRemoveFromParent](./IEventBridge.html#entityRemoveFromParent)
 - [entitySetActive](./IEventBridge.html#entitySetActive)
 - [entitySetLocalMatrixDirty](./IEventBridge.html#entitySetLocalMatrixDirty)
 - [refreshWorldTransform](./IEventBridge.html#refreshWorldTransform)
 - [setRootEntity](./IEventBridge.html#setRootEntity)
 - [unbindEntitiesFromBones](./IEventBridge.html#unbindEntitiesFromBones)
 - [unbindEntityFromBone](./IEventBridge.html#unbindEntityFromBone)


## # Methods

### # bindEntitiesToBones

▸ **bindEntitiesToBones**(`entities`, `boneEntities`): `void`

#### # Parameters
 | Name | Type |
| --- | --- |
| `entities` | { `id`: `number`  }[] |
| `boneEntities` | { `id`: `number`  }[] |
#### # Returns

`void`

### # bindEntityToBone

▸ **bindEntityToBone**(`entity`, `boneEntity`): `void`

#### # Parameters
 | Name | Type |
| --- | --- |
| `entity` | `Object` |
| `entity.id` | `number` |
| `boneEntity` | `Object` |
| `boneEntity.id` | `number` |
#### # Returns

`void`

### # entityAddChild

▸ **entityAddChild**(`entity`, `child`): `void`

#### # Parameters
 | Name | Type |
| --- | --- |
| `entity` | `number` |
| `child` | `number` |
#### # Returns

`void`

### # entityAddChildAtIndex

▸ **entityAddChildAtIndex**(`entity`, `child`, `index`): `void`

#### # Parameters
 | Name | Type |
| --- | --- |
| `entity` | `number` |
| `child` | `number` |
| `index` | `number` |
#### # Returns

`void`

### # entityClear

▸ **entityClear**(`entity`): `void`

#### # Parameters
 | Name | Type |
| --- | --- |
| `entity` | `number` |
#### # Returns

`void`

### # entityRemoveFromParent

▸ **entityRemoveFromParent**(`entity`): `void`

#### # Parameters
 | Name | Type |
| --- | --- |
| `entity` | `number` |
#### # Returns

`void`

### # entitySetActive

▸ **entitySetActive**(`entity`, `active`): `void`

#### # Parameters
 | Name | Type |
| --- | --- |
| `entity` | `number` |
| `active` | `boolean` |
#### # Returns

`void`

### # entitySetLocalMatrixDirty

▸ **entitySetLocalMatrixDirty**(`entity`): `void`

#### # Parameters
 | Name | Type |
| --- | --- |
| `entity` | `number` |
#### # Returns

`void`

### # refreshWorldTransform

▸ **refreshWorldTransform**(): `void`

#### # Returns

`void`

### # setRootEntity

▸ **setRootEntity**(`entity`): `void`

#### # Parameters
 | Name | Type |
| --- | --- |
| `entity` | `number` |
#### # Returns

`void`

### # unbindEntitiesFromBones

▸ **unbindEntitiesFromBones**(`entities`): `void`

#### # Parameters
 | Name | Type |
| --- | --- |
| `entities` | { `id`: `number`  }[] |
#### # Returns

`void`

### # unbindEntityFromBone

▸ **unbindEntityFromBone**(`entity`): `void`

#### # Parameters
 | Name | Type |
| --- | --- |
| `entity` | `Object` |
| `entity.id` | `number` |
#### # Returns

`void`
 The translations are provided by WeChat Translation and are for reference only. In case of any inconsistency and discrepancy between the Chinese version and the English version, the Chinese version shall prevail.Incorrect translation. [Tap to report.](javascript:;)