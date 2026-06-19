package com.ad.schedule.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReplayTypeEnum {
    NORMAL(0, "正常刊播"),
    REPLAY(1, "补播记录");

    private final Integer code;
    private final String desc;

    public static ReplayTypeEnum getByCode(Integer code) {
        for (ReplayTypeEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
