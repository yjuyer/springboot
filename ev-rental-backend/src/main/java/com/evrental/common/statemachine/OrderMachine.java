package com.evrental.common.statemachine;

import com.evrental.common.enums.OrderEventEnum;
import com.evrental.common.enums.OrderStatusEnum;
import com.evrental.common.exception.BusinessException;

import java.util.*;

/**
 * 订单状态机
 *
 *  待支付(0) → 已支付(1) → 待取车(2) → 租赁中(3) → 待还车(4) → 已完成(5) → 退款中(7) → 已退款(8)
 *                       ↘ 已取消(6)
 */
public class OrderMachine {

    private static final Map<String, OrderStatusEnum> TRANSITION_MAP = new LinkedHashMap<>();
    private static final Map<OrderStatusEnum, List<OrderEventEnum>> ALLOWED_EVENTS = new LinkedHashMap<>();

    static {
        addRule(OrderStatusEnum.PENDING_PAY, OrderEventEnum.PAY, OrderStatusEnum.PAID);
        addRule(OrderStatusEnum.PENDING_PAY, OrderEventEnum.CANCEL, OrderStatusEnum.CANCELLED);
        addRule(OrderStatusEnum.PENDING_PAY, OrderEventEnum.TIMEOUT, OrderStatusEnum.CANCELLED);

        addRule(OrderStatusEnum.PAID, OrderEventEnum.CANCEL, OrderStatusEnum.CANCELLED);
        addRule(OrderStatusEnum.PAID, OrderEventEnum.FREEZE_DEPOSIT, OrderStatusEnum.PENDING_PICKUP);
        addRule(OrderStatusEnum.PAID, OrderEventEnum.TIMEOUT, OrderStatusEnum.CANCELLED);

        addRule(OrderStatusEnum.PENDING_PICKUP, OrderEventEnum.PICKUP, OrderStatusEnum.RENTING);
        addRule(OrderStatusEnum.PENDING_PICKUP, OrderEventEnum.TIMEOUT, OrderStatusEnum.CANCELLED);

        addRule(OrderStatusEnum.RENTING, OrderEventEnum.RETURN_REQUEST, OrderStatusEnum.PENDING_RETURN);

        addRule(OrderStatusEnum.PENDING_RETURN, OrderEventEnum.RETURN_CONFIRM, OrderStatusEnum.COMPLETED);

        addRule(OrderStatusEnum.COMPLETED, OrderEventEnum.REFUND_INIT, OrderStatusEnum.REFUNDING);

        addRule(OrderStatusEnum.REFUNDING, OrderEventEnum.REFUND_COMPLETE, OrderStatusEnum.REFUNDED);
    }

    private static void addRule(OrderStatusEnum from, OrderEventEnum event, OrderStatusEnum to) {
        TRANSITION_MAP.put(buildKey(from, event), to);
        ALLOWED_EVENTS.computeIfAbsent(from, k -> new ArrayList<>()).add(event);
    }

    public static boolean canTransit(OrderStatusEnum currentStatus, OrderEventEnum event) {
        return TRANSITION_MAP.containsKey(buildKey(currentStatus, event));
    }

    public static OrderStatusEnum getNextStatus(OrderStatusEnum currentStatus, OrderEventEnum event) {
        return TRANSITION_MAP.get(buildKey(currentStatus, event));
    }

    public static OrderStatusEnum transit(OrderStatusEnum currentStatus, OrderEventEnum event) {
        OrderStatusEnum target = getNextStatus(currentStatus, event);
        if (target == null) {
            throw new BusinessException(String.format(
                    "非法的状态转换：当前状态[%s]不能执行[%s]操作",
                    currentStatus.getDesc(), event.getDesc()));
        }
        return target;
    }

    public static List<OrderEventEnum> getAllowedEvents(OrderStatusEnum status) {
        return ALLOWED_EVENTS.getOrDefault(status, Collections.emptyList());
    }

    public static List<String> getAllowedEventDescs(OrderStatusEnum status) {
        List<OrderEventEnum> events = getAllowedEvents(status);
        List<String> descs = new ArrayList<>();
        for (OrderEventEnum event : events) {
            descs.add(event.getDesc() + "（" + event.getActor() + "）");
        }
        return descs;
    }

    public static boolean isTerminal(OrderStatusEnum status) {
        return getAllowedEvents(status).isEmpty();
    }

    public static String printTransitionTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔══════════════════════════════════════════════════════════════════╗\n");
        sb.append("║                    订单状态转换规则表                             ║\n");
        sb.append("╠══════════════════════════════════════════════════════════════════╣\n");
        for (Map.Entry<String, OrderStatusEnum> entry : TRANSITION_MAP.entrySet()) {
            String[] parts = entry.getKey().split("_");
            int fromCode = Integer.parseInt(parts[0]);
            OrderStatusEnum from = OrderStatusEnum.of(fromCode);
            String eventName = parts[1];
            OrderStatusEnum to = entry.getValue();
            sb.append(String.format("║  %-12s + %-16s → %-12s  ║\n",
                    from.getDesc(), eventName, to.getDesc()));
        }
        sb.append("╚══════════════════════════════════════════════════════════════════╝\n");
        return sb.toString();
    }

    private static String buildKey(OrderStatusEnum status, OrderEventEnum event) {
        return status.getCode() + "_" + event.name();
    }
}