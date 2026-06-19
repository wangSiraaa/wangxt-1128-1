package com.ad.schedule.mapper;

import com.ad.schedule.entity.AdMaterialScreenAudit;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdMaterialScreenAuditMapper extends BaseMapper<AdMaterialScreenAudit> {

    @Select("SELECT msa.*, s.screen_code, s.screen_name, s.business_district " +
            "FROM ad_material_screen_audit msa " +
            "LEFT JOIN ad_screen s ON msa.screen_id = s.id " +
            "WHERE msa.material_id = #{materialId} AND msa.deleted = 0")
    List<AdMaterialScreenAudit> listByMaterialIdWithScreen(@Param("materialId") Long materialId);

    @Select("SELECT * FROM ad_material_screen_audit " +
            "WHERE material_id = #{materialId} AND screen_id = #{screenId} AND deleted = 0 " +
            "LIMIT 1")
    AdMaterialScreenAudit getByMaterialAndScreen(@Param("materialId") Long materialId,
                                                  @Param("screenId") Long screenId);
}
