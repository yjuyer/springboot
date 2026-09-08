package com.evrental.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evrental.business.entity.VehicleDispatch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VehicleDispatchMapper extends BaseMapper<VehicleDispatch> {

    @Select("<script>" +
            "SELECT d.*, v.model AS vehicle_model, fs.store_name AS from_store_name, ts.store_name AS to_store_name " +
            "FROM vehicle_dispatch d " +
            "LEFT JOIN vehicle v ON d.vehicle_id = v.id " +
            "LEFT JOIN store fs ON d.from_store_id = fs.id " +
            "LEFT JOIN store ts ON d.to_store_id = ts.id " +
            "WHERE d.deleted = 0 " +
            "<if test='status != null'> AND d.status = #{status}</if>" +
            " ORDER BY d.create_time DESC" +
            "</script>")
    IPage<VehicleDispatch> selectDispatchPage(Page<VehicleDispatch> page, @Param("status") Integer status);
}
