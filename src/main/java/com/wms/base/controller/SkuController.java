package com.wms.base.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.base.entity.Sku;
import com.wms.base.service.SkuService;
import com.wms.common.result.PageResult;
import com.wms.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/base/skus")
@RequiredArgsConstructor
public class SkuController {

    private final SkuService skuService;

    @GetMapping
    public Result<PageResult<Sku>> page(@RequestParam(defaultValue = "1") long pageNum,
                                        @RequestParam(defaultValue = "10") long pageSize,
                                        @RequestParam(required = false) String code) {
        LambdaQueryWrapper<Sku> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(code)) {
            qw.like(Sku::getCode, code);
        }
        qw.orderByDesc(Sku::getId);
        return Result.ok(PageResult.of(skuService.page(new Page<>(pageNum, pageSize), qw)));
    }

    @GetMapping("/{id}")
    public Result<Sku> get(@PathVariable Long id) {
        return Result.ok(skuService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Sku sku) {
        skuService.save(sku);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Sku sku) {
        sku.setId(id);
        skuService.updateById(sku);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        skuService.removeById(id);
        return Result.ok();
    }
}
