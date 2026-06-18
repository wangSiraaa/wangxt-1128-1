package com.ad.schedule.controller;

import com.ad.schedule.common.Result;
import com.ad.schedule.dto.ProofSubmitDTO;
import com.ad.schedule.entity.AdProof;
import com.ad.schedule.service.AdProofService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/proof")
public class AdProofController {

    @Autowired
    private AdProofService proofService;

    @PostMapping("/submit")
    public Result<AdProof> submit(@Validated @RequestBody ProofSubmitDTO dto) {
        return Result.success(proofService.submitProof(dto));
    }

    @GetMapping("/page")
    public Result<Page<AdProof>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                      @RequestParam(required = false) Long scheduleId,
                                      @RequestParam(required = false) Long screenId) {
        return Result.success(proofService.pageByCondition(pageNum, pageSize, scheduleId, screenId));
    }

    @GetMapping("/list-by-schedule/{scheduleId}")
    public Result<List<AdProof>> listByScheduleId(@PathVariable Long scheduleId) {
        return Result.success(proofService.listByScheduleId(scheduleId));
    }

    @GetMapping("/{id}")
    public Result<AdProof> getById(@PathVariable Long id) {
        return Result.success(proofService.getDetail(id));
    }
}
