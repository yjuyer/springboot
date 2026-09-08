package com.evrental.business.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * 支付请求DTO
 */
@Data
public class PayDTO {
    @NotNull(message = "订单ID不能为空")
    private Long orderId;
    /** 支付方式: 1支付宝 2微信 3银行卡 4余额 */
    @NotNull(message = "请选择支付方式")
    private Integer payType;
}
