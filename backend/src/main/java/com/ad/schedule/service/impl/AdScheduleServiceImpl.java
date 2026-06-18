package com.ad.schedule.service.impl;

import com.ad.schedule.common.BusinessException;
import com.ad.schedule.dto.ScheduleCreateDTO;
import com.ad.schedule.dto.ScheduleUpdateDTO;
import com.ad.schedule.entity.AdMaterial;
import com.ad.schedule.entity.AdSchedule;
import com.ad.schedule.entity.AdScreen;
import com.ad.schedule.enums.AuditStatusEnum;
import com.ad.schedule.enums.ProofStatusEnum;
import com.ad.schedule.enums.ScheduleStatusEnum;
import com.ad.schedule.mapper.AdMaterialMapper;
import com.ad.schedule.mapper.AdScheduleMapper;
import com.ad.schedule.mapper.AdScreenMapper;
import com.ad.schedule.service.AdScheduleService;
import com.ad.schedule.vo.ScheduleDetailVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AdScheduleServiceImpl extends ServiceImpl<AdScheduleMapper, AdSchedule> implements AdScheduleService {

    @Autowired
    private AdScreenMapper screenMapper;

    @Autowired
    private AdMaterialMapper materialMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdSchedule createSchedule(ScheduleCreateDTO dto) {
        AdScreen screen = screenMapper.selectById(dto.getScreenId());
        if (screen == null) {
            throw new BusinessException("广告屏不存在");
        }
        if (screen.getStatus() != 1) {
            throw new BusinessException("广告屏未启用，无法排期");
        }

        AdMaterial material = materialMapper.selectById(dto.getMaterialId());
        if (material == null) {
            throw new BusinessException("素材不存在");
        }
        if (!AuditStatusEnum.PASSED.getCode().equals(material.getAuditStatus())) {
            throw new BusinessException("素材未通过审核，无法排期");
        }

        if (dto.getEndTime().isBefore(dto.getStartTime()) || dto.getEndTime().equals(dto.getStartTime())) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }

        if (dto.getDuration() == null || dto.getDuration() <= 0) {
            throw new BusinessException("播放时长必须大于0");
        }

        boolean hasConflict = checkConflict(dto.getScreenId(), dto.getPlayDate(),
                dto.getStartTime(), dto.getEndTime(), null);
        if (hasConflict) {
            throw new BusinessException("该广告屏在此时段已有排期，请调整时间");
        }

        AdSchedule schedule = new AdSchedule();
        String scheduleCode = "SCH" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int) (Math.random() * 10000));
        schedule.setScheduleCode(scheduleCode);
        schedule.setScreenId(dto.getScreenId());
        schedule.setMaterialId(dto.getMaterialId());
        schedule.setPlayDate(dto.getPlayDate());
        schedule.setStartTime(dto.getStartTime());
        schedule.setEndTime(dto.getEndTime());
        schedule.setDuration(dto.getDuration());
        schedule.setPlayOrder(dto.getPlayOrder() != null ? dto.getPlayOrder() : 0);
        schedule.setRemark(dto.getRemark());
        schedule.setCreateBy(dto.getCreateBy());
        schedule.setScheduleStatus(ScheduleStatusEnum.PENDING.getCode());
        schedule.setProofStatus(ProofStatusEnum.NOT_SUBMITTED.getCode());
        this.save(schedule);

        log.info("排期创建成功, scheduleCode: {}", scheduleCode);
        return schedule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdSchedule updateSchedule(ScheduleUpdateDTO dto) {
        AdSchedule schedule = this.getById(dto.getId());
        if (schedule == null) {
            throw new BusinessException("排期不存在");
        }

        if (ScheduleStatusEnum.CANCELLED.getCode().equals(schedule.getScheduleStatus())) {
            throw new BusinessException("已取消的排期不能修改");
        }

        if (ProofStatusEnum.SUBMITTED.getCode().equals(schedule.getProofStatus())) {
            if (dto.getDuration() != null && !dto.getDuration().equals(schedule.getDuration())) {
                throw new BusinessException("已回传刊播证明的排期不能修改播放时长");
            }
            if (dto.getPlayDate() != null) {
                throw new BusinessException("已回传刊播证明的排期不能修改播放日期");
            }
            if (dto.getStartTime() != null || dto.getEndTime() != null) {
                throw new BusinessException("已回传刊播证明的排期不能修改播放时段");
            }
            if (dto.getScreenId() != null && !dto.getScreenId().equals(schedule.getScreenId())) {
                throw new BusinessException("已回传刊播证明的排期不能修改广告屏");
            }
            if (dto.getMaterialId() != null && !dto.getMaterialId().equals(schedule.getMaterialId())) {
                throw new BusinessException("已回传刊播证明的排期不能修改素材");
            }
        }

        Long screenId = dto.getScreenId() != null ? dto.getScreenId() : schedule.getScreenId();
        Long materialId = dto.getMaterialId() != null ? dto.getMaterialId() : schedule.getMaterialId();
        LocalDate playDate = dto.getPlayDate() != null ? dto.getPlayDate() : schedule.getPlayDate();
        LocalTime startTime = dto.getStartTime() != null ? dto.getStartTime() : schedule.getStartTime();
        LocalTime endTime = dto.getEndTime() != null ? dto.getEndTime() : schedule.getEndTime();

        if (dto.getScreenId() != null || dto.getPlayDate() != null
                || dto.getStartTime() != null || dto.getEndTime() != null) {
            AdScreen screen = screenMapper.selectById(screenId);
            if (screen == null || screen.getStatus() != 1) {
                throw new BusinessException("广告屏不存在或未启用");
            }

            if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
                throw new BusinessException("结束时间必须晚于开始时间");
            }

            boolean hasConflict = checkConflict(screenId, playDate, startTime, endTime, schedule.getId());
            if (hasConflict) {
                throw new BusinessException("该广告屏在此时段已有排期，请调整时间");
            }
        }

        if (dto.getMaterialId() != null) {
            AdMaterial material = materialMapper.selectById(materialId);
            if (material == null) {
                throw new BusinessException("素材不存在");
            }
            if (!AuditStatusEnum.PASSED.getCode().equals(material.getAuditStatus())) {
                throw new BusinessException("素材未通过审核，无法排期");
            }
        }

        if (dto.getDuration() != null && dto.getDuration() <= 0) {
            throw new BusinessException("播放时长必须大于0");
        }

        if (dto.getScreenId() != null) schedule.setScreenId(screenId);
        if (dto.getMaterialId() != null) schedule.setMaterialId(materialId);
        if (dto.getPlayDate() != null) schedule.setPlayDate(playDate);
        if (dto.getStartTime() != null) schedule.setStartTime(startTime);
        if (dto.getEndTime() != null) schedule.setEndTime(endTime);
        if (dto.getDuration() != null) schedule.setDuration(dto.getDuration());
        if (dto.getPlayOrder() != null) schedule.setPlayOrder(dto.getPlayOrder());
        if (dto.getRemark() != null) schedule.setRemark(dto.getRemark());
        schedule.setUpdateBy(dto.getUpdateBy());
        this.updateById(schedule);

        log.info("排期修改成功, scheduleId: {}", dto.getId());
        return schedule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelSchedule(Long id, String operator) {
        AdSchedule schedule = this.getById(id);
        if (schedule == null) {
            throw new BusinessException("排期不存在");
        }

        if (ProofStatusEnum.SUBMITTED.getCode().equals(schedule.getProofStatus())) {
            throw new BusinessException("已回传刊播证明的排期不能取消");
        }

        if (ScheduleStatusEnum.CANCELLED.getCode().equals(schedule.getScheduleStatus())) {
            throw new BusinessException("排期已取消");
        }

        schedule.setScheduleStatus(ScheduleStatusEnum.CANCELLED.getCode());
        schedule.setUpdateBy(operator);
        this.updateById(schedule);
        log.info("排期取消成功, scheduleId: {}", id);
    }

    @Override
    public Page<ScheduleDetailVO> pageDetail(Integer pageNum, Integer pageSize, Long screenId,
                                             LocalDate startDate, LocalDate endDate,
                                             Integer scheduleStatus, Integer proofStatus) {
        Page<AdSchedule> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AdSchedule> wrapper = new LambdaQueryWrapper<>();
        if (screenId != null) {
            wrapper.eq(AdSchedule::getScreenId, screenId);
        }
        if (startDate != null) {
            wrapper.ge(AdSchedule::getPlayDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(AdSchedule::getPlayDate, endDate);
        }
        if (scheduleStatus != null) {
            wrapper.eq(AdSchedule::getScheduleStatus, scheduleStatus);
        }
        if (proofStatus != null) {
            wrapper.eq(AdSchedule::getProofStatus, proofStatus);
        }
        wrapper.orderByDesc(AdSchedule::getPlayDate, AdSchedule::getStartTime);
        Page<AdSchedule> schedulePage = this.page(page, wrapper);

        Page<ScheduleDetailVO> resultPage = new Page<>(schedulePage.getCurrent(), schedulePage.getSize(), schedulePage.getTotal());
        List<ScheduleDetailVO> voList = new ArrayList<>();
        for (AdSchedule schedule : schedulePage.getRecords()) {
            ScheduleDetailVO vo = buildDetailVO(schedule);
            voList.add(vo);
        }
        resultPage.setRecords(voList);
        return resultPage;
    }

    @Override
    public ScheduleDetailVO getDetail(Long id) {
        AdSchedule schedule = this.getById(id);
        if (schedule == null) {
            throw new BusinessException("排期不存在");
        }
        return buildDetailVO(schedule);
    }

    @Override
    public List<AdSchedule> listByScreenAndDate(Long screenId, LocalDate playDate) {
        LambdaQueryWrapper<AdSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdSchedule::getScreenId, screenId)
                .eq(AdSchedule::getPlayDate, playDate)
                .ne(AdSchedule::getScheduleStatus, ScheduleStatusEnum.CANCELLED.getCode())
                .orderByAsc(AdSchedule::getStartTime);
        return this.list(wrapper);
    }

    @Override
    public boolean checkConflict(Long screenId, LocalDate playDate,
                                 LocalTime startTime, LocalTime endTime,
                                 Long excludeId) {
        List<AdSchedule> conflicting = baseMapper.findConflictingSchedules(
                screenId, playDate, startTime, endTime, excludeId == null ? -1L : excludeId);
        return conflicting != null && !conflicting.isEmpty();
    }

    private ScheduleDetailVO buildDetailVO(AdSchedule schedule) {
        ScheduleDetailVO vo = new ScheduleDetailVO();
        BeanUtils.copyProperties(schedule, vo);
        AdScreen screen = screenMapper.selectById(schedule.getScreenId());
        if (screen != null) {
            vo.setScreenName(screen.getScreenName());
            vo.setScreenCode(screen.getScreenCode());
        }
        AdMaterial material = materialMapper.selectById(schedule.getMaterialId());
        if (material != null) {
            vo.setMaterialName(material.getMaterialName());
            vo.setMaterialCode(material.getMaterialCode());
            vo.setCustomerName(material.getCustomerName());
            vo.setMaterialType(material.getMaterialType());
            vo.setFileUrl(material.getFileUrl());
            vo.setAuditStatus(material.getAuditStatus());
        }
        return vo;
    }
}
