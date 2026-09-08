package com.evrental.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evrental.business.entity.UserFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户收藏Mapper接口
 */
@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {

    /**
     * 查询用户的收藏列表（关联车辆信息）
     */
    @Select("SELECT uf.*, v.model, v.image, v.daily_price, v.range_km, v.vehicle_status " +
            "FROM user_favorite uf " +
            "LEFT JOIN vehicle v ON uf.vehicle_id = v.id " +
            "WHERE uf.user_id = #{userId} AND v.deleted = 0 " +
            "ORDER BY uf.create_time DESC")
    List<UserFavorite> selectFavoritesWithVehicle(@Param("userId") Long userId);
}
