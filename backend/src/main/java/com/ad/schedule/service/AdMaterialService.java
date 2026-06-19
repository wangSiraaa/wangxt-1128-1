package com.ad.schedule.service;

import com.ad.schedule.dto.MaterialAuditDTO;
import com.ad.schedule.dto.MaterialScreenAuditDTO;
import com.ad.schedule.dto.MaterialSubmitDTO;
import com.ad.schedule.entity.AdMaterial;
import com.ad.schedule.vo.MaterialScreenAuditVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AdMaterialService extends IService<AdMaterial> {
    AdMaterial submitMaterial(MaterialSubmitDTO dto);

    AdMaterial auditMaterial(MaterialAuditDTO dto);

    MaterialScreenAuditVO auditMaterialByScreen(MaterialScreenAuditDTO dto);

    MaterialScreenAuditVO getMaterialScreenAudit(Long materialId);

    boolean checkMaterialPassedForScreen(Long materialId, Long screenId);

    List<AdMaterial> listAuditedMaterials();

    Page<AdMaterial> pageByCondition(Integer pageNum, Integer pageSize, Integer auditStatus, String customerName);

    AdMaterial getDetail(Long id);
}
