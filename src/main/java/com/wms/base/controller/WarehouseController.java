package com.wms.base.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.base.entity.Warehouse;
import com.wms.base.service.WarehouseService;
import com.wms.common.result.PageResult;
import com.wms.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/base/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping
    public Result<PageResult<Warehouse>> page(@RequestParam(defaultValue = "1") long pageNum,
                                              @RequestParam(defaultValue = "10") long pageSize,
                                              @RequestParam(required = false) String code) {
        LambdaQueryWrapper<Warehouse> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(code)) {
            qw.like(Warehouse::getCode, code);
        }
        qw.orderByDesc(Warehouse::getId);
        return Result.ok(PageResult.of(warehouseService.page(new Page<>(pageNum, pageSize), qw)));
    }

    @GetMapping("/{id}")
    public Result<Warehouse> get(@PathVariable Long id) {
        return Result.ok(warehouseService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Warehouse warehouse) {
        warehouseService.save(warehouse);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Warehouse warehouse) {
        warehouse.setId(id);
        warehouseService.updateById(warehouse);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        warehouseService.removeById(id);
        return Result.ok();
    }
}
