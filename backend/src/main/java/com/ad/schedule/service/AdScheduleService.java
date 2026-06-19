package com.ad.schedule.service;

import com.ad.schedule.dto.ScheduleCreateDTO;
import com.ad.schedule.dto.ScheduleReplayDTO;
import com.ad.schedule.dto.ScheduleUpdateDTO;
import com.ad.schedule.entity.AdSchedule;
import com.ad.schedule.vo.ScheduleConflictVO;
import com.ad.schedule.vo.ScheduleDetailVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.util.List;

public interface AdScheduleService extends IService<AdSchedule> {
    AdSchedule createSchedule(ScheduleCreateDTO dto);

    AdSchedule updateSchedule(ScheduleUpdateDTO dto);

    void cancelSchedule(Long id, String operator);

    AdSchedule createReplaySchedule(ScheduleReplayDTO dto);

    ScheduleConflictVO detectConflictWithPlan(Long screenId, LocalDate playDate,
                                              java.time.LocalTime startTime, java.time.LocalTime endTime,
                                              Integer customerPriority, java.math.BigDecimal contractAmount,
                                              Long excludeId);

    Page<ScheduleDetailVO> pageDetail(Integer pageNum, Integer pageSize, Long screenId,
                                       LocalDate startDate, LocalDate endDate,
                                       Integer scheduleStatus, Integer proofStatus);

    ScheduleDetailVO getDetail(Long id);

    List<AdSchedule> listByScreenAndDate(Long screenId, LocalDate playDate);

    List<ScheduleDetailVO> listReplayByOriginId(Long originScheduleId);

    boolean checkConflict(Long screenId, LocalDate playDate,
                          java.time.LocalTime startTime, java.time.LocalTime endTime,
                          Long excludeId);
}
