# @capacitor/opentok

.

## Install

```bash
npm install @capacitor/opentok
npx cap sync
```

## API

<docgen-index>

* [`echo(...)`](#echo)
* [`connect(...)`](#connect)
* [`disconnect()`](#disconnect)
* [`mute(...)`](#mute)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### echo(...)

```typescript
echo(options: { value: string; }) => any
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ value: string; }</code> |

**Returns:** <code>any</code>

--------------------


### connect(...)

```typescript
connect(options: OpentokConnectOptions) => any
```

| Param         | Type                                                                    |
| ------------- | ----------------------------------------------------------------------- |
| **`options`** | <code><a href="#opentokconnectoptions">OpentokConnectOptions</a></code> |

**Returns:** <code>any</code>

--------------------


### disconnect()

```typescript
disconnect() => any
```

**Returns:** <code>any</code>

--------------------


### mute(...)

```typescript
mute(enabled: boolean) => any
```

| Param         | Type                 |
| ------------- | -------------------- |
| **`enabled`** | <code>boolean</code> |

**Returns:** <code>any</code>

--------------------


### Interfaces


#### OpentokConnectOptions

| Prop             | Type                |
| ---------------- | ------------------- |
| **`api_key`**    | <code>string</code> |
| **`session_id`** | <code>string</code> |
| **`token`**      | <code>string</code> |


#### AudioStatus

| Prop          | Type                 |
| ------------- | -------------------- |
| **`enabled`** | <code>boolean</code> |

</docgen-api>
