package com.evrental.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evrental.business.entity.VehicleReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VehicleReviewMapper extends BaseMapper<VehicleReview> {

    @Select("<script>" +
            "SELECT r.*, u.username, v.model AS vehicle_model, s.store_name AS store_name " +
            "FROM vehicle_review r " +
            "LEFT JOIN sys_user u ON r.user_id = u.id " +
            "LEFT JOIN vehicle v ON r.vehicle_id = v.id " +
            "LEFT JOIN store s ON r.store_id = s.id " +
            "WHERE r.deleted = 0 " +
            "<if test='vehicleId != null'> AND r.vehicle_id = #{vehicleId}</if>" +
            "<if test='status != null'> AND r.status = #{status}</if>" +
            " ORDER BY r.create_time DESC" +
            "</script>")
    IPage<VehicleReview> selectReviewPage(Page<VehicleReview> page,
                                          @Param("vehicleId") Long vehicleId,
                                          @Param("status") Integer status);
}
