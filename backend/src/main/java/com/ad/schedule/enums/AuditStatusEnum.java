package com.ad.schedule.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuditStatusEnum {
    PENDING(0, "待审核"),
    PASSED(1, "审核通过"),
    REJECTED(2, "审核未通过");

    private final Integer code;
    private final String desc;

    public static AuditStatusEnum getByCode(Integer code) {
        for (AuditStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
