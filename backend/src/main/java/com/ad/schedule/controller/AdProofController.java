package com.ad.schedule.controller;

import com.ad.schedule.common.Result;
import com.ad.schedule.dto.ProofLockDTO;
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

    @PostMapping
    public Result<AdProof> submit(@Validated @RequestBody ProofSubmitDTO dto) {
        return Result.success(proofService.submitProof(dto));
    }

    @PostMapping("/replay-append")
    public Result<AdProof> appendReplayProof(@Validated @RequestBody ProofSubmitDTO dto) {
        return Result.success(proofService.appendReplayProof(dto));
    }

    @PostMapping("/lock")
    public Result<AdProof> lock(@Validated @RequestBody ProofLockDTO dto) {
        return Result.success(proofService.lockProof(dto));
    }

    @GetMapping("/page")
    public Result<Page<AdProof>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                      @RequestParam(required = false) Long scheduleId,
                                      @RequestParam(required = false) Long screenId) {
        return Result.success(proofService.pageByCondition(pageNum, pageSize, scheduleId, screenId));
    }

    @GetMapping("/{id}")
    public Result<AdProof> getById(@PathVariable Long id) {
        return Result.success(proofService.getDetail(id));
    }

    @GetMapping("/list-by-schedule/{scheduleId}")
    public Result<List<AdProof>> listByScheduleId(@PathVariable Long scheduleId) {
        return Result.success(proofService.listByScheduleId(scheduleId));
    }

    @GetMapping("/normal-list/{scheduleId}")
    public Result<List<AdProof>> listNormalProof(@PathVariable Long scheduleId) {
        return Result.success(proofService.listNormalProofByScheduleId(scheduleId));
    }

    @GetMapping("/replay-list/{scheduleId}")
    public Result<List<AdProof>> listReplayProof(@PathVariable Long scheduleId) {
        return Result.success(proofService.listReplayProofByScheduleId(scheduleId));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(proofService.removeById(id));
    }
}
