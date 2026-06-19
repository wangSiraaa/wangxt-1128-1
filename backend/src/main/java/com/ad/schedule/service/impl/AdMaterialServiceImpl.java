package com.ad.schedule.service.impl;

import cn.hutool.core.util.StrUtil;
import com.ad.schedule.common.BusinessException;
import com.ad.schedule.dto.MaterialAuditDTO;
import com.ad.schedule.dto.MaterialScreenAuditDTO;
import com.ad.schedule.dto.MaterialSubmitDTO;
import com.ad.schedule.entity.AdAuditLog;
import com.ad.schedule.entity.AdMaterial;
import com.ad.schedule.entity.AdMaterialScreenAudit;
import com.ad.schedule.entity.AdScreen;
import com.ad.schedule.enums.AuditStatusEnum;
import com.ad.schedule.mapper.AdAuditLogMapper;
import com.ad.schedule.mapper.AdMaterialMapper;
import com.ad.schedule.mapper.AdMaterialScreenAuditMapper;
import com.ad.schedule.mapper.AdScreenMapper;
import com.ad.schedule.service.AdMaterialService;
import com.ad.schedule.vo.MaterialScreenAuditVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AdMaterialServiceImpl extends ServiceImpl<AdMaterialMapper, AdMaterial> implements AdMaterialService {

    @Autowired
    private AdAuditLogMapper auditLogMapper;

    @Autowired
    private AdMaterialScreenAuditMapper materialScreenAuditMapper;

    @Autowired
    private AdScreenMapper screenMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdMaterial submitMaterial(MaterialSubmitDTO dto) {
        AdMaterial material = new AdMaterial();
        String materialCode = "MAT" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int) (Math.random() * 10000));
        material.setMaterialCode(materialCode);
        material.setMaterialName(dto.getMaterialName());
        material.setCustomerName(dto.getCustomerName());
        material.setMaterialType(dto.getMaterialType());
        material.setFileUrl(dto.getFileUrl());
        material.setFileSize(dto.getFileSize());
        material.setDuration(dto.getDuration());
        material.setDescription(dto.getDescription());
        material.setCreateBy(dto.getCreateBy());
        material.setAuditStatus(AuditStatusEnum.PENDING.getCode());
        material.setSubmitTime(LocalDateTime.now());
        this.save(material);
        log.info("素材提交成功, materialCode: {}", materialCode);
        return material;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdMaterial auditMaterial(MaterialAuditDTO dto) {
        AdMaterial material = this.getById(dto.getMaterialId());
        if (material == null) {
            throw new BusinessException("素材不存在");
        }

        if (!AuditStatusEnum.PENDING.getCode().equals(material.getAuditStatus())) {
            throw new BusinessException("当前素材状态不允许审核");
        }

        if (!AuditStatusEnum.PASSED.getCode().equals(dto.getAuditStatus())
                && !AuditStatusEnum.REJECTED.getCode().equals(dto.getAuditStatus())) {
            throw new BusinessException("审核状态参数错误");
        }

        material.setAuditStatus(dto.getAuditStatus());
        material.setAuditTime(LocalDateTime.now());
        material.setAuditor(dto.getAuditor());
        material.setAuditRemark(dto.getAuditRemark());
        this.updateById(material);

        AdAuditLog auditLog = new AdAuditLog();
        auditLog.setMaterialId(material.getId());
        auditLog.setAuditStatus(dto.getAuditStatus());
        auditLog.setAuditRemark(dto.getAuditRemark());
        auditLog.setAuditor(dto.getAuditor());
        auditLog.setAuditTime(LocalDateTime.now());
        auditLogMapper.insert(auditLog);

        log.info("素材审核完成, materialId: {}, auditStatus: {}", dto.getMaterialId(), dto.getAuditStatus());
        return material;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaterialScreenAuditVO auditMaterialByScreen(MaterialScreenAuditDTO dto) {
        AdMaterial material = this.getById(dto.getMaterialId());
        if (material == null) {
            throw new BusinessException("素材不存在");
        }

        if (dto.getScreenAuditList() == null || dto.getScreenAuditList().isEmpty()) {
            throw new BusinessException("屏幕审核列表不能为空");
        }

        List<MaterialScreenAuditDTO.ScreenAuditItem> items = dto.getScreenAuditList();
        int allPassedCount = 0;
        int hasAuditCount = 0;

        for (MaterialScreenAuditDTO.ScreenAuditItem item : items) {
            if (item.getScreenId() == null && StrUtil.isBlank(item.getBusinessDistrict())) {
                continue;
            }

            AdMaterialScreenAudit screenAudit;
            if (item.getScreenId() != null) {
                screenAudit = materialScreenAuditMapper.getByMaterialAndScreen(dto.getMaterialId(), item.getScreenId());
            } else {
                LambdaQueryWrapper<AdMaterialScreenAudit> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(AdMaterialScreenAudit::getMaterialId, dto.getMaterialId())
                        .eq(AdMaterialScreenAudit::getBusinessDistrict, item.getBusinessDistrict())
                        .isNull(AdMaterialScreenAudit::getScreenId)
                        .last("LIMIT 1");
                screenAudit = materialScreenAuditMapper.selectOne(wrapper);
            }

            if (screenAudit == null) {
                screenAudit = new AdMaterialScreenAudit();
                screenAudit.setMaterialId(dto.getMaterialId());
                screenAudit.setScreenId(item.getScreenId());
                screenAudit.setBusinessDistrict(item.getBusinessDistrict());
                screenAudit.setAuditStatus(item.getAuditStatus());
                screenAudit.setAuditRemark(item.getAuditRemark());
                screenAudit.setComplianceRule(item.getComplianceRule());
                screenAudit.setAuditTime(LocalDateTime.now());
                screenAudit.setAuditor(dto.getAuditor());
                materialScreenAuditMapper.insert(screenAudit);
            } else {
                screenAudit.setAuditStatus(item.getAuditStatus());
                screenAudit.setAuditRemark(item.getAuditRemark());
                screenAudit.setComplianceRule(item.getComplianceRule());
                screenAudit.setAuditTime(LocalDateTime.now());
                screenAudit.setAuditor(dto.getAuditor());
                materialScreenAuditMapper.updateById(screenAudit);
            }

            if (item.getScreenId() != null) {
                AdAuditLog auditLog = new AdAuditLog();
                auditLog.setMaterialId(dto.getMaterialId());
                auditLog.setScreenId(item.getScreenId());
                auditLog.setAuditStatus(item.getAuditStatus());
                auditLog.setAuditRemark(item.getAuditRemark() + " [商圈:" + item.getBusinessDistrict() + "]");
                auditLog.setAuditor(dto.getAuditor());
                auditLog.setAuditTime(LocalDateTime.now());
                auditLogMapper.insert(auditLog);
            }

            hasAuditCount++;
            if (AuditStatusEnum.PASSED.getCode().equals(item.getAuditStatus())) {
                allPassedCount++;
            }
        }

        if (hasAuditCount > 0) {
            if (allPassedCount == hasAuditCount) {
                material.setAuditStatus(AuditStatusEnum.PASSED.getCode());
                material.setAuditRemark("全部分屏审核通过");
            } else if (allPassedCount == 0) {
                material.setAuditStatus(AuditStatusEnum.REJECTED.getCode());
                material.setAuditRemark("全部分屏审核未通过");
            } else {
                material.setAuditStatus(AuditStatusEnum.PENDING.getCode());
                material.setAuditRemark("部分分屏审核完成，待其余分屏审核");
            }
            material.setAuditTime(LocalDateTime.now());
            material.setAuditor(dto.getAuditor());
            this.updateById(material);
        }

        log.info("素材分屏审核完成, materialId: {}, 已审核: {}, 通过: {}", dto.getMaterialId(), hasAuditCount, allPassedCount);
        return getMaterialScreenAudit(dto.getMaterialId());
    }

    @Override
    public MaterialScreenAuditVO getMaterialScreenAudit(Long materialId) {
        AdMaterial material = this.getById(materialId);
        if (material == null) {
            throw new BusinessException("素材不存在");
        }

        MaterialScreenAuditVO vo = new MaterialScreenAuditVO();
        vo.setMaterialId(material.getId());
        vo.setMaterialName(material.getMaterialName());
        vo.setCustomerName(material.getCustomerName());
        vo.setOverallAuditStatus(material.getAuditStatus());
        vo.setOverallAuditRemark(material.getAuditRemark());

        List<AdMaterialScreenAudit> auditList = materialScreenAuditMapper.listByMaterialIdWithScreen(materialId);
        List<MaterialScreenAuditVO.ScreenAuditDetail> detailList = new ArrayList<>();

        for (AdMaterialScreenAudit audit : auditList) {
            MaterialScreenAuditVO.ScreenAuditDetail detail = new MaterialScreenAuditVO.ScreenAuditDetail();
            BeanUtils.copyProperties(audit, detail);

            if (audit.getScreenId() != null) {
                AdScreen screen = screenMapper.selectById(audit.getScreenId());
                if (screen != null) {
                    detail.setScreenCode(screen.getScreenCode());
                    detail.setScreenName(screen.getScreenName());
                    if (detail.getBusinessDistrict() == null) {
                        detail.setBusinessDistrict(screen.getBusinessDistrict());
                    }
                }
            }

            AuditStatusEnum statusEnum = AuditStatusEnum.getByCode(audit.getAuditStatus());
            detail.setAuditStatusDesc(statusEnum != null ? statusEnum.getDesc() : "未知");
            detailList.add(detail);
        }
        vo.setScreenAuditDetails(detailList);
        return vo;
    }

    @Override
    public boolean checkMaterialPassedForScreen(Long materialId, Long screenId) {
        AdMaterial material = this.getById(materialId);
        if (material == null) {
            return false;
        }
        if (AuditStatusEnum.PASSED.getCode().equals(material.getAuditStatus())) {
            return true;
        }
        AdMaterialScreenAudit screenAudit = materialScreenAuditMapper.getByMaterialAndScreen(materialId, screenId);
        if (screenAudit != null && AuditStatusEnum.PASSED.getCode().equals(screenAudit.getAuditStatus())) {
            return true;
        }
        if (screenId != null) {
            AdScreen screen = screenMapper.selectById(screenId);
            if (screen != null && StrUtil.isNotBlank(screen.getBusinessDistrict())) {
                LambdaQueryWrapper<AdMaterialScreenAudit> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(AdMaterialScreenAudit::getMaterialId, materialId)
                        .eq(AdMaterialScreenAudit::getBusinessDistrict, screen.getBusinessDistrict())
                        .eq(AdMaterialScreenAudit::getAuditStatus, AuditStatusEnum.PASSED.getCode())
                        .last("LIMIT 1");
                AdMaterialScreenAudit districtAudit = materialScreenAuditMapper.selectOne(wrapper);
                if (districtAudit != null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<AdMaterial> listAuditedMaterials() {
        LambdaQueryWrapper<AdMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdMaterial::getAuditStatus, AuditStatusEnum.PASSED.getCode())
                .orderByDesc(AdMaterial::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public Page<AdMaterial> pageByCondition(Integer pageNum, Integer pageSize, Integer auditStatus, String customerName) {
        Page<AdMaterial> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AdMaterial> wrapper = new LambdaQueryWrapper<>();
        if (auditStatus != null) {
            wrapper.eq(AdMaterial::getAuditStatus, auditStatus);
        }
        if (StrUtil.isNotBlank(customerName)) {
            wrapper.like(AdMaterial::getCustomerName, customerName);
        }
        wrapper.orderByDesc(AdMaterial::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public AdMaterial getDetail(Long id) {
        AdMaterial material = this.getById(id);
        if (material == null) {
            throw new BusinessException("素材不存在");
        }
        return material;
    }
}
