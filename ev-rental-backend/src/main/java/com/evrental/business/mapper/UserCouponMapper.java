package com.evrental.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evrental.business.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户优惠券Mapper
 */
@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {

    /**
     * 查询用户优惠券列表（关联优惠券表）
     */
    @Select("SELECT uc.*, c.coupon_name, c.coupon_type, c.discount_value, c.min_amount, " +
            "c.start_time, c.end_time, c.description " +
            "FROM user_coupon uc " +
            "LEFT JOIN coupon c ON uc.coupon_id = c.id " +
            "WHERE uc.user_id = #{userId} AND uc.deleted = 0 " +
            "ORDER BY uc.create_time DESC")
    List<UserCoupon> selectUserCoupons(@Param("userId") Long userId);

    /**
     * 查询用户可用优惠券列表（未使用且在有效期内）
     * 有效期取用户优惠券过期时间和优惠券模板结束时间中较早的那个
     */
    @Select("SELECT uc.*, c.coupon_name, c.coupon_type, c.discount_value, c.min_amount, " +
            "c.start_time, c.end_time, c.description " +
            "FROM user_coupon uc " +
            "LEFT JOIN coupon c ON uc.coupon_id = c.id " +
            "WHERE uc.user_id = #{userId} AND uc.status = 0 AND uc.deleted = 0 " +
            "AND c.status = 1 " +
            "AND (uc.expire_time IS NULL OR uc.expire_time > NOW()) " +
            "AND c.end_time > NOW() " +
            "ORDER BY c.discount_value DESC")
    List<UserCoupon> selectAvailableCoupons(@Param("userId") Long userId);
}
