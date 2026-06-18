package com.ad.schedule.mapper;

import com.ad.schedule.entity.AdSchedule;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Mapper
public interface AdScheduleMapper extends BaseMapper<AdSchedule> {

    @Select("SELECT * FROM ad_schedule WHERE screen_id = #{screenId} AND play_date = #{playDate} " +
            "AND deleted = 0 AND schedule_status != 4 " +
            "AND id != #{excludeId} " +
            "AND ((start_time < #{endTime} AND end_time > #{startTime}))")
    List<AdSchedule> findConflictingSchedules(@Param("screenId") Long screenId,
                                              @Param("playDate") LocalDate playDate,
                                              @Param("startTime") LocalTime startTime,
                                              @Param("endTime") LocalTime endTime,
                                              @Param("excludeId") Long excludeId);
}
