package com.ad.schedule.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConflictPlanTypeEnum {
    EXISTING_FIRST(1, "原排期优先"),
    NEW_FIRST(2, "新排期优先"),
    SPLIT_TIME(3, "拆分时段"),
    RECOMMEND_REPLAY(4, "推荐补播");

    private final Integer code;
    private final String desc;

    public static ConflictPlanTypeEnum getByCode(Integer code) {
        for (ConflictPlanTypeEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
