package com.ad.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialAuditDTO {
    @NotNull(message = "素材ID不能为空")
    private Long materialId;

    @NotNull(message = "审核状态不能为空")
    private Integer auditStatus;

    private String auditRemark;

    @NotNull(message = "审核人不能为空")
    private String auditor;
}
