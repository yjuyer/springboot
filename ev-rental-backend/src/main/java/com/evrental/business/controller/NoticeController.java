package com.evrental.business.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evrental.business.entity.Notice;
import com.evrental.business.mapper.NoticeMapper;
import com.evrental.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告管理（公开 + 管理员CRUD）
 */
@RestController
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeMapper noticeMapper;

    /** 公开接口 - 已发布公告列表 */
    @GetMapping("/api/notice/list")
    public R<List<Notice>> list() {
        return R.ok(noticeMapper.selectList(
                new LambdaQueryWrapper<Notice>()
                        .eq(Notice::getStatus, 1)
                        .orderByDesc(Notice::getIsTop)
                        .orderByDesc(Notice::getPublishTime)));
    }

    @GetMapping("/api/notice/detail/{id}")
    public R<Notice> detail(@PathVariable Long id) {
        return R.ok(noticeMapper.selectById(id));
    }

    /** 管理员接口 */
    @GetMapping("/api/admin/notice/list")
    public R<IPage<Notice>> adminList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<Notice> page = new Page<>(pageNum, pageSize);
        return R.ok(noticeMapper.selectPage(page,
                new LambdaQueryWrapper<Notice>().orderByDesc(Notice::getCreateTime)));
    }

    @PostMapping("/api/admin/notice/add")
    public R<Void> add(@RequestBody Notice notice) {
        noticeMapper.insert(notice);
        return R.ok();
    }

    @PutMapping("/api/admin/notice/update")
    public R<Void> update(@RequestBody Notice notice) {
        noticeMapper.updateById(notice);
        return R.ok();
    }

    @DeleteMapping("/api/admin/notice/delete/{id}")
    public R<Void> delete(@PathVariable Long id) {
        noticeMapper.deleteById(id);
        return R.ok();
    }

    @PostMapping("/api/admin/notice/publish/{id}")
    public R<Void> publish(@PathVariable Long id) {
        Notice n = new Notice();
        n.setId(id);
        n.setStatus(1);
        n.setPublishTime(LocalDateTime.now());
        noticeMapper.updateById(n);
        return R.ok();
    }
}
