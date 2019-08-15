package com.iisquare.im.server.api.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
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
@IdClass(Scatter.IdClass.class)
/**
 * 消息读扩散
 */
public class Scatter implements Serializable {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IdClass implements Serializable {
        private String messageId;
        private String receiver;
    }

    @Id
    private String messageId;
    @Id
    private String receiver;
    @Column
    private Long version; // 累加版本号，单个接收者下唯一
    @Column
    private Date read; // 阅读时间
    @Column
    private Date delete; // 删除时间

}
