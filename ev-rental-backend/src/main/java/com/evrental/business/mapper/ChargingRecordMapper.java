package com.evrental.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evrental.business.entity.ChargingRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ChargingRecordMapper extends BaseMapper<ChargingRecord> {

    @Select("<script>" +
            "SELECT c.*, v.model AS vehicle_model " +
            "FROM charging_record c LEFT JOIN vehicle v ON c.vehicle_id = v.id " +
            "WHERE c.deleted = 0 " +
            "<if test='status != null'> AND c.status = #{status}</if>" +
            " ORDER BY c.create_time DESC" +
            "</script>")
    IPage<ChargingRecord> selectChargingPage(Page<ChargingRecord> page, @Param("status") Integer status);
}
