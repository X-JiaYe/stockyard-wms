package com.wms.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.result.PageResult;
import com.wms.common.result.Result;
import com.wms.system.entity.SysUser;
import com.wms.system.security.AuthContext;
import com.wms.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 系统用户 CRUD。
 */
@RestController
@RequestMapping("/sys/users")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;
    private final AuthContext authContext;

    @GetMapping
    public Result<PageResult<SysUser>> page(@RequestParam(defaultValue = "1") long pageNum,
                                            @RequestParam(defaultValue = "10") long pageSize,
                                            @RequestParam(required = false) String username) {
        authContext.requireAdmin();
        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            qw.like(SysUser::getUsername, username);
        }
        qw.orderByDesc(SysUser::getId);
        return Result.ok(PageResult.of(sysUserService.page(new Page<>(pageNum, pageSize), qw)));
    }

    @GetMapping("/{id}")
    public Result<SysUser> get(@PathVariable Long id) {
        authContext.requireAdmin();
        return Result.ok(sysUserService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody SysUser user) {
        authContext.requireAdmin();
        sysUserService.save(user);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysUser user) {
        authContext.requireAdmin();
        user.setId(id);
        sysUserService.updateById(user);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        authContext.requireAdmin();
        sysUserService.removeById(id);
        return Result.ok();
    }
}
