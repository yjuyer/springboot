package com.evrental.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evrental.business.entity.VehicleRepair;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VehicleRepairMapper extends BaseMapper<VehicleRepair> {

    @Select("<script>" +
            "SELECT r.*, v.model AS vehicle_model " +
            "FROM repair_record r LEFT JOIN vehicle v ON r.vehicle_id = v.id " +
            "WHERE r.deleted = 0 " +
            "<if test='status != null'> AND r.status = #{status}</if>" +
            " ORDER BY r.create_time DESC" +
            "</script>")
    IPage<VehicleRepair> selectRepairPage(Page<VehicleRepair> page, @Param("status") Integer status);
}
