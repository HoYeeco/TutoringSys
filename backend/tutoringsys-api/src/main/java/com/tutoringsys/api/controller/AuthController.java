package com.tutoringsys.api.controller;

import com.tutoringsys.common.response.R;
import com.tutoringsys.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "认证管理", description = "认证相关接口")
public class AuthController {

    @Resource
    private UserService userService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录接口")
    public R<?> login(@RequestBody LoginRequest request) {
        try {
            // 验证用户名和密码
            var user = userService.getUserByUsername(request.getUsername());
            if (user == null) {
                return R.error(401, "用户名或密码错误");
            }

            // 暂时使用明文密码验证，实际项目中应该使用加密密码
            if (!request.getPassword().equals(user.getPassword())) {
                return R.error(401, "用户名或密码错误");
            }

            // 生成token（暂时使用简单的token）
            String token = "token-" + System.currentTimeMillis();

            // 构建返回数据
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("realName", user.getRealName());
            userInfo.put("role", user.getRole());

            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("userInfo", userInfo);

            return R.ok(result);
        } catch (Exception e) {
            return R.error(500, "登录失败");
        }
    }

    // 登录请求DTO
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
