package com.ad.schedule.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProofStatusEnum {
    NOT_SUBMITTED(0, "未回传"),
    SUBMITTED(1, "已回传");

    private final Integer code;
    private final String desc;

    public static ProofStatusEnum getByCode(Integer code) {
        for (ProofStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
