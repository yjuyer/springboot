package com.evrental.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * <p>对应数据库表：sys_user</p>
 *
 * <p>主要字段：</p>
 * <ul>
 *   <li>id - 用户ID（主键）</li>
 *   <li>username - 用户名（登录账号，唯一）</li>
 *   <li>password - 密码（BCrypt加密存储）</li>
 *   <li>realName - 真实姓名（实名认证后填写）</li>
 *   <li>phone - 手机号（唯一）</li>
 *   <li>email - 邮箱</li>
 *   <li>avatar - 头像URL</li>
 *   <li>idCard - 身份证号</li>
 *   <li>idCardFront - 身份证正面图片路径</li>
 *   <li>idCardBack - 身份证背面图片路径</li>
 *   <li>idCardVerified - 实名认证状态：0-未认证/待审核 1-已认证</li>
 *   <li>driverLicense - 驾驶证图片路径</li>
 *   <li>licenseVerified - 驾驶证审核状态：0-未上传 1-待审核 2-已通过 3-已拒绝</li>
 *   <li>licenseUpdateTime - 驾驶证最后更改时间（每半年只能更改一次）</li>
 *   <li>creditScore - 信用积分（默认100，低于80无法租车）</li>
 *   <li>status - 状态：0-禁用 1-启用</li>
 * </ul>
 */
@Data
@TableName("sys_user")
public class SysUser implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
    private String city;
    private String address;
    private String avatar;
    private String idCard;
    /** 身份证正面图片路径 */
    private String idCardFront;
    /** 身份证背面图片路径 */
    private String idCardBack;
    /** 实名认证: 0未认证 1已认证 */
    private Integer idCardVerified;
    private String driverLicense;
    /** 驾照审核: 0未上传 1待审核 2通过 3拒绝 */
    private Integer licenseVerified;
    /** 驾驶证最后更改时间 */
    private LocalDateTime licenseUpdateTime;
    private Integer creditScore;
    /** 会员等级：0-白银 1-黄金 2-白金 3-钻石 4-黑金 */
    private Integer memberLevel;
    /** 累计消费金额 */
    private java.math.BigDecimal totalSpent;
    /** 会员积分（订单金额1元=1积分，押金不算） */
    private Integer memberPoints;
    /** 本月已使用免费取消次数 */
    private Integer freeCancelCount;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    /** 非数据库字段 - 角色（从sys_user_role关联查询） */
    @TableField(exist = false)
    private String role;
}
