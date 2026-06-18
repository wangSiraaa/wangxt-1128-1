package com.ad.schedule.controller;

import com.ad.schedule.common.Result;
import com.ad.schedule.dto.ScheduleCreateDTO;
import com.ad.schedule.dto.ScheduleUpdateDTO;
import com.ad.schedule.entity.AdSchedule;
import com.ad.schedule.service.AdScheduleService;
import com.ad.schedule.vo.ScheduleDetailVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/schedule")
public class AdScheduleController {

    @Autowired
    private AdScheduleService scheduleService;

    @PostMapping
    public Result<AdSchedule> create(@Validated @RequestBody ScheduleCreateDTO dto) {
        return Result.success(scheduleService.createSchedule(dto));
    }

    @PutMapping
    public Result<AdSchedule> update(@Validated @RequestBody ScheduleUpdateDTO dto) {
        return Result.success(scheduleService.updateSchedule(dto));
    }

    @PostMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id, @RequestParam String operator) {
        scheduleService.cancelSchedule(id, operator);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<Page<ScheduleDetailVO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize,
                                               @RequestParam(required = false) Long screenId,
                                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                               @RequestParam(required = false) Integer scheduleStatus,
                                               @RequestParam(required = false) Integer proofStatus) {
        return Result.success(scheduleService.pageDetail(pageNum, pageSize, screenId, startDate, endDate, scheduleStatus, proofStatus));
    }

    @GetMapping("/{id}")
    public Result<ScheduleDetailVO> getById(@PathVariable Long id) {
        return Result.success(scheduleService.getDetail(id));
    }

    @GetMapping("/list-by-screen-date")
    public Result<List<AdSchedule>> listByScreenAndDate(@RequestParam Long screenId,
                                                        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate playDate) {
        return Result.success(scheduleService.listByScreenAndDate(screenId, playDate));
    }

    @GetMapping("/check-conflict")
    public Result<Boolean> checkConflict(@RequestParam Long screenId,
                                         @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate playDate,
                                         @RequestParam @DateTimeFormat(pattern = "HH:mm:ss") LocalTime startTime,
                                         @RequestParam @DateTimeFormat(pattern = "HH:mm:ss") LocalTime endTime,
                                         @RequestParam(required = false) Long excludeId) {
        return Result.success(scheduleService.checkConflict(screenId, playDate, startTime, endTime, excludeId));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(scheduleService.removeById(id));
    }
}
