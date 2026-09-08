package com.evrental.common.statemachine;

import com.evrental.common.enums.OrderEventEnum;
import com.evrental.common.enums.OrderStatusEnum;
import com.evrental.common.enums.VehicleStatusEnum;
import com.evrental.common.exception.BusinessException;
import lombok.Data;

/**
 * 状态机上下文
 *
 * 封装一次状态转换的完整信息：
 * - 当前状态、事件、目标状态
 * - 关联的订单ID、车辆ID
 * - 转换前后的副作用操作
 *
 * @author ev-rental
 */
@Data
public class OrderTransitionContext {

    /** 订单ID */
    private Long orderId;

    /** 车辆ID */
    private Long vehicleId;

    /** 用户ID */
    private Long userId;

    /** 当前状态 */
    private OrderStatusEnum currentStatus;

    /** 触发事件 */
    private OrderEventEnum event;

    /** 目标状态（转换后） */
    private OrderStatusEnum targetStatus;

    /** 业务参数 */
    private java.math.BigDecimal battery;
    private java.math.BigDecimal mileage;
    private String reason;
    private Integer payType;

    /**
     * 执行状态校验并计算目标状态
     *
     * @param current 当前状态
     * @param event   事件
     * @return this（链式调用）
     */
    public OrderTransitionContext validateAndTransit(OrderStatusEnum current, OrderEventEnum event) {
        this.currentStatus = current;
        this.event = event;
        this.targetStatus = OrderMachine.transit(current, event);
        return this;
    }

    /**
     * 获取状态转换对应的车辆目标状态
     * 订单状态变更需要同步更新车辆状态
     *
     * @return 车辆应变更到的目标状态
     */
    public VehicleStatusEnum getVehicleTargetStatus() {
        if (targetStatus == null) {
            throw new BusinessException("请先执行状态校验");
        }

        switch (targetStatus) {
            case PAID:
                return VehicleStatusEnum.RESERVED;
            case RENTING:
                return VehicleStatusEnum.RENTING;
            case COMPLETED:
                return VehicleStatusEnum.IDLE;
            case CANCELLED:
            case REFUNDED:
                return VehicleStatusEnum.IDLE;
            default:
                return null;
        }
    }
}
