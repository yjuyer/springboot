package com.evrental.system.controller;

import com.evrental.common.result.R;
import com.evrental.common.security.LoginUser;
import com.evrental.system.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 会员控制器
 *
 * <p>提供以下接口：</p>
 * <ul>
 *   <li>GET /api/member/info - 获取当前用户会员信息</li>
 *   <li>GET /api/member/benefits - 获取会员权益列表</li>
 *   <li>GET /api/member/can-free-cancel - 检查是否可以免费取消</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /** 获取当前登录用户 */
    private LoginUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser) {
            return (LoginUser) auth.getPrincipal();
        }
        return null;
    }

    /**
     * 获取当前用户会员信息
     */
    @GetMapping("/info")
    public R<Map<String, Object>> getMemberInfo() {
        LoginUser loginUser = getCurrentUser();
        if (loginUser == null) {
            return R.error("请先登录");
        }
        Map<String, Object> info = memberService.getMemberInfo(loginUser.getUserId());
        return R.ok(info);
    }

    /**
     * 获取指定等级的权益列表
     */
    @GetMapping("/benefits")
    public R<Map<String, Object>> getBenefits(@RequestParam(defaultValue = "0") int level) {
        return R.ok(memberService.getBenefits(level));
    }

    /**
     * 检查是否可以免费取消
     */
    @GetMapping("/can-free-cancel")
    public R<Map<String, Object>> canFreeCancel() {
        LoginUser loginUser = getCurrentUser();
        if (loginUser == null) {
            return R.error("请先登录");
        }
        boolean canCancel = memberService.canFreeCancel(loginUser.getUserId());
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("canFreeCancel", canCancel);
        return R.ok(result);
    }
}
