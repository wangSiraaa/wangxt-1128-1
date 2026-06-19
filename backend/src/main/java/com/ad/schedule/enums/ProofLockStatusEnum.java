package com.ad.schedule.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProofLockStatusEnum {
    UNLOCKED(0, "未锁定"),
    LOCKED(1, "已锁定");

    private final Integer code;
    private final String desc;

    public static ProofLockStatusEnum getByCode(Integer code) {
        for (ProofLockStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
