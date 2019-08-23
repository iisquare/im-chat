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
 * 用户信息
 */
public class User implements Serializable {

    @Id
    private String id; // 主键
    @Column
    private String token; // Token，唯一约束
    @Column
    private Date block; // 封禁结束时间
    @Column
    private Integer pushable; // 客户端通知

    public String block() {
        if (null == block) return null;
        return String.valueOf(block.getTime());
    }

    public String pushable() {
        if (null == pushable) return null;
        return String.valueOf(pushable);
    }

    public boolean isBlocked() {
        if (null == block) return false;
        return block.getTime() > System.currentTimeMillis();
    }

}
