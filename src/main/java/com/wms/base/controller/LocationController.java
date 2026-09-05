package com.wms.base.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.base.entity.Location;
import com.wms.base.service.LocationService;
import com.wms.common.result.PageResult;
import com.wms.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/base/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    public Result<PageResult<Location>> page(@RequestParam(defaultValue = "1") long pageNum,
                                             @RequestParam(defaultValue = "10") long pageSize,
                                             @RequestParam(required = false) Long warehouseId,
                                             @RequestParam(required = false) Long zoneId) {
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
        return Result.ok(locationService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Location location) {
        locationService.save(location);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Location location) {
        location.setId(id);
        locationService.updateById(location);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        locationService.removeById(id);
        return Result.ok();
    }
}
