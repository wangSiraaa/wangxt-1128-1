package com.ad.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialScreenAuditDTO {
    @NotNull(message = "素材ID不能为空")
    private Long materialId;

    @NotNull(message = "审核人不能为空")
    private String auditor;

    private List<ScreenAuditItem> screenAuditList;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScreenAuditItem {
        private Long screenId;
        private String businessDistrict;
        @NotNull(message = "审核状态不能为空")
        private Integer auditStatus;
        private String auditRemark;
        private String complianceRule;
    }
}
