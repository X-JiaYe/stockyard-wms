package com.wms.system.service.impl;

import com.wms.common.exception.BizException;
import com.wms.system.entity.SysUser;
import com.wms.system.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 系统用户单测：密码非空校验与加密存储（不覆盖已设密码）。
 */
@ExtendWith(MockitoExtension.class)
class SysUserServiceImplTest {

    @Mock
    private SysUserMapper sysUserMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    private SysUserServiceImpl sysUserService;

    @BeforeEach
    void setUp() {
        sysUserService = new SysUserServiceImpl(passwordEncoder);
        // 注入 MyBatis-Plus ServiceImpl 的 baseMapper，避免落库真实执行
        ReflectionTestUtils.setField(sysUserService, "baseMapper", sysUserMapper);
    }

    @Test
    @DisplayName("保存：空密码拒绝")
    void save_blankPassword_rejected() {
        SysUser user = new SysUser();
        user.setPassword("");
        assertThatThrownBy(() -> sysUserService.save(user))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("密码不能为空");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("保存：密码被编码后落库")
    void save_encodesPassword() {
        when(passwordEncoder.encode("secret")).thenReturn("ENCODED");
        SysUser user = new SysUser();
        user.setPassword("secret");

        sysUserService.save(user);

        assertThat(user.getPassword()).isEqualTo("ENCODED");
        verify(sysUserMapper).insert(user);
    }

    @Test
    @DisplayName("更新：不传密码则不覆盖")
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
    @DisplayName("更新：传密码则重新编码")
    void updateById_nonBlankPassword_reencodes() {
        when(passwordEncoder.encode("newpass")).thenReturn("ENC2");
        SysUser user = new SysUser();
        user.setId(1L);
        user.setPassword("newpass");

        sysUserService.updateById(user);

        assertThat(user.getPassword()).isEqualTo("ENC2");
        verify(sysUserMapper).updateById(user);
    }
}
