package com.ad.schedule.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class ProofSubmitDTO {
    @NotNull(message = "排期ID不能为空")
    private Long scheduleId;

    @NotNull(message = "证明类型不能为空")
    private Integer proofType;

    @NotNull(message = "证明文件地址不能为空")
    private String fileUrl;

    private String fileName;

    private Long fileSize;

    private LocalDateTime actualStartTime;

    private LocalDateTime actualEndTime;

    private Integer actualDuration;

    private String remark;

    @NotNull(message = "回传人不能为空")
    private String submitBy;
}
