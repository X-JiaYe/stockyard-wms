package com.wms.system.service.impl;

import com.wms.common.exception.BizException;
import com.wms.system.entity.SysRole;
import com.wms.system.entity.SysUser;
import com.wms.system.entity.SysUserRole;
import com.wms.system.mapper.SysRoleMapper;
import com.wms.system.mapper.SysUserMapper;
import com.wms.system.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 系统用户单测：密码校验/加密、角色绑定、仓库归属校验（防 fail-open）。
 */
@ExtendWith(MockitoExtension.class)
class SysUserServiceImplTest {

    @Mock
    private SysUserMapper sysUserMapper;
    @Mock
    private SysUserRoleMapper sysUserRoleMapper;
    @Mock
    private SysRoleMapper sysRoleMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    private SysUserServiceImpl sysUserService;

    @BeforeEach
    void setUp() {
        sysUserService = new SysUserServiceImpl(passwordEncoder, sysUserRoleMapper, sysRoleMapper);
        ReflectionTestUtils.setField(sysUserService, "baseMapper", sysUserMapper);
    }

    private SysRole role(String code) {
        SysRole r = new SysRole();
        r.setId(2L);
        r.setCode(code);
        return r;
    }

    @Test
    @DisplayName("保存：空密码拒绝")
    void save_blankPassword_rejected() {
        SysUser user = new SysUser();
        user.setPassword("");
        user.setRoleCodes(List.of("ADMIN"));
        assertThatThrownBy(() -> sysUserService.save(user))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("密码不能为空");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("保存：未指定角色拒绝")
    void save_missingRoles_rejected() {
        SysUser user = new SysUser();
        user.setPassword("secret");
        assertThatThrownBy(() -> sysUserService.save(user))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("必须指定角色");
    }

    @Test
    @DisplayName("保存：非管理员未绑仓拒绝")
    void save_operatorWithoutWarehouse_rejected() {
        SysUser user = new SysUser();
        user.setPassword("secret");
        user.setRoleCodes(List.of("OPERATOR"));
        assertThatThrownBy(() -> sysUserService.save(user))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("非管理员用户必须绑定仓库");
        verify(sysUserMapper, never()).insert(any(SysUser.class));
    }

    @Test
    @DisplayName("保存：管理员成功并绑定角色")
    void save_admin_encodesPasswordAndBindsRole() {
        when(passwordEncoder.encode("secret")).thenReturn("ENCODED");
        when(sysRoleMapper.selectOne(any())).thenReturn(role("ADMIN"));
        SysUser user = new SysUser();
        user.setPassword("secret");
        user.setRoleCodes(List.of("ADMIN"));

        sysUserService.save(user);

        assertThat(user.getPassword()).isEqualTo("ENCODED");
        verify(sysUserMapper).insert(user);
        verify(sysUserRoleMapper).deleteByUserId(user.getId());
        verify(sysUserRoleMapper).insert(any(SysUserRole.class));
    }

    @Test
    @DisplayName("更新：不传密码不覆盖")
    void updateById_blankPassword_isIgnored() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setPassword(null);

        sysUserService.updateById(user);

        assertThat(user.getPassword()).isNull();
        verify(sysUserMapper).updateById(user);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("更新：传密码重新编码")
    void updateById_nonBlankPassword_reencodes() {
        when(passwordEncoder.encode("newpass")).thenReturn("ENC2");
        SysUser user = new SysUser();
        user.setId(1L);
        user.setPassword("newpass");

        sysUserService.updateById(user);

        assertThat(user.getPassword()).isEqualTo("ENC2");
        verify(sysUserMapper).updateById(user);
    }

    @Test
    @DisplayName("更新：非管理员未绑仓拒绝（降级保护）")
    void updateById_operatorWithoutWarehouse_rejected() {
        SysUser db = new SysUser();
        db.setWarehouseId(null);
        when(sysUserMapper.selectById(1L)).thenReturn(db);
        SysUser user = new SysUser();
        user.setId(1L);
        user.setRoleCodes(List.of("OPERATOR"));

        assertThatThrownBy(() -> sysUserService.updateById(user))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("非管理员用户必须绑定仓库");
        verify(sysUserMapper, never()).updateById(any(SysUser.class));
    }

    @Test
    @DisplayName("更新：改为管理员角色时清空仓库归属")
    void updateById_adminRole_clearsWarehouse() {
        when(sysRoleMapper.selectOne(any())).thenReturn(role("ADMIN"));
        SysUser user = new SysUser();
        user.setId(1L);
        user.setWarehouseId(5L); // 原为操作员绑定的仓库
        user.setRoleCodes(List.of("ADMIN"));

        sysUserService.updateById(user);

        assertThat(user.getWarehouseId()).isNull();
        verify(sysUserMapper).updateById(user);
    }

    @Test
    @DisplayName("更新：未传角色未传仓库时保留现有仓库")
    void updateById_noRolesNoWarehouse_keepsExisting() {
        SysUser db = new SysUser();
        db.setWarehouseId(3L);
        when(sysUserMapper.selectById(1L)).thenReturn(db);
        SysUser user = new SysUser();
        user.setId(1L);
        user.setNickname("新昵称");

        sysUserService.updateById(user);

        assertThat(user.getWarehouseId()).isEqualTo(3L);
        verify(sysUserMapper).updateById(user);
    }
}
