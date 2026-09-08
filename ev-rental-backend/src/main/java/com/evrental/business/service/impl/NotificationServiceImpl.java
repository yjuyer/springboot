package com.evrental.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evrental.business.entity.Notification;
import com.evrental.business.mapper.NotificationMapper;
import com.evrental.business.service.NotificationService;
import com.evrental.common.config.NotificationHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户通知服务实现类
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>发送通知 - 写入数据库 + WebSocket实时推送</li>
 *   <li>查询通知 - 分页查询、未读计数</li>
 *   <li>标记已读 - 单条/全部</li>
 *   <li>删除通知</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final NotificationHandler notificationHandler;
    private final ObjectMapper objectMapper;

    @Override
    public void sendToUser(Long userId, String title, String content, int type, Long relatedId) {
        // 1. 写入数据库
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setRelatedId(relatedId);
        notification.setIsRead(0);
        notificationMapper.insert(notification);

        // 2. WebSocket实时推送
        try {
            Map<String, Object> wsMsg = new HashMap<>();
            wsMsg.put("type", "notification");
            wsMsg.put("id", notification.getId());
            wsMsg.put("title", title);
            wsMsg.put("content", content);
            wsMsg.put("notificationType", type);
            wsMsg.put("relatedId", relatedId);
            wsMsg.put("createTime", notification.getCreateTime());
            notificationHandler.sendToUser(userId, objectMapper.writeValueAsString(wsMsg));
        } catch (Exception e) {
            log.warn("WebSocket推送通知失败, userId={}, 错误: {}", userId, e.getMessage());
        }

        log.info("通知发送成功: userId={}, title={}, type={}", userId, title, type);
    }

    @Override
    public IPage<Notification> getMyNotifications(Long userId, Integer isRead, int pageNum, int pageSize) {
        Page<Notification> page = new Page<>(pageNum, pageSize);
        return notificationMapper.selectByUserIdPage(page, userId, isRead);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return notificationMapper.selectUnreadCount(userId);
    }

    @Override
    public void markAsRead(Long id, Long userId) {
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getId, id)
                .eq(Notification::getUserId, userId)
                .set(Notification::getIsRead, 1));
    }

    @Override
    public void markAllAsRead(Long userId) {
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1));
    }

    @Override
    public void deleteNotification(Long id, Long userId) {
        notificationMapper.delete(new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getId, id)
                .eq(Notification::getUserId, userId));
    }
}
