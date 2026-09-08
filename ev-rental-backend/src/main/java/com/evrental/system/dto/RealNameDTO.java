package com.evrental.system.dto;

import lombok.Data;

/**
 * 实名认证请求DTO
 */
@Data
public class RealNameDTO {
    /** 真实姓名 */
    private String realName;
    /** 身份证号 */
    private String idCard;
}
