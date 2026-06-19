package com.ad.schedule.vo;

import com.ad.schedule.entity.AdSchedule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleConflictVO {
    private boolean hasConflict;
    private List<ConflictDetail> conflictList;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConflictDetail {
        private AdSchedule existingSchedule;
        private String existingCustomerName;
        private Integer existingCustomerPriority;
        private BigDecimal existingContractAmount;
        private String priorityCompare;
        private String amountCompare;
        private List<ReplayPlan> replayPlans;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReplayPlan {
        private Integer planType;
        private String planTypeDesc;
        private LocalDate recommendedDate;
        private String recommendedTime;
        private String description;
    }
}
