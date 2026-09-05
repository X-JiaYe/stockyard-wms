package com.wms.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.system.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色 Mapper。
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /** 查询用户拥有的角色编码集合。 */
    @Select("SELECT r.code FROM sys_role r JOIN sys_user_role ur ON r.id = ur.role_id "
            + "WHERE ur.user_id = #{userId} AND r.deleted = 0 AND r.status = 1")
    List<String> selectCodesByUserId(@Param("userId") Long userId);
}
