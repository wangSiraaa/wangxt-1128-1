package com.ad.schedule.service.impl;

import com.ad.schedule.common.BusinessException;
import com.ad.schedule.dto.ProofSubmitDTO;
import com.ad.schedule.entity.AdProof;
import com.ad.schedule.entity.AdSchedule;
import com.ad.schedule.enums.ProofStatusEnum;
import com.ad.schedule.enums.ScheduleStatusEnum;
import com.ad.schedule.mapper.AdProofMapper;
import com.ad.schedule.mapper.AdScheduleMapper;
import com.ad.schedule.service.AdProofService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class AdProofServiceImpl extends ServiceImpl<AdProofMapper, AdProof> implements AdProofService {

    @Autowired
    private AdScheduleMapper scheduleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdProof submitProof(ProofSubmitDTO dto) {
        AdSchedule schedule = scheduleMapper.selectById(dto.getScheduleId());
        if (schedule == null) {
            throw new BusinessException("排期不存在");
        }

        if (ScheduleStatusEnum.CANCELLED.getCode().equals(schedule.getScheduleStatus())) {
            throw new BusinessException("已取消的排期不能回传刊播证明");
        }

        AdProof proof = new AdProof();
        String proofCode = "PRF" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int) (Math.random() * 10000));
        proof.setProofCode(proofCode);
        proof.setScheduleId(dto.getScheduleId());
        proof.setScreenId(schedule.getScreenId());
        proof.setMaterialId(schedule.getMaterialId());
        proof.setProofType(dto.getProofType());
        proof.setFileUrl(dto.getFileUrl());
        proof.setFileName(dto.getFileName());
        proof.setFileSize(dto.getFileSize());
        proof.setActualStartTime(dto.getActualStartTime());
        proof.setActualEndTime(dto.getActualEndTime());
        proof.setActualDuration(dto.getActualDuration());
        proof.setRemark(dto.getRemark());
        proof.setSubmitBy(dto.getSubmitBy());
        proof.setSubmitTime(LocalDateTime.now());
        this.save(proof);

        schedule.setProofStatus(ProofStatusEnum.SUBMITTED.getCode());
        if (!ScheduleStatusEnum.FINISHED.getCode().equals(schedule.getScheduleStatus())) {
            schedule.setScheduleStatus(ScheduleStatusEnum.FINISHED.getCode());
        }
        scheduleMapper.updateById(schedule);

        log.info("刊播证明回传成功, proofCode: {}, scheduleId: {}", proofCode, dto.getScheduleId());
        return proof;
    }

    @Override
    public Page<AdProof> pageByCondition(Integer pageNum, Integer pageSize, Long scheduleId, Long screenId) {
        Page<AdProof> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AdProof> wrapper = new LambdaQueryWrapper<>();
        if (scheduleId != null) {
            wrapper.eq(AdProof::getScheduleId, scheduleId);
        }
        if (screenId != null) {
            wrapper.eq(AdProof::getScreenId, screenId);
        }
        wrapper.orderByDesc(AdProof::getSubmitTime);
        return this.page(page, wrapper);
    }

    @Override
    public List<AdProof> listByScheduleId(Long scheduleId) {
        LambdaQueryWrapper<AdProof> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdProof::getScheduleId, scheduleId)
                .orderByDesc(AdProof::getSubmitTime);
        return this.list(wrapper);
    }

    @Override
    public AdProof getDetail(Long id) {
        AdProof proof = this.getById(id);
        if (proof == null) {
            throw new BusinessException("刊播证明不存在");
        }
        return proof;
    }
}
