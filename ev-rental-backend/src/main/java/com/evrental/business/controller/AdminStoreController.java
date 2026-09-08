package com.evrental.business.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evrental.business.entity.Store;
import com.evrental.business.mapper.StoreMapper;
import com.evrental.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 门店管理（公开列表 + 管理员CRUD）
 */
@RestController
@RequiredArgsConstructor
public class AdminStoreController {

    private final StoreMapper storeMapper;

    /** 公开接口 - 门店列表（用户端筛选用） */
    @GetMapping("/api/store/list")
    public R<List<Store>> list() {
        return R.ok(storeMapper.selectList(
                new LambdaQueryWrapper<Store>().eq(Store::getStatus, 1)));
    }

    /** 管理员 - 分页查询门店 */
    @GetMapping("/api/admin/store/list")
    public R<IPage<Store>> adminList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String storeName) {
        Page<Store> page = new Page<>(pageNum, pageSize);
        return R.ok(storeMapper.selectPage(page,
                new LambdaQueryWrapper<Store>()
                        .like(storeName != null, Store::getStoreName, storeName)
                        .orderByDesc(Store::getCreateTime)));
    }

    @PostMapping("/api/admin/store/add")
    public R<Void> add(@RequestBody Store store) {
        storeMapper.insert(store);
        return R.ok();
    }

    @PutMapping("/api/admin/store/update")
    public R<Void> update(@RequestBody Store store) {
        storeMapper.updateById(store);
        return R.ok();
    }

    @DeleteMapping("/api/admin/store/delete/{id}")
    public R<Void> delete(@PathVariable Long id) {
        storeMapper.deleteById(id);
        return R.ok();
    }
}
