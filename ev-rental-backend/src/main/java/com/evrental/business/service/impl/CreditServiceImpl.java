package com.evrental.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evrental.business.entity.CreditLog;
import com.evrental.business.mapper.CreditLogMapper;
import com.evrental.business.service.CreditService;
import com.evrental.common.exception.BusinessException;
import com.evrental.system.entity.SysUser;
import com.evrental.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreditServiceImpl implements CreditService {

    private static final int MIN_SCORE = 0;
    private static final int MAX_SCORE = 100;

    private final SysUserMapper userMapper;
    private final CreditLogMapper creditLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeCredit(Long userId, Integer amount, String reason, Long orderId) {
        if (userId == null || amount == null || amount == 0) {
            return;
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        int before = user.getCreditScore() == null ? MAX_SCORE : user.getCreditScore();
        int after = Math.max(MIN_SCORE, Math.min(MAX_SCORE, before + amount));
        if (after == before) {
            return;
        }

        SysUser update = new SysUser();
        update.setId(userId);
        update.setCreditScore(after);
        userMapper.updateById(update);

        CreditLog log = new CreditLog();
        log.setUserId(userId);
        log.setChangeType(amount > 0 ? "加分" : "扣分");
        log.setChangeAmount(Math.abs(amount));
        log.setBeforeScore(before);
        log.setAfterScore(after);
        log.setReason(reason);
        log.setOrderId(orderId);
        creditLogMapper.insert(log);
    }

    @Override
    public IPage<CreditLog> pageLogs(Long userId, int pageNum, int pageSize) {
        return creditLogMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<CreditLog>()
                        .eq(CreditLog::getUserId, userId)
                        .eq(CreditLog::getDeleted, 0)
                        .orderByDesc(CreditLog::getCreateTime));
    }
}
