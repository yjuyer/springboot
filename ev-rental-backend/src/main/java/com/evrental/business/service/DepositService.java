package com.evrental.business.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evrental.business.entity.*;
import com.evrental.business.mapper.*;
import com.evrental.common.enums.DepositStatusEnum;
import com.evrental.common.enums.OrderStatusEnum;
import com.evrental.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositService extends ServiceImpl<DepositRecordMapper, DepositRecord> {

    private final DepositRecordMapper depositMapper;
    private final DepositRefundMapper refundMapper;
    private final RentalOrderMapper orderMapper;
    private final VehicleMapper vehicleMapper;

    @Transactional
    public void createDepositRecord(RentalOrder order) {
        DepositRecord record = new DepositRecord();
        record.setOrderId(order.getId());
        record.setOrderNo(order.getOrderNo());
        record.setUserId(order.getUserId());
        record.setVehicleId(order.getVehicleId());
        record.setAmount(order.getDepositAmount());
        record.setStatus(DepositStatusEnum.PENDING_PAY.getCode());
        depositMapper.insert(record);
        log.info("创建押金记录: orderNo={}, amount={}", order.getOrderNo(), order.getDepositAmount());
    }

    @Transactional
    public void freezeDeposit(String orderNo) {
        DepositRecord record = depositMapper.selectOne(
                new LambdaQueryWrapper<DepositRecord>().eq(DepositRecord::getOrderNo, orderNo));
        if (record == null) {
            log.warn("押金记录不存在: orderNo={}", orderNo);
            return;
        }
        if (record.getStatus() == DepositStatusEnum.PENDING_PAY.getCode()) {
            record.setStatus(DepositStatusEnum.FROZEN.getCode());
            record.setFreezeTime(LocalDateTime.now());
            depositMapper.updateById(record);
            log.info("押金冻结: orderNo={}, amount={}", orderNo, record.getAmount());
        }
    }

    /**
     * 发起退款：订单 已完成→退款中
     */
    @Transactional
    public void initiateRefund(Long orderId, Long operatorId) {
        RentalOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getOrderStatus() != OrderStatusEnum.COMPLETED.getCode()) {
            throw new BusinessException("订单状态异常，仅已完成订单可发起退款");
        }
        order.setOrderStatus(OrderStatusEnum.REFUNDING.getCode());
        orderMapper.updateById(order);
        log.info("发起退款: orderNo={}, operatorId={}", order.getOrderNo(), operatorId);
    }

    /**
     * 押金退款：执行实际退款，订单 退款中→已退款
     */
    @Transactional
    public void refundDeposit(Long orderId, Integer refundType, String reason, Long operatorId) {
        DepositRecord record = depositMapper.selectOne(
                new LambdaQueryWrapper<DepositRecord>().eq(DepositRecord::getOrderId, orderId));
        if (record == null) {
            throw new BusinessException("押金记录不存在");
        }
        if (record.getStatus() != DepositStatusEnum.FROZEN.getCode()) {
            throw new BusinessException("押金状态异常，当前状态: " + DepositStatusEnum.of(record.getStatus()).getDesc());
        }

        RentalOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getOrderStatus() != OrderStatusEnum.REFUNDING.getCode()) {
            throw new BusinessException("订单状态异常，当前不可执行退款");
        }

        record.setStatus(DepositStatusEnum.REFUNDED.getCode());
        record.setRefundTime(LocalDateTime.now());
        depositMapper.updateById(record);

        DepositRefund refund = new DepositRefund();
        refund.setDepositId(record.getId());
        refund.setOrderId(record.getOrderId());
        refund.setOrderNo(record.getOrderNo());
        refund.setUserId(record.getUserId());
        refund.setAmount(record.getAmount());
        refund.setRefundType(refundType != null ? refundType : 1);
        refund.setRefundReason(reason);
        refund.setOperatorId(operatorId);
        refundMapper.insert(refund);

        order.setOrderStatus(OrderStatusEnum.REFUNDED.getCode());
        orderMapper.updateById(order);

        log.info("押金退款成功: depositId={}, orderNo={}, amount={}", record.getId(), record.getOrderNo(), record.getAmount());
    }

    public IPage<DepositRecord> pageRecords(int pageNum, int pageSize, Integer status, String orderNo) {
        return depositMapper.selectDepositPage(new Page<>(pageNum, pageSize), status, orderNo);
    }

    public Map<String, Object> getDepositStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        long totalCount = depositMapper.selectCount(
                new LambdaQueryWrapper<DepositRecord>().eq(DepositRecord::getDeleted, 0));
        long frozenCount = depositMapper.selectCount(
                new LambdaQueryWrapper<DepositRecord>()
                        .eq(DepositRecord::getStatus, DepositStatusEnum.FROZEN.getCode())
                        .eq(DepositRecord::getDeleted, 0));
        long refundedCount = depositMapper.selectCount(
                new LambdaQueryWrapper<DepositRecord>()
                        .eq(DepositRecord::getStatus, DepositStatusEnum.REFUNDED.getCode())
                        .eq(DepositRecord::getDeleted, 0));

        BigDecimal frozenAmount = depositMapper.selectList(
                new LambdaQueryWrapper<DepositRecord>()
                        .eq(DepositRecord::getStatus, DepositStatusEnum.FROZEN.getCode())
                        .eq(DepositRecord::getDeleted, 0))
                .stream().map(DepositRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal refundedAmount = depositMapper.selectList(
                new LambdaQueryWrapper<DepositRecord>()
                        .eq(DepositRecord::getStatus, DepositStatusEnum.REFUNDED.getCode())
                        .eq(DepositRecord::getDeleted, 0))
                .stream().map(DepositRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.put("totalCount", totalCount);
        stats.put("frozenCount", frozenCount);
        stats.put("refundedCount", refundedCount);
        stats.put("frozenAmount", frozenAmount);
        stats.put("refundedAmount", refundedAmount);

        return stats;
    }

    public DepositRecord getByOrderNo(String orderNo) {
        return depositMapper.selectOne(
                new LambdaQueryWrapper<DepositRecord>().eq(DepositRecord::getOrderNo, orderNo));
    }
}