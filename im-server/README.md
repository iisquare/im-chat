# im-server

## 技术选型
- spring-boot-2.x 开发框架，后期可集成到微服务
- spring-data-jpa 数据访问层，内部采用herbernate5.x
- redis 数据缓存和同步锁
    ```
    会话状态：im:chat:token:#id
    ```
- mysql 持久化存储
- sharding-jdbc 分表分库中间件
- nginx 负载均衡反向代理
- feign 内部接口调用

## 开发调试
- File > Settings > Plugins > Browse repositories > add lombok
- Settings > Build > Compiler > Annotation Processors > Enable annotation processing
- Settings > Build > Compiler > Build project automatically

## 部署运行

## 参考文档
- [ShardingSphere](https://shardingsphere.apache.org/document/legacy/3.x/document/cn/overview/)
