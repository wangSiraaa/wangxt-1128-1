package com.ad.schedule.vo;

import com.ad.schedule.entity.AdSchedule;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
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
