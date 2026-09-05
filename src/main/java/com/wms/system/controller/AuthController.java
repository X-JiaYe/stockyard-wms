package com.wms.system.controller;

import com.wms.common.result.Result;
import com.wms.system.dto.LoginRequest;
import com.wms.system.dto.LoginResponse;
import com.wms.system.entity.SysUser;
import com.wms.system.security.JwtUtil;
import com.wms.system.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口。
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        LoginUser loginUser = (LoginUser) auth.getPrincipal();
        SysUser user = loginUser.getSysUser();
        String token = jwtUtil.generateToken(user.getUsername());
        return Result.ok(new LoginResponse(token, user.getUsername(), user.getNickname()));
    }
}
