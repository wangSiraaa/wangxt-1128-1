package com.ad.schedule.service.impl;

import com.ad.schedule.common.BusinessException;
import com.ad.schedule.dto.ProofLockDTO;
import com.ad.schedule.dto.ProofSubmitDTO;
import com.ad.schedule.entity.AdProof;
import com.ad.schedule.entity.AdSchedule;
import com.ad.schedule.entity.AdScreen;
import com.ad.schedule.enums.ProofLockStatusEnum;
import com.ad.schedule.enums.ProofStatusEnum;
import com.ad.schedule.enums.ReplayTypeEnum;
import com.ad.schedule.enums.ScheduleStatusEnum;
import com.ad.schedule.mapper.AdProofMapper;
import com.ad.schedule.mapper.AdScheduleMapper;
import com.ad.schedule.mapper.AdScreenMapper;
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

    @Autowired
    private AdScreenMapper screenMapper;

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

        AdScreen screen = screenMapper.selectById(schedule.getScreenId());
        if (screen == null) {
            throw new BusinessException("广告屏不存在");
        }

        Integer replayType = dto.getReplayType() != null ? dto.getReplayType() : ReplayTypeEnum.NORMAL.getCode();

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
        proof.setWatermarkSn(dto.getWatermarkSn());
        proof.setReplayType(replayType);
        proof.setReplayRemark(dto.getReplayRemark());
        proof.setRemark(dto.getRemark());
        proof.setSubmitBy(dto.getSubmitBy());
        proof.setSubmitTime(LocalDateTime.now());
        proof.setIsLocked(ProofLockStatusEnum.UNLOCKED.getCode());
        this.save(proof);

        schedule.setProofStatus(ProofStatusEnum.SUBMITTED.getCode());
        if (!ScheduleStatusEnum.FINISHED.getCode().equals(schedule.getScheduleStatus())
                && !ScheduleStatusEnum.PENDING_REPLAY.getCode().equals(schedule.getScheduleStatus())) {
            schedule.setScheduleStatus(ScheduleStatusEnum.FINISHED.getCode());
        }
        scheduleMapper.updateById(schedule);

        log.info("刊播证明回传成功, proofCode: {}, scheduleId: {}", proofCode, dto.getScheduleId());
        return proof;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdProof appendReplayProof(ProofSubmitDTO dto) {
        AdSchedule schedule = scheduleMapper.selectById(dto.getScheduleId());
        if (schedule == null) {
            throw new BusinessException("排期不存在");
        }

        if (ScheduleStatusEnum.CANCELLED.getCode().equals(schedule.getScheduleStatus())) {
            throw new BusinessException("已取消的排期不能追加补播证明");
        }

        LambdaQueryWrapper<AdProof> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdProof::getScheduleId, dto.getScheduleId())
                .eq(AdProof::getReplayType, ReplayTypeEnum.NORMAL.getCode())
                .eq(AdProof::getIsLocked, ProofLockStatusEnum.LOCKED.getCode())
                .last("LIMIT 1");
        AdProof lockedNormalProof = this.getOne(wrapper);
        if (lockedNormalProof == null) {
            throw new BusinessException("该排期尚未存在已锁定的正常刊播证明，无法追加补播记录");
        }

        AdScreen screen = screenMapper.selectById(schedule.getScreenId());

        AdProof replayProof = new AdProof();
        String proofCode = "RPF" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int) (Math.random() * 10000));
        replayProof.setProofCode(proofCode);
        replayProof.setScheduleId(dto.getScheduleId());
        replayProof.setScreenId(schedule.getScreenId());
        replayProof.setMaterialId(schedule.getMaterialId());
        replayProof.setProofType(dto.getProofType());
        replayProof.setFileUrl(dto.getFileUrl());
        replayProof.setFileName(dto.getFileName());
        replayProof.setFileSize(dto.getFileSize());
        replayProof.setActualStartTime(dto.getActualStartTime());
        replayProof.setActualEndTime(dto.getActualEndTime());
        replayProof.setActualDuration(dto.getActualDuration());
        replayProof.setWatermarkSn(dto.getWatermarkSn());
        replayProof.setReplayType(ReplayTypeEnum.REPLAY.getCode());
        replayProof.setReplayRemark(dto.getReplayRemark());
        replayProof.setRemark(dto.getRemark());
        replayProof.setSubmitBy(dto.getSubmitBy());
        replayProof.setSubmitTime(LocalDateTime.now());
        replayProof.setIsLocked(ProofLockStatusEnum.LOCKED.getCode());
        replayProof.setLockTime(LocalDateTime.now());
        replayProof.setLockedBy(dto.getSubmitBy());
        replayProof.setScreenCodeSnapshot(screen != null ? screen.getScreenCode() : null);
        replayProof.setDurationSnapshot(schedule.getDuration());
        this.save(replayProof);

        log.info("补播证明追加成功, proofCode: {}, scheduleId: {}", proofCode, dto.getScheduleId());
        return replayProof;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdProof lockProof(ProofLockDTO dto) {
        AdProof proof = this.getById(dto.getProofId());
        if (proof == null) {
            throw new BusinessException("刊播证明不存在");
        }

        if (ProofLockStatusEnum.LOCKED.getCode().equals(proof.getIsLocked())) {
            throw new BusinessException("该刊播证明已锁定，不可重复锁定");
        }

        AdSchedule schedule = scheduleMapper.selectById(proof.getScheduleId());
        if (schedule == null) {
            throw new BusinessException("关联排期不存在");
        }
        AdScreen screen = screenMapper.selectById(proof.getScreenId());

        proof.setIsLocked(ProofLockStatusEnum.LOCKED.getCode());
        proof.setLockTime(LocalDateTime.now());
        proof.setLockedBy(dto.getLockedBy());
        proof.setScreenCodeSnapshot(screen != null ? screen.getScreenCode() : null);
        proof.setDurationSnapshot(proof.getActualDuration() != null
                ? proof.getActualDuration() : schedule.getDuration());

        this.updateById(proof);
        log.info("刊播证明已锁定, proofId: {}, lockedBy: {}", dto.getProofId(), dto.getLockedBy());
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
    public List<AdProof> listNormalProofByScheduleId(Long scheduleId) {
        LambdaQueryWrapper<AdProof> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdProof::getScheduleId, scheduleId)
                .eq(AdProof::getReplayType, ReplayTypeEnum.NORMAL.getCode())
                .orderByDesc(AdProof::getSubmitTime);
        return this.list(wrapper);
    }

    @Override
    public List<AdProof> listReplayProofByScheduleId(Long scheduleId) {
        LambdaQueryWrapper<AdProof> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdProof::getScheduleId, scheduleId)
                .eq(AdProof::getReplayType, ReplayTypeEnum.REPLAY.getCode())
                .orderByAsc(AdProof::getSubmitTime);
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
