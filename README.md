# im-chat
Instant Message 即时通讯
- 基于Mars处理弱网络环境下的长连接问题，使用MySQL存储聊天记录。基于Protobuf制定多端通讯协议，采用Redis实现分布式锁、存储黑名单、转发下行通知。
- 简介：支持PC、触屏、APP、小程序等多端接入，支持Comet长连接、TCP、WebSocket、TLS等多种连接方式，支持文本、图片、语音、位置、房源信息等多种消息类型。
- 特点：采用主从、冷热分离等方式提高数据存取性能；协议层与业务层分离，支持长连接、短连接混合访问模式提高网络传输性能；提供服务端定制化接口，方便业务端统计、管理和批量下发通知。

## 服务拆分
- 用户中心：IM端不关联实体用户，认证和用户信息通过用户服务获取。
- IM服务：支持长连接、短连接混合访问模式，存储聊天数据，下发消息通知。
- 存储服务：保存用户上传的图片、语音、视频等数据，IM消息体中仅包含对应关联信息。

## 消息存储
采用MySQL数据库存储消息记录，存储架构整体遵循读扩散+增量扩散的原则，单个用户的发送数据和接收数据存储在同一张表中，并通过分表、分库、主从等方式提高读写性能。若仅采用读扩散，则分表策略受限制，难以支撑千万级以上的数据读写。若仅采用写扩散，则增加拉取对话消息的复杂度，难以遍历单对单聊天的历史数据。

### 单对单消息
- 表结构

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| id | String | 消息服务端统一主键 |
| version | Long | 消息版本号，用于客户端增量同步 |
| sequence | String | 消息客户端统一主键 |
| sender | String | 发送者主键 |
| receiver | String | 接收者主键 |
| shard | String | 分表字段，发送者主键或接收者主键 |
| content | String | 消息体 |
| send_time | Long | 消息发送时间 |
| withdraw_time | Long | 消息撤回时间 |

- 消息存储

当用户A发送消息时，将shard赋值为发送者A的主键，写入“发送”消息表。若发送者A的主键与接收者B的主键分表结果不一致，则将shard赋值为接收者B的主键，全量（含完整消息内容）拷贝到“接收”消息表中。此处的“发送”消息表和“接收”消息表，由于shard分表结果不一致，属于两张相同结构的物理表，但在业务代码端属于同一张逻辑实体表。

- 对话历史

根据“(发送者=A and 接收者=B) or (发送者=B and 接收者=A)”查询语句可直接获取到单对单聊天的全部内容，通过增加时间筛选限制用户可拉取到的最早记录。

- 增量同步

根据“(发送者=A or 接收者=A) and version>:begin and version<=:end”查询语句可获取到用户未同步的全部记录，通过分页拉取即可同步到最新数据。

### 群组消息
- 表结构

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| id | String | 消息服务端统一主键 |
| version | Long | 消息版本号，用于客户端增量同步 |
| sequence | String | 消息客户端统一主键 |
| sender | String | 发送者主键 |
| receiver | String | 群组主键，同时用于分表 |
| content | String | 消息体 |
| send_time | Long | 消息发送时间 |
| withdraw_time | Long | 消息撤回时间 |

### 会话状态

- 联系人列表

采用Redis进行Hash存储，Key为对话者的类型+主键，Value含最后一条收发的消息内容。允许用户删除单个会话，或清除整个会话列表。

- 未读消息计数

采用Redis进行Hash存储，Key为对话者的类型+主键，Value为未读的消息数量。当用户阅读消息后，直接删除对应的会话Key。

- 发布订阅

IM服务端各节点维护各自的长连接，当有消息达到时，直接写入到MySQL进行持久化，同时写入到Redis的发布通道。订阅通道的节点，通过判断接收者是否在自己的连接列表中，将消息下发给客户端。

## 交互流程

### 有状态客户端

通过Socket+TLS进行连接，维护本地SQLite数据库。

- 通过用户中心登陆并获取Tocken。
- 与IM建立Socket连接，并发送tocken认证。
- 获取联系人列表和未读消息计数。
- 获取最新版本号，若落后则进行同步（个人消息、群组消息等分开同步），并写入本地数据库。
- 按心跳策略维护长连接，发送ping消息，服务端在pong中返回最新版本号。

### 无状态客户端

不在本地存储数据，数据直接通过WebSocket+TLS或HTTP+TLS从服务端拉取。

## 参考
- [IM服务器设计-消息存储](https://www.codedump.info/post/20190608-im-msg-storage/)
- [一套海量在线用户的移动端IM架构设计实践分享](http://www.52im.net/thread-812-1-1.html)
