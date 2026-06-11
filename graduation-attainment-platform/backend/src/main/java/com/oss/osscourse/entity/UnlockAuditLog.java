package com.oss.osscourse.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("unlock_audit_log")
public class UnlockAuditLog {
    @TableId(value = "ulog_id", type = IdType.AUTO)
    private Long ulogId;
    private Long classId;
    private Long requestBy;
    private Long approvedBy;
    private String reason;

    @TableField(value = "created_at", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createdAt;
}
