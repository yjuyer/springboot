package com.evrental.common.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.evrental.business.entity.DepositRecord;
import com.evrental.business.entity.RentalOrder;
import com.evrental.business.mapper.DepositRecordMapper;
import com.evrental.business.mapper.RentalOrderMapper;
import com.evrental.business.service.DepositService;
import com.evrental.business.service.NotificationService;
import com.evrental.common.enums.DepositStatusEnum;
import com.evrental.common.enums.OrderStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务 - 押金自动退款
 * 还车满15天后自动退还押金
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AutoRefundTask {

    private final DepositRecordMapper depositMapper;
    private final RentalOrderMapper orderMapper;
    private final DepositService depositService;
    private final NotificationService notificationService;

    /** 系统自动退款的操作员ID */
    private static final Long SYSTEM_OPERATOR_ID = 0L;

    /**
     * 每天凌晨2点执行，检查已还车满15天的订单，自动退还押金
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void autoRefund() {
        log.info("===== 开始执行押金自动退款任务 =====");

        // 查询所有冻结状态的押金记录
        List<DepositRecord> frozenDeposits = depositMapper.selectList(
                new LambdaQueryWrapper<DepositRecord>()
                        .eq(DepositRecord::getStatus, DepositStatusEnum.FROZEN.getCode())
                        .eq(DepositRecord::getDeleted, 0));

        int refundedCount = 0;
        for (DepositRecord deposit : frozenDeposits) {
            try {
                RentalOrder order = orderMapper.selectById(deposit.getOrderId());
                if (order == null) {
                    continue;
                }

                // 只处理已完成（已还车）的订单，且还车时间超过15天
                if (order.getOrderStatus() != OrderStatusEnum.COMPLETED.getCode()) {
                    continue;
                }
                if (order.getActualReturnTime() == null) {
                    continue;
                }
                if (order.getActualReturnTime().plusDays(15).isAfter(LocalDateTime.now())) {
                    continue;
                }

                // 自动发起退款 → 执行退款
                depositService.initiateRefund(order.getId(), SYSTEM_OPERATOR_ID);
                depositService.refundDeposit(order.getId(), 1, "还车满15天自动退款", SYSTEM_OPERATOR_ID);

                refundedCount++;

                // 发送押金退还通知
                try {
                    notificationService.sendToUser(order.getUserId(), "押金已退还",
                            String.format("您的订单 %s 押金 ¥%s 已自动退还至原支付账户，感谢使用e租出行！", order.getOrderNo(), deposit.getAmount()),
                            1, order.getId());
                } catch (Exception ne) {
                    log.warn("发送押金退还通知失败: orderNo={}", order.getOrderNo());
                }

                log.info("押金自动退款成功: orderNo={}, 金额={}", order.getOrderNo(), deposit.getAmount());
            } catch (Exception e) {
                log.error("押金自动退款失败: orderId={}, error={}", deposit.getOrderId(), e.getMessage());
            }
        }

        log.info("===== 押金自动退款任务完成，本次共退款 {} 笔 =====", refundedCount);
    }
}
