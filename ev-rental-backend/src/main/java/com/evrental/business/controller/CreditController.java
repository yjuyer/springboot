package com.evrental.business.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evrental.business.entity.CreditLog;
import com.evrental.business.service.CreditService;
import com.evrental.common.result.R;
import com.evrental.common.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 用户端信用记录 */
@RestController
@RequestMapping("/api/credit")
@RequiredArgsConstructor
public class CreditController {

    private final CreditService creditService;
    private final com.evrental.business.mapper.CreditRuleMapper creditRuleMapper;

    @GetMapping("/logs")
    public R<IPage<CreditLog>> logs(@AuthenticationPrincipal LoginUser user,
                                    @RequestParam(defaultValue = "1") int pageNum,
                                    @RequestParam(defaultValue = "20") int pageSize) {
        return R.ok(creditService.pageLogs(user.getUserId(), pageNum, pageSize));
    }

    @GetMapping("/rules")
    public R<java.util.List<com.evrental.business.entity.CreditRule>> rules() {
        return R.ok(creditRuleMapper.selectList(new LambdaQueryWrapper<com.evrental.business.entity.CreditRule>()
                .eq(com.evrental.business.entity.CreditRule::getStatus, 1)
                .eq(com.evrental.business.entity.CreditRule::getDeleted, 0)));
    }
}
