package com.ad.schedule.service.impl;

import com.ad.schedule.entity.AdScreen;
import com.ad.schedule.mapper.AdScreenMapper;
import com.ad.schedule.service.AdScreenService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdScreenServiceImpl extends ServiceImpl<AdScreenMapper, AdScreen> implements AdScreenService {

    @Override
    public List<AdScreen> listAllEnabled() {
        LambdaQueryWrapper<AdScreen> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdScreen::getStatus, 1)
                .orderByAsc(AdScreen::getScreenCode);
        return this.list(wrapper);
    }
}
