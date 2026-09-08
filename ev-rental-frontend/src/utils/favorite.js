/**
 * 收藏工具 - 基于后端API
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>getFavorites() - 获取收藏列表（从后端API）</li>
 *   <li>isFavorited(vehicleId) - 检查是否已收藏（从后端API）</li>
 *   <li>toggleFavorite(vehicleId) - 切换收藏状态（调用后端API）</li>
 * </ul>
 */

import { favoriteApi } from '@/api/vehicle'

/**
 * 获取收藏列表
 * @returns {Promise<Array>} 收藏的车辆列表
 */
export async function getFavorites() {
  try {
    const res = await favoriteApi.getMyFavorites()
    return res.data || []
  } catch {
    return []
  }
}

/**
 * 检查是否已收藏某车辆
 * @param {number} vehicleId 车辆ID
 * @returns {Promise<boolean>} 是否已收藏
 */
export async function isFavorited(vehicleId) {
  try {
    const res = await favoriteApi.checkFavorite(vehicleId)
    return res.data?.favorited || false
  } catch {
    return false
  }
}

/**
 * 切换收藏状态（已收藏则取消，未收藏则添加）
 * @param {number} vehicleId 车辆ID
 * @returns {Promise<boolean>} 操作后是否已收藏
 */
export async function toggleFavorite(vehicleId) {
  try {
    const res = await favoriteApi.toggleFavorite(vehicleId)
    return res.data?.favorited || false
  } catch (e) {
    throw e
  }
}
