package com.wms.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.common.exception.BizException;
import com.wms.system.entity.SysRole;
import com.wms.system.entity.SysUser;
import com.wms.system.entity.SysUserRole;
import com.wms.system.mapper.SysRoleMapper;
import com.wms.system.mapper.SysUserMapper;
import com.wms.system.mapper.SysUserRoleMapper;
import com.wms.system.service.SysUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 系统用户 Service 实现：密码加密 + 角色绑定 + 仓库归属校验。
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private static final String ROLE_ADMIN = "ADMIN";

    private final PasswordEncoder passwordEncoder;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;

    public SysUserServiceImpl(PasswordEncoder passwordEncoder,
                              SysUserRoleMapper sysUserRoleMapper,
                              SysRoleMapper sysRoleMapper) {
        this.passwordEncoder = passwordEncoder;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
    }

    @Override
    @Transactional
    public boolean save(SysUser entity) {
        if (entity.getPassword() == null || entity.getPassword().isBlank()) {
            throw new BizException("密码不能为空");
        }
        List<String> roles = entity.getRoleCodes();
        if (roles == null || roles.isEmpty()) {
            throw new BizException("必须指定角色");
        }
        if (!roles.contains(ROLE_ADMIN) && entity.getWarehouseId() == null) {
            throw new BizException("非管理员用户必须绑定仓库");
        }
        entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        boolean saved = super.save(entity);
        bindRoles(entity.getId(), roles);
        return saved;
    }

    @Override
    @Transactional
    public boolean updateById(SysUser entity) {
        if (entity.getPassword() != null && !entity.getPassword().isBlank()) {
            entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        } else {
            entity.setPassword(null);
        }
        List<String> roles = entity.getRoleCodes();
        if (roles != null) {
            if (roles.isEmpty()) {
                throw new BizException("角色不能为空");
            }
            if (roles.contains(ROLE_ADMIN)) {
                entity.setWarehouseId(null); // 管理员全局，显式清空仓库
            } else {
                Long warehouseId = entity.getWarehouseId();
                if (warehouseId == null) {
                    SysUser db = getById(entity.getId());
                    warehouseId = db == null ? null : db.getWarehouseId();
                    entity.setWarehouseId(warehouseId);
                }
                if (warehouseId == null) {
                    throw new BizException("非管理员用户必须绑定仓库");
                }
            }
            bindRoles(entity.getId(), roles);
        } else if (entity.getWarehouseId() == null) {
            // 未传角色也未传仓库：回填现有仓库，避免 ALWAYS 策略误清空
            SysUser db = getById(entity.getId());
            entity.setWarehouseId(db == null ? null : db.getWarehouseId());
        }
        return super.updateById(entity);
    }

    private void bindRoles(Long userId, List<String> roleCodes) {
        sysUserRoleMapper.deleteByUserId(userId);
        for (String code : roleCodes) {
            SysRole role = sysRoleMapper.selectOne(
                    new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, code));
            if (role == null) {
                throw new BizException("角色不存在: " + code);
            }
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(role.getId());
            sysUserRoleMapper.insert(ur);
        }
    }
}
