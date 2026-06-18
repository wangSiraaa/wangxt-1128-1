package com.ad.schedule.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class MaterialAuditDTO {
    @NotNull(message = "素材ID不能为空")
    private Long materialId;

    @NotNull(message = "审核状态不能为空")
    private Integer auditStatus;

    private String auditRemark;

    @NotNull(message = "审核人不能为空")
    private String auditor;
}
