package com.evrental.business.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.evrental.business.entity.UserFavorite;
import com.evrental.business.mapper.UserFavoriteMapper;
import com.evrental.common.result.R;
import com.evrental.common.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户收藏控制器
 *
 * <p>提供以下接口：</p>
 * <ul>
 *   <li>GET    /api/favorite/list           - 我的收藏列表</li>
 *   <li>POST   /api/favorite/toggle/{vehicleId} - 切换收藏状态</li>
 *   <li>GET    /api/favorite/check/{vehicleId}  - 检查是否已收藏</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final UserFavoriteMapper favoriteMapper;

    private LoginUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser) {
            return (LoginUser) auth.getPrincipal();
        }
        return null;
    }

    /**
     * 获取我的收藏列表
     */
    @GetMapping("/list")
    public R<List<UserFavorite>> getMyFavorites() {
        LoginUser user = getCurrentUser();
        if (user == null) {
            return R.error("请先登录");
        }
        List<UserFavorite> favorites = favoriteMapper.selectFavoritesWithVehicle(user.getUserId());
        return R.ok(favorites);
    }

    /**
     * 切换收藏状态（已收藏则取消，未收藏则添加）
     */
    @PostMapping("/toggle/{vehicleId}")
    public R<Map<String, Object>> toggleFavorite(@PathVariable Long vehicleId) {
        LoginUser user = getCurrentUser();
        if (user == null) {
            return R.error("请先登录");
        }

        // 检查是否已收藏
        UserFavorite existing = favoriteMapper.selectOne(
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, user.getUserId())
                        .eq(UserFavorite::getVehicleId, vehicleId));

        Map<String, Object> result = new HashMap<>();
        if (existing != null) {
            // 已收藏，取消收藏
            favoriteMapper.deleteById(existing.getId());
            result.put("favorited", false);
            result.put("message", "已取消收藏");
        } else {
            // 未收藏，添加收藏
            UserFavorite fav = new UserFavorite();
            fav.setUserId(user.getUserId());
            fav.setVehicleId(vehicleId);
            favoriteMapper.insert(fav);
            result.put("favorited", true);
            result.put("message", "已收藏");
        }

        return R.ok(result);
    }

    /**
     * 检查是否已收藏某车辆
     */
    @GetMapping("/check/{vehicleId}")
    public R<Map<String, Boolean>> checkFavorite(@PathVariable Long vehicleId) {
        LoginUser user = getCurrentUser();
        if (user == null) {
            Map<String, Boolean> result = new HashMap<>();
            result.put("favorited", false);
            return R.ok(result);
        }

        Long count = favoriteMapper.selectCount(
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, user.getUserId())
                        .eq(UserFavorite::getVehicleId, vehicleId));

        Map<String, Boolean> result = new HashMap<>();
        result.put("favorited", count > 0);
        return R.ok(result);
    }

    /**
     * 删除收藏
     */
    @DeleteMapping("/{id}")
    public R<Void> deleteFavorite(@PathVariable Long id) {
        LoginUser user = getCurrentUser();
        if (user == null) {
            return R.error("请先登录");
        }
        // 只能删除自己的收藏
        favoriteMapper.delete(
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getId, id)
                        .eq(UserFavorite::getUserId, user.getUserId()));
        return R.ok();
    }
}
