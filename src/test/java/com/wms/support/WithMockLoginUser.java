package com.wms.support;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注入 {@link com.wms.system.security.LoginUser} 作为认证主体的测试注解。
 * 用于集成测试中精确控制当前用户的仓库归属与权限点（替代 @WithMockUser，
 * 因为 AuthContext 只认 LoginUser 类型的 principal）。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@WithSecurityContext(factory = WithMockLoginUserSecurityContextFactory.class)
public @interface WithMockLoginUser {

    String username() default "tester";

    /** true 表示管理员（warehouse_id 为 NULL，可访问全仓）。 */
    boolean admin() default false;

    /** 普通用户的绑定仓库（admin=true 时忽略）。 */
    long warehouseId() default 1L;

    /** 权限点/角色，如 {"ROLE_ADMIN", "system:manage"}。 */
    String[] authorities() default {};
}
