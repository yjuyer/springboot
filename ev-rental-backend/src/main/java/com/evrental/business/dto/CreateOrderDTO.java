package com.evrental.business.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 创建订单DTO
 */
@Data
public class CreateOrderDTO {
    @NotNull(message = "车辆ID不能为空")
    private Long vehicleId;
    @NotNull(message = "取车时间不能为空")
    private LocalDateTime pickupTime;
    @NotNull(message = "还车时间不能为空")
    private LocalDateTime returnTime;
    @NotNull(message = "取车门店不能为空")
    private Long pickupStoreId;
    @NotNull(message = "还车门店不能为空")
    private Long returnStoreId;
    /** 用户优惠券ID（可选） */
    private Long userCouponId;
    private String remark;
}
