package com.ad.schedule.vo;

import com.ad.schedule.entity.AdMaterialScreenAudit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialScreenAuditVO {
    private Long materialId;
    private String materialName;
    private String customerName;
    private Integer overallAuditStatus;
    private String overallAuditRemark;
    private List<ScreenAuditDetail> screenAuditDetails;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScreenAuditDetail {
        private Long id;
        private Long screenId;
        private String screenCode;
        private String screenName;
        private String businessDistrict;
        private Integer auditStatus;
        private String auditStatusDesc;
        private String auditRemark;
        private String complianceRule;
    }
}
