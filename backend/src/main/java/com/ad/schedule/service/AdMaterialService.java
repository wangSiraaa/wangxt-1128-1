package com.ad.schedule.service;

import com.ad.schedule.dto.MaterialAuditDTO;
import com.ad.schedule.dto.MaterialSubmitDTO;
import com.ad.schedule.entity.AdMaterial;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AdMaterialService extends IService<AdMaterial> {
    AdMaterial submitMaterial(MaterialSubmitDTO dto);

    AdMaterial auditMaterial(MaterialAuditDTO dto);

    List<AdMaterial> listAuditedMaterials();

    Page<AdMaterial> pageByCondition(Integer pageNum, Integer pageSize, Integer auditStatus, String customerName);

    AdMaterial getDetail(Long id);
}
