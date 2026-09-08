package com.evrental.business.service;

import cn.hutool.core.util.IdUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.evrental.business.entity.PaymentRecord;
import com.evrental.business.entity.RentalOrder;
import com.evrental.business.entity.Vehicle;
import com.evrental.business.mapper.PaymentRecordMapper;
import com.evrental.business.mapper.RentalOrderMapper;
import com.evrental.business.mapper.VehicleMapper;
import com.evrental.common.config.AlipayConfig;
import com.evrental.common.enums.OrderStatusEnum;
import com.evrental.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlipayService {

    private final AlipayConfig alipayConfig;
    private final RentalOrderMapper orderMapper;
    private final PaymentRecordMapper paymentMapper;
    private final VehicleMapper vehicleMapper;
    private final QRCodeService qrCodeService;
    private final DepositService depositService;

    /**
     * 获取支付宝客户端
     */
    private AlipayClient getAlipayClient() {
        return new DefaultAlipayClient(
                alipayConfig.getGatewayUrl(),
                alipayConfig.getAppId(),
                alipayConfig.getPrivateKey(),
                alipayConfig.getFormat(),
                alipayConfig.getCharset(),
                alipayConfig.getAlipayPublicKey(),
                alipayConfig.getSignType()
        );
    }

    public Map<String, Object> createPayUrl(Long orderId) {
        RentalOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != OrderStatusEnum.PENDING_PAY.getCode()) {
            throw new BusinessException("订单状态异常，无法支付");
        }

        BigDecimal totalAmount = order.getTotalAmount().add(order.getDepositAmount());

        PaymentRecord payment = paymentMapper.selectOne(
                new LambdaQueryWrapper<PaymentRecord>()
                        .eq(PaymentRecord::getOrderId, orderId)
                        .eq(PaymentRecord::getPayStatus, 0));
        if (payment == null) {
            payment = new PaymentRecord();
            payment.setOrderId(order.getId());
            payment.setOrderNo(order.getOrderNo());
            payment.setUserId(order.getUserId());
            payment.setAmount(totalAmount);
            payment.setPayType(1);
            payment.setPayStatus(0);
            payment.setExpireTime(LocalDateTime.now().plusMinutes(15));
            paymentMapper.insert(payment);
        }

        Vehicle vehicle = vehicleMapper.selectById(order.getVehicleId());

        // 使用支付宝SDK生成当面付二维码
        String qrCodeUrl = generateAlipayQrCode(order.getOrderNo(), totalAmount, "新能源汽车租赁-" + order.getOrderNo());

        log.info("创建支付: orderNo={}, amount={}, qrCode={}", order.getOrderNo(), totalAmount, qrCodeUrl);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderId", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("vehicleName", vehicle != null ? vehicle.getModel() : "未知车辆");
        result.put("rentalDays", order.getRentalDays());
        result.put("dailyPrice", order.getDailyPrice());
        result.put("rentAmount", order.getTotalAmount());
        result.put("depositAmount", order.getDepositAmount());
        result.put("totalAmount", totalAmount);
        result.put("qrCodeUrl", qrCodeUrl);
        result.put("subject", "新能源汽车租赁-" + order.getOrderNo());
        return result;
    }

    /**
     * 调用支付宝当面付接口生成二维码
     */
    private String generateAlipayQrCode(String orderNo, BigDecimal amount, String subject) {
        try {
            AlipayClient alipayClient = getAlipayClient();
            AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
            request.setNotifyUrl(alipayConfig.getNotifyUrl());

            // 构建业务参数
            StringBuilder bizContent = new StringBuilder();
            bizContent.append("{");
            bizContent.append("\"out_trade_no\":\"").append(orderNo).append("\",");
            bizContent.append("\"total_amount\":\"").append(amount).append("\",");
            bizContent.append("\"subject\":\"").append(subject).append("\",");
            bizContent.append("\"timeout_express\":\"30m\"");
            bizContent.append("}");
            request.setBizContent(bizContent.toString());

            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                String qrCode = response.getQrCode();
                log.info("支付宝当面付二维码生成成功: orderNo={}, qrCode={}", orderNo, qrCode);
                // 生成二维码图片
                return qrCodeService.generateQRCode(qrCode);
            } else {
                log.error("支付宝当面付接口调用失败: code={}, msg={}, subMsg={}",
                        response.getCode(), response.getMsg(), response.getSubMsg());
                // 降级：生成本地二维码
                return generateFallbackQrCode(orderNo, amount);
            }
        } catch (AlipayApiException e) {
            log.error("支付宝SDK调用异常: {}", e.getMessage());
            // 降级：生成本地二维码
            return generateFallbackQrCode(orderNo, amount);
        }
    }

    /**
     * 降级方案：生成本地二维码（用于演示或SDK调用失败时）
     */
    private String generateFallbackQrCode(String orderNo, BigDecimal amount) {
        log.info("使用降级方案生成二维码: orderNo={}", orderNo);
        String qrContent = "alipay://pay?out_trade_no=" + orderNo + "&amount=" + amount;
        return qrCodeService.generateQRCode(qrContent);
    }

    public Map<String, Object> getPayDetail(String orderNo) {
        RentalOrder order = orderMapper.selectOne(
                new LambdaQueryWrapper<RentalOrder>().eq(RentalOrder::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        BigDecimal totalAmount = order.getTotalAmount().add(order.getDepositAmount());
        Vehicle vehicle = vehicleMapper.selectById(order.getVehicleId());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderId", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("orderStatus", order.getOrderStatus());
        result.put("vehicleName", vehicle != null ? vehicle.getModel() : "未知车辆");
        result.put("rentalDays", order.getRentalDays());
        result.put("dailyPrice", order.getDailyPrice());
        result.put("rentAmount", order.getTotalAmount());
        result.put("depositAmount", order.getDepositAmount());
        result.put("totalAmount", totalAmount);
        result.put("paid", order.getOrderStatus() >= OrderStatusEnum.PAID.getCode());
        return result;
    }

    public String generateQRCodeByOrderNo(String orderNo) {
        RentalOrder order = orderMapper.selectOne(
                new LambdaQueryWrapper<RentalOrder>().eq(RentalOrder::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != OrderStatusEnum.PENDING_PAY.getCode()) {
            throw new BusinessException("订单状态异常，无法生成支付二维码");
        }

        BigDecimal totalAmount = order.getTotalAmount().add(order.getDepositAmount());

        String qrContent = alipayConfig.getGatewayUrl()
                + "?out_trade_no=" + order.getOrderNo()
                + "&total_amount=" + totalAmount
                + "&subject=EV-" + order.getOrderNo();

        return qrCodeService.generateQRCode(qrContent);
    }

    public Map<String, Object> queryPayStatus(String orderNo) {
        RentalOrder order = orderMapper.selectOne(
                new LambdaQueryWrapper<RentalOrder>().eq(RentalOrder::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 如果订单还未支付，主动查询支付宝交易状态
        if (order.getOrderStatus() < OrderStatusEnum.PAID.getCode()) {
            checkAndSyncAlipayStatus(order);
        }

        boolean paid = order.getOrderStatus() >= OrderStatusEnum.PAID.getCode();
        String desc = OrderStatusEnum.of(order.getOrderStatus()).getDesc();

        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", order.getOrderNo());
        result.put("orderStatus", order.getOrderStatus());
        result.put("paid", paid);
        result.put("statusDesc", desc);
        return result;
    }

    /**
     * 主动查询支付宝交易状态并同步
     */
    private void checkAndSyncAlipayStatus(RentalOrder order) {
        try {
            AlipayClient alipayClient = getAlipayClient();
            com.alipay.api.request.AlipayTradeQueryRequest request = new com.alipay.api.request.AlipayTradeQueryRequest();
            request.setBizContent("{\"out_trade_no\":\"" + order.getOrderNo() + "\"}");

            com.alipay.api.response.AlipayTradeQueryResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                String tradeStatus = response.getTradeStatus();
                log.info("查询支付宝交易状态: orderNo={}, tradeStatus={}", order.getOrderNo(), tradeStatus);

                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                    // 交易成功，更新订单状态
                    BigDecimal totalAmount = new BigDecimal(response.getTotalAmount());
                    order.setOrderStatus(OrderStatusEnum.PAID.getCode());
                    order.setPayTime(LocalDateTime.now());
                    order.setPaidAmount(totalAmount);
                    orderMapper.updateById(order);

                    // 更新支付记录
                    PaymentRecord payment = paymentMapper.selectOne(
                            new LambdaQueryWrapper<PaymentRecord>()
                                    .eq(PaymentRecord::getOrderNo, order.getOrderNo())
                                    .eq(PaymentRecord::getPayStatus, 0));
                    if (payment != null) {
                        payment.setPayStatus(1);
                        payment.setTransactionNo(response.getTradeNo());
                        payment.setPayTime(LocalDateTime.now());
                        paymentMapper.updateById(payment);
                    }

                    // 冻结押金并转为待取车
                    depositService.freezeDeposit(order.getOrderNo());
                    orderMapper.update(null, new LambdaUpdateWrapper<RentalOrder>()
                            .eq(RentalOrder::getId, order.getId())
                            .set(RentalOrder::getOrderStatus, OrderStatusEnum.PENDING_PICKUP.getCode()));

                    log.info("主动同步支付宝支付成功: orderNo={}", order.getOrderNo());
                }
            }
        } catch (Exception e) {
            log.warn("查询支付宝交易状态失败: orderNo={}, error={}", order.getOrderNo(), e.getMessage());
        }
    }

    @Transactional
    public String handleNotify(Map<String, String> params) {
        log.info("收到支付宝异步回调: {}", params);

        String orderNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");
        BigDecimal totalAmount = new BigDecimal(params.get("total_amount"));

        RentalOrder order = orderMapper.selectOne(
                new LambdaQueryWrapper<RentalOrder>().eq(RentalOrder::getOrderNo, orderNo));
        if (order == null) return "fail";
        if (order.getOrderStatus() >= OrderStatusEnum.PAID.getCode()) return "success";

        BigDecimal expected = order.getTotalAmount().add(order.getDepositAmount());
        if (totalAmount.compareTo(expected) != 0) return "fail";

        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            order.setOrderStatus(OrderStatusEnum.PAID.getCode());
            order.setPayTime(LocalDateTime.now());
            order.setPaidAmount(totalAmount);
            orderMapper.updateById(order);

            PaymentRecord payment = paymentMapper.selectOne(
                    new LambdaQueryWrapper<PaymentRecord>()
                            .eq(PaymentRecord::getOrderNo, orderNo)
                            .eq(PaymentRecord::getPayStatus, 0));
            if (payment != null) {
                payment.setPayStatus(1);
                payment.setTransactionNo(tradeNo);
                payment.setPayTime(LocalDateTime.now());
                paymentMapper.updateById(payment);
            }
            depositService.freezeDeposit(orderNo);
            orderMapper.update(null, new LambdaUpdateWrapper<RentalOrder>()
                    .eq(RentalOrder::getId, order.getId())
                    .set(RentalOrder::getOrderStatus, OrderStatusEnum.PENDING_PICKUP.getCode()));
            return "success";
        }
        return "fail";
    }

    @Transactional
    public void confirmPay(Long orderId, Integer payType) {
        RentalOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getOrderStatus() != OrderStatusEnum.PENDING_PAY.getCode()) {
            throw new BusinessException("订单状态异常");
        }

        BigDecimal totalAmount = order.getTotalAmount().add(order.getDepositAmount());

        order.setOrderStatus(OrderStatusEnum.PAID.getCode());
        order.setPayTime(LocalDateTime.now());
        order.setPaidAmount(totalAmount);
        orderMapper.updateById(order);

        PaymentRecord payment = paymentMapper.selectOne(
                new LambdaQueryWrapper<PaymentRecord>()
                        .eq(PaymentRecord::getOrderId, orderId)
                        .eq(PaymentRecord::getPayStatus, 0));
        if (payment != null) {
            payment.setPayStatus(1);
            payment.setPayType(payType);
            payment.setTransactionNo("SIM_" + IdUtil.fastSimpleUUID());
            payment.setPayTime(LocalDateTime.now());
            paymentMapper.updateById(payment);
        }

        log.info("模拟支付成功: orderId={}, amount={}", orderId, totalAmount);

        depositService.freezeDeposit(order.getOrderNo());
        orderMapper.update(null, new LambdaUpdateWrapper<RentalOrder>()
                .eq(RentalOrder::getId, order.getId())
                .set(RentalOrder::getOrderStatus, OrderStatusEnum.PENDING_PICKUP.getCode()));
    }

    @Transactional
    public void refund(Long orderId) {
        PaymentRecord payment = paymentMapper.selectOne(
                new LambdaQueryWrapper<PaymentRecord>()
                        .eq(PaymentRecord::getOrderId, orderId)
                        .eq(PaymentRecord::getPayStatus, 1));
        if (payment != null) {
            payment.setPayStatus(3);
            paymentMapper.updateById(payment);
        }
        log.info("退款成功: orderId={}", orderId);
    }
}