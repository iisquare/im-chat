# official accounts

全量推送时为读扩散，定向推送时为写扩散。

## 公众号

### 数据存储

- 公众号表

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| id | String | 主键 |
| name | String | 名称 |
| ... | - | 配置项 |

- 关注信息表

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| id | String | 主键 |
| ofc_id | String | 公众号主键 |
| user_id | String | 关联用户主键 |
| label | String | 用户标签，英文逗号分割 |
| ... | - | 配置项 |

- 公众号消息表

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| id | String | 消息服务端统一主键 |
| version | Long | 消息版本号，用于客户端增量同步 |
| sequence | String | 消息客户端统一主键 |
| ofc_id | String | 公众号主键 |
| sender | String | 发送者主键，程序接口调用时为空 |
| receiver | String | 接收者标识（全部，定向，为空时表示用户向程序端发送） |
| shard | String | 分表字段，采用公众号主键Hash |
| content | String | 消息体 |
| send_time | Long | 消息发送时间 |

- 公众号消息推送表

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| msg_id | String | 公众号消息主键 |
| ofc_id | String | 公众号主键 |
| receiver | String | 接收者标识（全部用户或用户主键） |
| shard | String | 分表字段，采用公众号主键Hash |
| send_time | Long | 消息发送时间 |

### 存取规则

- 服务端全量推送消息（接口调用）
```
{
    ofc_id: '公众号'
}
- 写入"公众号消息表"(sender='', receiver='__all__')
- 写入"公众号消息推送表"(receiver='__all__')
- 查询"关注信息表"，分批推送消息
```
- 服务端定向推送消息（接口调用）
```
{
    ofc_id: '公众号',
    receiver: '接收者筛选条件'
}
- 写入"公众号消息表"(sender='', receiver='__direct__')
- 查询"关注信息表"获取满足条筛选条件的关注者
- 循环写入"公众号消息推送表"(receiver='用户主键')
- 分批推送消息
```
- 用户拉取历史消息（客户端）
```
select msg_id from '公众号消息推送表' where ofc_id='公众号主键' and (receiver='__all__' || receiver='用户主键');
select * from '公众号消息表' where id in (select msg_id);
```
- 用户发送消息（客户端）
```
{
    ofc_id: '公众号'
}
- 写入"公众号消息表" (sender='用户主键', receiver='')
- 转发至公众号服务端接收接口
```
- 服务端拉取历史消息（接口调用）
```
from '公众号消息表' where ofc_id='公众号主键' and sender=''; // 历史推送
from '公众号消息表' where ofc_id='公众号主键' and sender!=''; // 历史接收
from '公众号消息表' where ofc_id='公众号主键' and sender='用户主键'; // 指定用户的消息
```
