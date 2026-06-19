package com.ad.schedule.controller;

import com.ad.schedule.common.Result;
import com.ad.schedule.dto.MaterialAuditDTO;
import com.ad.schedule.dto.MaterialScreenAuditDTO;
import com.ad.schedule.dto.MaterialSubmitDTO;
import com.ad.schedule.entity.AdMaterial;
import com.ad.schedule.service.AdMaterialService;
import com.ad.schedule.vo.MaterialScreenAuditVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/material")
public class AdMaterialController {

    @Autowired
    private AdMaterialService materialService;

    @PostMapping("/submit")
    public Result<AdMaterial> submit(@Validated @RequestBody MaterialSubmitDTO dto) {
        return Result.success(materialService.submitMaterial(dto));
    }

    @PostMapping("/audit")
    public Result<AdMaterial> audit(@Validated @RequestBody MaterialAuditDTO dto) {
        return Result.success(materialService.auditMaterial(dto));
    }

    @PostMapping("/audit-by-screen")
    public Result<MaterialScreenAuditVO> auditByScreen(@Validated @RequestBody MaterialScreenAuditDTO dto) {
        return Result.success(materialService.auditMaterialByScreen(dto));
    }

    @GetMapping("/screen-audit/{materialId}")
    public Result<MaterialScreenAuditVO> getScreenAudit(@PathVariable Long materialId) {
        return Result.success(materialService.getMaterialScreenAudit(materialId));
    }

    @GetMapping("/check-screen-pass")
    public Result<Boolean> checkScreenPass(@RequestParam Long materialId,
                                           @RequestParam Long screenId) {
        return Result.success(materialService.checkMaterialPassedForScreen(materialId, screenId));
    }

    @GetMapping("/list-audited")
    public Result<List<AdMaterial>> listAudited() {
        return Result.success(materialService.listAuditedMaterials());
    }

    @GetMapping("/page")
    public Result<Page<AdMaterial>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         @RequestParam(required = false) Integer auditStatus,
                                         @RequestParam(required = false) String customerName) {
        return Result.success(materialService.pageByCondition(pageNum, pageSize, auditStatus, customerName));
    }

    @GetMapping("/{id}")
    public Result<AdMaterial> getById(@PathVariable Long id) {
        return Result.success(materialService.getDetail(id));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(materialService.removeById(id));
    }
}
