# customer service
在线客服。

## 客服号

### 数据存储

- 客服号表

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| id | String | 主键 |
| name | String | 名称 |
| ... | - | 配置项 |

- 客服信息表

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| id | String | 主键 |
| svr_id | String | 客服号主键 |
| user_id | String | 关联用户主键 |
| name | String | 当前客服号下的昵称 |
| ... | - | 配置项 |

- 客服号消息表

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| id | String | 消息服务端统一主键 |
| version | Long | 消息版本号，用于客户端增量同步 |
| sequence | String | 消息客户端统一主键 |
| svr_id | String | 客服号主键 |
| sender | String | 发送者主键 |
| receiver | String | 接收者主键 |
| shard | String | 分表字段，信箱标识 |
| content | String | 消息体 |
| send_time | Long | 消息发送时间 |

### 存取规则

- 用户端发送咨询消息（用户端）
```
{
    svr_id: '客服号',
    sender: '用户ID',
    shard: '用户ID',
    receiver: '' // 无论与哪个客服交流，receiver始终存空值，防止与用户端冲突
}
```
- 用户端拉取消息记录（用户端）
```
svr_id='客服号' and ((sender='用户ID' and receiver='') or (receiver='用户ID')) [and shard='用户ID']
```
- 客服端回复用户消息（客服端）
```
{
    svr_id: '客服号',
    sender: '客服ID',
    shard: '用户ID',
    receiver: '用户ID'
}
```
- 客服端拉取消息记录（客服端）
```
svr_id='客服号' and (sender='用户ID' or receiver='用户ID') [and shard='用户ID']
```

### 回复机制

```
onRecieve(message) {
    if (是否已分配客服) {
        if (是否启用智能问答 && 是否保持智能问答) {
            do 智能回复
        }
    } else {
        switch (message) {
            case 触发咨询: // 事件类消息，在客服端可见，在用户端不可见
                do 发送欢迎语
                break
            case 人工客服：
                do 转接人工客服
                break
              default:
                if (是否启用智能问答) {
                    do 智能回复
                }
                if (是否立即转接) {
                    do 转接人工客服
                }
        }
    }
}

```

### 客服转接机制

- 客服号的客服人员列表中，可配置客服权限、分配权重、是否允许挂起。
- 若分配权重为0，则不会出现在待分配列表中，但可以被手动转接。
- 若客服选择挂起，则临时从待分配列表中移除。
- 当客服A将用户转接给客服B，客服B收到转接消息（用户不可见），该用户从客服A的会话列表中移除。
- 普通客服，仅可以查看当前服务用户的历史消息，转接后无法查看。

## 交互机制

- 用户端按正常流程全量拉取版本记录，客服账号允许保持普通用户身份。
- 客服端不在本地存储客服号的会话历史，按“无状态客户端”方式拉取会话消息。
