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
public class ScheduleReplayDTO {
    @NotNull(message = "原始排期ID不能为空")
    private Long originalScheduleId;

    @NotNull(message = "补播日期不能为空")
    private LocalDate replayDate;

    @NotNull(message = "补播开始时间不能为空")
    private LocalTime replayStartTime;

    @NotNull(message = "补播结束时间不能为空")
    private LocalTime replayEndTime;

    private Long replayScreenId;

    private String replayRemark;

    private String createBy;
}
