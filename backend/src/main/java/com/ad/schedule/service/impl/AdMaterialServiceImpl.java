package com.ad.schedule.service.impl;

import cn.hutool.core.util.StrUtil;
import com.ad.schedule.common.BusinessException;
import com.ad.schedule.dto.MaterialAuditDTO;
import com.ad.schedule.dto.MaterialSubmitDTO;
import com.ad.schedule.entity.AdAuditLog;
import com.ad.schedule.entity.AdMaterial;
import com.ad.schedule.enums.AuditStatusEnum;
import com.ad.schedule.mapper.AdAuditLogMapper;
import com.ad.schedule.mapper.AdMaterialMapper;
import com.ad.schedule.service.AdMaterialService;
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
public class AdMaterialServiceImpl extends ServiceImpl<AdMaterialMapper, AdMaterial> implements AdMaterialService {

    @Autowired
    private AdAuditLogMapper auditLogMapper;

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
