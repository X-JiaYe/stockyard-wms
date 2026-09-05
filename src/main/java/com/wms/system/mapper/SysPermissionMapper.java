package com.wms.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.system.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限 Mapper。
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /** 查询用户通过角色间接拥有的权限编码集合。 */
    @Select("SELECT p.code FROM sys_permission p "
            + "JOIN sys_role_permission rp ON p.id = rp.permission_id "
            + "JOIN sys_user_role ur ON rp.role_id = ur.role_id "
            + "WHERE ur.user_id = #{userId} AND p.deleted = 0")
    List<String> selectCodesByUserId(@Param("userId") Long userId);
}
