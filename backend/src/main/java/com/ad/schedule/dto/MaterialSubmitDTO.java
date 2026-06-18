package com.ad.schedule.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class MaterialSubmitDTO {
    @NotBlank(message = "素材名称不能为空")
    private String materialName;

    @NotBlank(message = "客户名称不能为空")
    private String customerName;

    private Integer materialType;

    @NotBlank(message = "素材文件地址不能为空")
    private String fileUrl;

    private Long fileSize;

    private Integer duration;

    private String description;

    private String createBy;
}
