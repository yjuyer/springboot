package com.evrental.common.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.evrental.business.entity.CreditLog;
import com.evrental.business.mapper.CreditLogMapper;
import com.evrental.system.entity.SysUser;
import com.evrental.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 定时任务 - 每周信誉积分增加
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>每周一凌晨3点执行</li>
 *   <li>为所有正常用户增加1点信誉积分</li>
 *   <li>信誉积分上限为100分</li>
 *   <li>记录积分变更日志</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyCreditTask {

    private final SysUserMapper userMapper;
    private final CreditLogMapper creditLogMapper;

    /** 信誉积分上限 */
    private static final int MAX_CREDIT_SCORE = 100;

    /**
     * 每周日凌晨3点执行，为所有正常用户增加1点信誉积分
     */
    @Scheduled(cron = "0 0 3 ? * SUN")
    @Transactional(rollbackFor = Exception.class)
    public void weeklyCreditIncrease() {
        log.info("===== 开始执行每周信誉积分增加任务 =====");

        // 查询所有正常状态的用户
        List<SysUser> users = userMapper.selectList(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getStatus, 1)
                        .eq(SysUser::getDeleted, 0));

        int updatedCount = 0;
        for (SysUser user : users) {
            try {
                int currentScore = user.getCreditScore() != null ? user.getCreditScore() : 100;

                // 已达上限则跳过
                if (currentScore >= MAX_CREDIT_SCORE) {
                    continue;
                }

                // 计算新积分（不超过上限）
                int newScore = Math.min(currentScore + 1, MAX_CREDIT_SCORE);

                // 更新用户积分
                user.setCreditScore(newScore);
                userMapper.updateById(user);

                // 记录积分变更日志
                CreditLog creditLog = new CreditLog();
                creditLog.setUserId(user.getId());
                creditLog.setChangeType("加分");
                creditLog.setChangeAmount(1);
                creditLog.setBeforeScore(currentScore);
                creditLog.setAfterScore(newScore);
                creditLog.setReason("每周信誉积分奖励");
                creditLogMapper.insert(creditLog);

                updatedCount++;
                log.debug("用户信誉积分增加: userId={}, {} -> {}", user.getId(), currentScore, newScore);
            } catch (Exception e) {
                log.error("用户信誉积分增加失败: userId={}, error={}", user.getId(), e.getMessage());
            }
        }

        log.info("===== 每周信誉积分增加任务完成，本次共增加 {} 人 =====", updatedCount);
    }
}
