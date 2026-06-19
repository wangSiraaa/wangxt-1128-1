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
