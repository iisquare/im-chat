package com.iisquare.im.server.api.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
/**
 * 发送消息
 */
public class Message implements Serializable {

    @Id
    private String id; // 主键
    @Column
    private Long version; // 累加版本号，单个发送者下唯一
    @Column
    private String sequence; // 序列号，用于回执
    @Column
    private String sender; // 发送者
    @Column
    private String reception; // 接收方类型[person-单聊，group-群发，official-公众号]
    @Column
    private String receiver; // 接收者
    @Column
    private String type; // 消息类型
    @Column
    private String content; // 消息内容
    @Column
    private Date time; // 发送时间
    @Column
    private Date withdraw; // 撤回时间

}
