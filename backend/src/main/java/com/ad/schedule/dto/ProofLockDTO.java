package com.ad.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProofLockDTO {
    @NotNull(message = "证明ID不能为空")
    private Long proofId;

    @NotNull(message = "锁定人不能为空")
    private String lockedBy;
}
