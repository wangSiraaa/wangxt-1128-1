package com.ad.schedule.service;

import com.ad.schedule.entity.AdScreen;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AdScreenService extends IService<AdScreen> {
    List<AdScreen> listAllEnabled();
}
