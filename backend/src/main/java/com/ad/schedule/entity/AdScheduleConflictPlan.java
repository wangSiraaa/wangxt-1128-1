package com.ad.schedule.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ad_schedule_conflict_plan")
public class AdScheduleConflictPlan implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("conflict_schedule_id")
    private Long conflictScheduleId;

    @TableField("new_schedule_id")
    private Long newScheduleId;

    @TableField("plan_type")
    private Integer planType;

    @TableField("plan_detail")
    private String planDetail;

    @TableField("customer_priority_compare")
    private String customerPriorityCompare;

    @TableField("contract_amount_compare")
    private String contractAmountCompare;

    @TableField("recommended_replay_date")
    private LocalDate recommendedReplayDate;

    @TableField("recommended_replay_time")
    private String recommendedReplayTime;

    @TableField("status")
    private Integer status;

    @TableField("create_by")
    private String createBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
