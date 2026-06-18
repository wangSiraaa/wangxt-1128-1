package com.ad.schedule.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ScheduleUpdateDTO {
    @NotNull(message = "排期ID不能为空")
    private Long id;

    private Long screenId;

    private Long materialId;

    private LocalDate playDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer duration;

    private Integer playOrder;

    private String remark;

    private String updateBy;
}
