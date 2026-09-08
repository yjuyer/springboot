package com.evrental.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evrental.business.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券Mapper
 */
@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {
}
