package com.evrental.business.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.evrental.business.dto.CreateOrderDTO;
import com.evrental.business.dto.PayDTO;
import com.evrental.business.entity.RentalOrder;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 订单服务接口
 */
public interface RentalOrderService extends IService<RentalOrder> {

    IPage<RentalOrder> pageOrders(Long userId, Integer orderStatus, String orderNo, int pageNum, int pageSize);

    /** 获取订单详情（含车辆、门店信息） */
    RentalOrder getOrderDetail(Long orderId);

    RentalOrder createOrder(Long userId, CreateOrderDTO dto);

    void payOrder(Long userId, PayDTO dto);

    /** 取消订单（返回手续费信息） */
    Map<String, Object> cancelOrder(Long userId, Long orderId, String reason);

    /** 管理员确认取车 */
    void pickupVehicle(Long orderId, BigDecimal battery, BigDecimal mileage);

    /** 用户去取车 */
    void userPickup(Long userId, Long orderId);

    /** 用户申请还车 */
    void requestReturn(Long userId, Long orderId);

    /** 管理员确认还车 */
    void confirmReturn(Long orderId, BigDecimal battery, BigDecimal mileage);
}