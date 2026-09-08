package com.evrental.business.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evrental.business.entity.Notification;
import com.evrental.business.service.NotificationService;
import com.evrental.common.result.R;
import com.evrental.common.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户通知控制器
 *
 * <p>提供用户通知的查询、标记已读、删除等接口</p>
 */
@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    private LoginUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser) {
            return (LoginUser) auth.getPrincipal();
        }
        return null;
    }

    /**
     * 获取我的通知列表（分页）
     *
     * @param isRead   已读状态筛选（null查全部，0未读，1已读）
     * @param pageNum  页码
     * @param pageSize 每页大小
     */
    @GetMapping("/api/notification/my")
    public R<IPage<Notification>> getMyNotifications(
            @RequestParam(required = false) Integer isRead,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = getCurrentUser().getUserId();
        return R.ok(notificationService.getMyNotifications(userId, isRead, pageNum, pageSize));
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/api/notification/unread-count")
    public R<Map<String, Integer>> getUnreadCount() {
        Long userId = getCurrentUser().getUserId();
        Map<String, Integer> result = new HashMap<>();
        result.put("count", notificationService.getUnreadCount(userId));
        return R.ok(result);
    }

    /**
     * 标记单条通知为已读
     */
    @PutMapping("/api/notification/read/{id}")
    public R<Void> markAsRead(@PathVariable Long id) {
        Long userId = getCurrentUser().getUserId();
        notificationService.markAsRead(id, userId);
        return R.ok();
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/api/notification/read-all")
    public R<Void> markAllAsRead() {
        Long userId = getCurrentUser().getUserId();
        notificationService.markAllAsRead(userId);
        return R.ok();
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/api/notification/{id}")
    public R<Void> deleteNotification(@PathVariable Long id) {
        Long userId = getCurrentUser().getUserId();
        notificationService.deleteNotification(id, userId);
        return R.ok();
    }
}
