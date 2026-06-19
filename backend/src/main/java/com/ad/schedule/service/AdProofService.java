package com.ad.schedule.service;

import com.ad.schedule.dto.ProofLockDTO;
import com.ad.schedule.dto.ProofSubmitDTO;
import com.ad.schedule.entity.AdProof;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AdProofService extends IService<AdProof> {
    AdProof submitProof(ProofSubmitDTO dto);

    AdProof appendReplayProof(ProofSubmitDTO dto);

    AdProof lockProof(ProofLockDTO dto);

    Page<AdProof> pageByCondition(Integer pageNum, Integer pageSize, Long scheduleId, Long screenId);

    List<AdProof> listByScheduleId(Long scheduleId);

    List<AdProof> listNormalProofByScheduleId(Long scheduleId);

    List<AdProof> listReplayProofByScheduleId(Long scheduleId);

    AdProof getDetail(Long id);
}
