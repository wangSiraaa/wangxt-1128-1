package com.ad.schedule.controller;

import com.ad.schedule.common.Result;
import com.ad.schedule.entity.AdScreen;
import com.ad.schedule.service.AdScreenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/screen")
public class AdScreenController {

    @Autowired
    private AdScreenService screenService;

    @GetMapping("/list")
    public Result<List<AdScreen>> list() {
        return Result.success(screenService.list());
    }

    @GetMapping("/list-enabled")
    public Result<List<AdScreen>> listEnabled() {
        return Result.success(screenService.listAllEnabled());
    }

    @GetMapping("/{id}")
    public Result<AdScreen> getById(@PathVariable Long id) {
        return Result.success(screenService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody AdScreen screen) {
        return Result.success(screenService.save(screen));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody AdScreen screen) {
        return Result.success(screenService.updateById(screen));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(screenService.removeById(id));
    }
}
