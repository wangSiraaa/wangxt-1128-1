package com.ad.schedule.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ad_schedule")
public class AdSchedule implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("schedule_code")
    private String scheduleCode;

    @TableField("screen_id")
    private Long screenId;

    @TableField("material_id")
    private Long materialId;

    @TableField("play_date")
    private LocalDate playDate;

    @TableField("start_time")
    private LocalTime startTime;

    @TableField("end_time")
    private LocalTime endTime;

    @TableField("duration")
    private Integer duration;

    @TableField("play_order")
    private Integer playOrder;

    @TableField("customer_priority")
    private Integer customerPriority;

    @TableField("contract_amount")
    private BigDecimal contractAmount;

    @TableField("schedule_status")
    private Integer scheduleStatus;

    @TableField("proof_status")
    private Integer proofStatus;

    @TableField("replay_of_id")
    private Long replayOfId;

    @TableField("remark")
    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("create_by")
    private String createBy;

    @TableField("update_by")
    private String updateBy;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
