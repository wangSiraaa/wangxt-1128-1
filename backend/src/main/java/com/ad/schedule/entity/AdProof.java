package com.ad.schedule.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ad_proof")
public class AdProof implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("proof_code")
    private String proofCode;

    @TableField("schedule_id")
    private Long scheduleId;

    @TableField("screen_id")
    private Long screenId;

    @TableField("material_id")
    private Long materialId;

    @TableField("proof_type")
    private Integer proofType;

    @TableField("file_url")
    private String fileUrl;

    @TableField("file_name")
    private String fileName;

    @TableField("file_size")
    private Long fileSize;

    @TableField("actual_start_time")
    private LocalDateTime actualStartTime;

    @TableField("actual_end_time")
    private LocalDateTime actualEndTime;

    @TableField("actual_duration")
    private Integer actualDuration;

    @TableField("watermark_sn")
    private String watermarkSn;

    @TableField("screen_code_snapshot")
    private String screenCodeSnapshot;

    @TableField("duration_snapshot")
    private Integer durationSnapshot;

    @TableField("is_locked")
    private Integer isLocked;

    @TableField("lock_time")
    private LocalDateTime lockTime;

    @TableField("locked_by")
    private String lockedBy;

    @TableField("replay_type")
    private Integer replayType;

    @TableField("replay_remark")
    private String replayRemark;

    @TableField("remark")
    private String remark;

    @TableField("submit_time")
    private LocalDateTime submitTime;

    @TableField("submit_by")
    private String submitBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
