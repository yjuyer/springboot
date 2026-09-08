package com.evrental.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evrental.business.entity.Vehicle;
import com.evrental.business.dto.VehicleQueryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface VehicleMapper extends BaseMapper<Vehicle> {

    @Select("SELECT v.*, COUNT(ro.id) AS order_count " +
            "FROM vehicle v " +
            "LEFT JOIN rental_order ro ON v.id = ro.vehicle_id " +
            "WHERE v.deleted = 0 AND v.vehicle_status = 0 " +
            "GROUP BY v.id " +
            "ORDER BY order_count DESC " +
            "LIMIT 6")
    List<Vehicle> selectHotVehicles();

    @Select("<script>" +
            "SELECT v.*, s.store_name AS store_name, " +
            "v.image AS main_image_url " +
            "FROM vehicle v LEFT JOIN store s ON v.store_id = s.id " +
            "WHERE v.deleted = 0 " +
            "<if test='q.brandId != null'> AND v.brand_id = #{q.brandId}</if>" +
            "<if test='q.vehicleType != null and q.vehicleType != \"\"'> AND v.vehicle_type = #{q.vehicleType}</if>" +
            "<if test='q.minPrice != null'> AND v.daily_price &gt;= #{q.minPrice}</if>" +
            "<if test='q.maxPrice != null'> AND v.daily_price &lt;= #{q.maxPrice}</if>" +
            "<if test='q.minRange != null'> AND v.range_km &gt;= #{q.minRange}</if>" +
            "<if test='q.maxRange != null'> AND v.range_km &lt;= #{q.maxRange}</if>" +
            "<if test='q.storeId != null'> AND v.store_id = #{q.storeId}</if>" +
            "<if test='q.vehicleStatus != null'> AND v.vehicle_status = #{q.vehicleStatus}</if>" +
            "<if test='q.keyword != null and q.keyword != \"\"'>" +
            " AND (v.model LIKE CONCAT('%',#{q.keyword},'%') OR v.license_plate LIKE CONCAT('%',#{q.keyword},'%'))" +
            "</if>" +
            " ORDER BY v.create_time DESC" +
            "</script>")
    IPage<Vehicle> selectVehiclePage(Page<Vehicle> page, @Param("q") VehicleQueryDTO query);
}
