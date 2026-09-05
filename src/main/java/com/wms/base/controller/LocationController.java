package com.wms.base.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.base.entity.Location;
import com.wms.base.service.LocationService;
import com.wms.common.result.PageResult;
import com.wms.common.result.Result;
import com.wms.system.security.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/base/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;
    private final AuthContext authContext;

    @GetMapping
    public Result<PageResult<Location>> page(@RequestParam(defaultValue = "1") long pageNum,
                                             @RequestParam(defaultValue = "10") long pageSize,
                                             @RequestParam(required = false) Long warehouseId,
                                             @RequestParam(required = false) Long zoneId) {
        warehouseId = authContext.scopeWarehouse(warehouseId);
        LambdaQueryWrapper<Location> qw = new LambdaQueryWrapper<>();
        if (warehouseId != null) {
            qw.eq(Location::getWarehouseId, warehouseId);
        }
        if (zoneId != null) {
            qw.eq(Location::getZoneId, zoneId);
        }
        qw.orderByDesc(Location::getId);
        return Result.ok(PageResult.of(locationService.page(new Page<>(pageNum, pageSize), qw)));
    }

    @GetMapping("/{id}")
    public Result<Location> get(@PathVariable Long id) {
        Location location = locationService.getById(id);
        if (location != null) {
            authContext.checkWarehouse(location.getWarehouseId());
        }
        return Result.ok(location);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('base:manage')")
    public Result<Void> create(@RequestBody Location location) {
        locationService.save(location);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('base:manage')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Location location) {
        location.setId(id);
        locationService.updateById(location);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('base:manage')")
    public Result<Void> delete(@PathVariable Long id) {
        locationService.removeById(id);
        return Result.ok();
    }
}
