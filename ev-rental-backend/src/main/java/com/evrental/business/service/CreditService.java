package com.evrental.business.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evrental.business.entity.CreditLog;

public interface CreditService {

    void changeCredit(Long userId, Integer amount, String reason, Long orderId);

    IPage<CreditLog> pageLogs(Long userId, int pageNum, int pageSize);
}
