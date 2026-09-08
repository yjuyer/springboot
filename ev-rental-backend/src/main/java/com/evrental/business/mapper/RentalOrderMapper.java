package com.evrental.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evrental.business.entity.RentalOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RentalOrderMapper extends BaseMapper<RentalOrder> {

    @Select("<script>" +
            "SELECT o.*, v.model AS vehicle_model, v.image AS main_image_url, v.vehicle_status AS vehicle_status, " +
            "ps.store_name AS pickup_store_name, rs.store_name AS return_store_name, " +
            "u.username, u.phone " +
            "FROM rental_order o " +
            "LEFT JOIN vehicle v ON o.vehicle_id = v.id " +
            "LEFT JOIN store ps ON o.pickup_store_id = ps.id " +
            "LEFT JOIN store rs ON o.return_store_id = rs.id " +
            "LEFT JOIN sys_user u ON o.user_id = u.id " +
            "WHERE o.deleted = 0 " +
            "<if test='userId != null'> AND o.user_id = #{userId}</if>" +
            "<if test='orderStatus != null'> AND o.order_status = #{orderStatus}</if>" +
            "<if test='orderNo != null and orderNo != \"\"'> AND o.order_no LIKE CONCAT('%',#{orderNo},'%')</if>" +
            " ORDER BY o.create_time DESC" +
            "</script>")
    IPage<RentalOrder> selectOrderPage(Page<RentalOrder> page,
                                       @Param("userId") Long userId,
                                       @Param("orderStatus") Integer orderStatus,
                                       @Param("orderNo") String orderNo);

    @Select("SELECT o.*, v.model AS vehicle_model, v.image AS main_image_url, v.vehicle_status AS vehicle_status, " +
            "v.daily_price AS vehicle_daily_price, v.deposit AS vehicle_deposit, v.range_km, v.seat_count, v.color, " +
            "ps.store_name AS pickup_store_name, ps.address AS pickup_store_address, ps.phone AS pickup_store_phone, " +
            "rs.store_name AS return_store_name, rs.address AS return_store_address, rs.phone AS return_store_phone, " +
            "u.username, u.phone " +
            "FROM rental_order o " +
            "LEFT JOIN vehicle v ON o.vehicle_id = v.id " +
            "LEFT JOIN store ps ON o.pickup_store_id = ps.id " +
            "LEFT JOIN store rs ON o.return_store_id = rs.id " +
            "LEFT JOIN sys_user u ON o.user_id = u.id " +
            "WHERE o.deleted = 0 AND o.id = #{orderId}")
    RentalOrder selectOrderDetail(@Param("orderId") Long orderId);
}
