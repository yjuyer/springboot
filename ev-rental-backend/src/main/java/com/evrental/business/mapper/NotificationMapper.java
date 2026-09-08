package com.evrental.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evrental.business.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户通知Mapper接口
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 查询用户未读通知数量
     */
    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = 0")
    int selectUnreadCount(@Param("userId") Long userId);

    /**
     * 分页查询用户通知（按时间倒序）
     *
     * @param page   分页参数
     * @param userId 用户ID
     * @param isRead 已读状态（null则查全部）
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT * FROM notification WHERE user_id = #{userId}" +
            "<if test='isRead != null'> AND is_read = #{isRead}</if>" +
            " ORDER BY create_time DESC" +
            "</script>")
    IPage<Notification> selectByUserIdPage(Page<Notification> page,
                                           @Param("userId") Long userId,
                                           @Param("isRead") Integer isRead);
}
