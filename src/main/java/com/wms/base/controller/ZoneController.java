package com.wms.base.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.base.entity.Zone;
import com.wms.base.service.ZoneService;
import com.wms.common.result.PageResult;
import com.wms.common.result.Result;
import com.wms.system.security.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/base/zones")
@RequiredArgsConstructor
public class ZoneController {

    private final ZoneService zoneService;
    private final AuthContext authContext;

    @GetMapping
    public Result<PageResult<Zone>> page(@RequestParam(defaultValue = "1") long pageNum,
                                         @RequestParam(defaultValue = "10") long pageSize,
                                         @RequestParam(required = false) Long warehouseId) {
        warehouseId = authContext.scopeWarehouse(warehouseId);
        LambdaQueryWrapper<Zone> qw = new LambdaQueryWrapper<>();
        if (warehouseId != null) {
            qw.eq(Zone::getWarehouseId, warehouseId);
        }
        qw.orderByDesc(Zone::getId);
        return Result.ok(PageResult.of(zoneService.page(new Page<>(pageNum, pageSize), qw)));
    }

    @GetMapping("/{id}")
    public Result<Zone> get(@PathVariable Long id) {
        Zone zone = zoneService.getById(id);
        if (zone != null) {
            authContext.checkWarehouse(zone.getWarehouseId());
        }
        return Result.ok(zone);
    }

    @PostMapping
    public Result<Void> create(@RequestBody Zone zone) {
        authContext.requireAdmin();
        zoneService.save(zone);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Zone zone) {
        authContext.requireAdmin();
        zone.setId(id);
        zoneService.updateById(zone);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        authContext.requireAdmin();
        zoneService.removeById(id);
        return Result.ok();
    }
}
