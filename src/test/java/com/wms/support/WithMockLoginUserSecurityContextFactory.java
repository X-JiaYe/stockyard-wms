package com.wms.support;

import com.wms.system.entity.SysUser;
import com.wms.system.security.LoginUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 构造以 {@link LoginUser} 为 principal 的认证上下文。
 */
public class WithMockLoginUserSecurityContextFactory
        implements WithSecurityContextFactory<WithMockLoginUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockLoginUser annotation) {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername(annotation.username());
        user.setStatus(1);
        if (!annotation.admin()) {
            user.setWarehouseId(annotation.warehouseId());
        }

        List<GrantedAuthority> auths = new ArrayList<>();
        for (String authority : annotation.authorities()) {
            auths.add(new SimpleGrantedAuthority(authority));
        }

        LoginUser loginUser = new LoginUser(user, auths);
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(loginUser, null, auths);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(token);
        return context;
    }
}
