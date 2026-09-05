package com.wms.system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 登录响应。
 */
@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String username;
    private String nickname;
    private List<String> roles;
}
