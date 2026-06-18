package com.ad.schedule.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MaterialTypeEnum {
    IMAGE(1, "图片"),
    VIDEO(2, "视频"),
    TEXT(3, "文字");

    private final Integer code;
    private final String desc;

    public static MaterialTypeEnum getByCode(Integer code) {
        for (MaterialTypeEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
