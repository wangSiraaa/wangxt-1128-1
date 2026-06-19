package com.ad.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialSubmitDTO {
    @NotBlank(message = "素材名称不能为空")
    private String materialName;

    @NotBlank(message = "客户名称不能为空")
    private String customerName;

    @NotNull(message = "素材类型不能为空")
    private Integer materialType;

    @NotBlank(message = "素材文件地址不能为空")
    private String fileUrl;

    private Long fileSize;

    private Integer duration;

    private String description;

    private String createBy;
}
