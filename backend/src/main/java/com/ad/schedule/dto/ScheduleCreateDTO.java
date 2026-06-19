package com.ad.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleCreateDTO {
    @NotNull(message = "广告屏ID不能为空")
    private Long screenId;

    @NotNull(message = "素材ID不能为空")
    private Long materialId;

    @NotNull(message = "播放日期不能为空")
    private LocalDate playDate;

    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;

    @NotNull(message = "播放时长不能为空")
    private Integer duration;

    private Integer playOrder;

    private String remark;

    private String createBy;
}
