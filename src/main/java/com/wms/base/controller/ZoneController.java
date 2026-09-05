package com.wms.base.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.base.entity.Zone;
import com.wms.base.service.ZoneService;
import com.wms.common.result.PageResult;
import com.wms.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/base/zones")
@RequiredArgsConstructor
public class ZoneController {

    private final ZoneService zoneService;

    @GetMapping
    public Result<PageResult<Zone>> page(@RequestParam(defaultValue = "1") long pageNum,
                                         @RequestParam(defaultValue = "10") long pageSize,
                                         @RequestParam(required = false) Long warehouseId) {
        LambdaQueryWrapper<Zone> qw = new LambdaQueryWrapper<>();
        if (warehouseId != null) {
            qw.eq(Zone::getWarehouseId, warehouseId);
        }
        qw.orderByDesc(Zone::getId);
        return Result.ok(PageResult.of(zoneService.page(new Page<>(pageNum, pageSize), qw)));
    }

    @GetMapping("/{id}")
    public Result<Zone> get(@PathVariable Long id) {
        return Result.ok(zoneService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Zone zone) {
        zoneService.save(zone);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Zone zone) {
        zone.setId(id);
        zoneService.updateById(zone);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        zoneService.removeById(id);
        return Result.ok();
    }
}
