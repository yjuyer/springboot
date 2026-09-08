package com.evrental.business.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evrental.business.entity.Notification;

/**
 * 用户通知服务接口
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>发送通知 - 写入数据库 + WebSocket实时推送</li>
 *   <li>查询通知 - 分页查询、未读计数</li>
 *   <li>标记已读 - 单条/全部</li>
 *   <li>删除通知</li>
 * </ul>
 */
public interface NotificationService {

    /**
     * 发送通知给指定用户（写库 + WebSocket推送）
     *
     * @param userId    接收用户ID
     * @param title     通知标题
     * @param content   通知内容
     * @param type      类型: 1订单 2认证 3系统 4优惠
     * @param relatedId 关联ID（订单ID等，可为null）
     */
    void sendToUser(Long userId, String title, String content, int type, Long relatedId);

    /**
     * 分页查询用户通知
     *
     * @param userId   用户ID
     * @param isRead   已读状态（null查全部，0查未读，1查已读）
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    IPage<Notification> getMyNotifications(Long userId, Integer isRead, int pageNum, int pageSize);

    /**
     * 获取用户未读通知数量
     */
    int getUnreadCount(Long userId);

    /**
     * 标记单条通知为已读
     */
    void markAsRead(Long id, Long userId);

    /**
     * 标记用户所有通知为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 删除通知
     */
    void deleteNotification(Long id, Long userId);
}
