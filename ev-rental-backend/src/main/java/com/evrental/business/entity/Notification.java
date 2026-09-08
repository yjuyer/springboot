package com.evrental.business.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户通知实体类
 *
 * <p>对应数据库表：notification</p>
 *
 * <p>类型说明：</p>
 * <ul>
 *   <li>1 - 订单通知（创建/支付/取车/还车/取消/超时）</li>
 *   <li>2 - 认证通知（实名认证/驾驶证审核结果）</li>
 *   <li>3 - 系统通知（公告/系统维护等）</li>
 *   <li>4 - 优惠通知（优惠券到账/到期提醒）</li>
 * </ul>
 */
@Data
@TableName("notification")
public class Notification implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收用户ID */
    private Long userId;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 类型: 1订单 2认证 3系统 4优惠 */
    private Integer type;

    /** 关联ID（订单ID/认证ID等） */
    private Long relatedId;

    /** 已读状态: 0未读 1已读 */
    private Integer isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
