package com.wms.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.common.exception.BizException;
import com.wms.system.entity.SysUser;
import com.wms.system.mapper.SysUserMapper;
import com.wms.system.service.SysUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 系统用户 Service 实现。
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final PasswordEncoder passwordEncoder;

    public SysUserServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean save(SysUser entity) {
        if (entity.getPassword() == null || entity.getPassword().isBlank()) {
            throw new BizException("密码不能为空");
        }
        entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        return super.save(entity);
    }

    @Override
    public boolean updateById(SysUser entity) {
        if (entity.getPassword() != null && !entity.getPassword().isBlank()) {
            entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        } else {
            entity.setPassword(null);
        }
        return super.updateById(entity);
    }
}
