package com.ad.schedule.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ScheduleStatusEnum {
    PENDING(1, "待刊播"),
    PLAYING(2, "刊播中"),
    FINISHED(3, "已完成"),
    CANCELLED(4, "已取消");

    private final Integer code;
    private final String desc;

    public static ScheduleStatusEnum getByCode(Integer code) {
        for (ScheduleStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
