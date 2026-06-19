package com.ad.schedule.vo;

import com.ad.schedule.entity.AdSchedule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ScheduleDetailVO extends AdSchedule {
    private String screenName;
    private String screenCode;
    private String materialName;
    private String materialCode;
    private String customerName;
    private Integer materialType;
    private String fileUrl;
    private Integer auditStatus;
}
