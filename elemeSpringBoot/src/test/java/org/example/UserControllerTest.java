package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.controller.UserController;
import org.example.entity.User;
import org.example.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController单元测试类
 * 测试用户控制器的各种功能
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.sql.init.mode=never"
})
@DisplayName("用户控制器测试")
class UserControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private IUserService userService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Nested
    @DisplayName("用户登录测试")
    class LoginTest {

        @Test
        @DisplayName("成功登录测试")
        void testLoginSuccess() throws Exception {
            // 准备测试数据
            Map<String, String> loginData = new HashMap<>();
            loginData.put("phoneNumber", "13812345678");
            loginData.put("password", "Test123");

            // Mock服务调用
            when(userService.login("13812345678", "Test123")).thenReturn(testUser);

            // 执行测试
            mockMvc.perform(post("/user/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginData)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("登录成功"))
                    .andExpect(jsonPath("$.user.phoneNumber").value("13812345678"))
                    .andExpect(jsonPath("$.user.name").value("张三"))
                    .andExpect(jsonPath("$.token").exists());

            // 验证服务调用
            verify(userService, times(1)).login("13812345678", "Test123");
        }

        @Test
        @DisplayName("登录失败测试 - 错误的凭据")
        void testLoginFailureInvalidCredentials() throws Exception {
            // 准备测试数据
            Map<String, String> loginData = new HashMap<>();
            loginData.put("phoneNumber", "13812345678");
            loginData.put("password", "wrongpassword");

            // Mock服务调用返回null表示登录失败
            when(userService.login("13812345678", "wrongpassword")).thenReturn(null);

            // 执行测试
            mockMvc.perform(post("/user/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginData)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(401))
                    .andExpect(jsonPath("$.message").value("用户名或密码错误"));

            // 验证服务调用
            verify(userService, times(1)).login("13812345678", "wrongpassword");
        }

        @Test
        @DisplayName("登录失败测试 - 缺少手机号")
        void testLoginFailureMissingPhoneNumber() throws Exception {
            // 准备测试数据
            Map<String, String> loginData = new HashMap<>();
            loginData.put("password", "Test123");

            // 执行测试
            mockMvc.perform(post("/user/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginData)))
                    .andExpect(status().isBadRequest());

            // 验证服务未被调用
            verify(userService, never()).login(any(), any());
        }

        @Test
        @DisplayName("登录失败测试 - 缺少密码")
        void testLoginFailureMissingPassword() throws Exception {
            // 准备测试数据
            Map<String, String> loginData = new HashMap<>();
            loginData.put("phoneNumber", "13812345678");

            // 执行测试
            mockMvc.perform(post("/user/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginData)))
                    .andExpect(status().isBadRequest());

            // 验证服务未被调用
            verify(userService, never()).login(any(), any());
        }

        @Test
        @DisplayName("登录异常测试")
        void testLoginException() throws Exception {
            // 准备测试数据
            Map<String, String> loginData = new HashMap<>();
            loginData.put("phoneNumber", "13812345678");
            loginData.put("password", "Test123");

            // Mock服务抛出异常
            when(userService.login("13812345678", "Test123"))
                    .thenThrow(new RuntimeException("数据库连接错误"));

            // 执行测试
            mockMvc.perform(post("/user/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginData)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("服务器错误：数据库连接错误"));

            // 验证服务调用
            verify(userService, times(1)).login("13812345678", "Test123");
        }
    }

    @Nested
    @DisplayName("发送验证码测试")
    class SendVerificationCodeTest {

        @Test
        @DisplayName("成功发送验证码测试")
        void testSendVerificationCodeSuccess() throws Exception {
            // 准备测试数据
            Map<String, String> requestData = new HashMap<>();
            requestData.put("email", "test@example.com");

            // Mock服务调用
            doNothing().when(emailService).sendVerificationCode("test@example.com");

            // 执行测试
            mockMvc.perform(post("/user/sendVerifyCode")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("验证码已发送"));

            // 验证服务调用
            verify(emailService, times(1)).sendVerificationCode("test@example.com");
        }

        @Test
        @DisplayName("发送验证码失败测试 - 空邮箱")
        void testSendVerificationCodeEmptyEmail() throws Exception {
            // 准备测试数据
            Map<String, String> requestData = new HashMap<>();
            requestData.put("email", "");

            // 执行测试
            mockMvc.perform(post("/user/sendVerifyCode")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("邮箱不能为空"));

            // 验证服务未被调用
            verify(emailService, never()).sendVerificationCode(any());
        }

        @Test
        @DisplayName("发送验证码失败测试 - 缺少邮箱字段")
        void testSendVerificationCodeMissingEmail() throws Exception {
            // 准备测试数据
            Map<String, String> requestData = new HashMap<>();

            // 执行测试
            mockMvc.perform(post("/user/sendVerifyCode")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("邮箱不能为空"));

            // 验证服务未被调用
            verify(emailService, never()).sendVerificationCode(any());
        }

        @Test
        @DisplayName("发送验证码异常测试")
        void testSendVerificationCodeException() throws Exception {
            // 准备测试数据
            Map<String, String> requestData = new HashMap<>();
            requestData.put("email", "test@example.com");

            // Mock服务抛出异常
            doThrow(new RuntimeException("邮件服务不可用"))
                    .when(emailService).sendVerificationCode("test@example.com");

            // 执行测试
            mockMvc.perform(post("/user/sendVerifyCode")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string("发送验证码失败"));

            // 验证服务调用
            verify(emailService, times(1)).sendVerificationCode("test@example.com");
        }
    }

    @Nested
    @DisplayName("用户注册测试")
    class RegisterTest {

        @Test
        @DisplayName("成功注册测试")
        void testRegisterSuccess() throws Exception {
            // 准备测试数据
            Map<String, String> registerData = new HashMap<>();
            registerData.put("phoneNumber", "13812345678");
            registerData.put("password", "Test123");
            registerData.put("confirmPassword", "Test123");
            registerData.put("name", "张三");
            registerData.put("email", "test@example.com");
            registerData.put("verifyCode", "123456");

            // Mock服务调用
            when(emailService.verifyCode("test@example.com", "123456")).thenReturn(true);
            when(userService.register("13812345678", "Test123", "Test123", "张三", "test@example.com"))
                    .thenReturn(1); // 1表示注册成功

            // 执行测试
            mockMvc.perform(post("/user/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerData)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("1"));

            // 验证服务调用
            verify(emailService, times(1)).verifyCode("test@example.com", "123456");
            verify(userService, times(1)).register("13812345678", "Test123", "Test123", "张三", "test@example.com");
        }

        @Test
        @DisplayName("注册失败测试 - 验证码错误")
        void testRegisterFailureInvalidVerifyCode() throws Exception {
            // 准备测试数据
            Map<String, String> registerData = new HashMap<>();
            registerData.put("phoneNumber", "13812345678");
            registerData.put("password", "Test123");
            registerData.put("confirmPassword", "Test123");
            registerData.put("name", "张三");
            registerData.put("email", "test@example.com");
            registerData.put("verifyCode", "wrong123");

            // Mock服务调用
            when(emailService.verifyCode("test@example.com", "wrong123")).thenReturn(false);

            // 执行测试
            mockMvc.perform(post("/user/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerData)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("-1")); // -1表示验证码错误

            // 验证服务调用
            verify(emailService, times(1)).verifyCode("test@example.com", "wrong123");
            verify(userService, never()).register(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("注册失败测试 - 用户已存在")
        void testRegisterFailureUserExists() throws Exception {
            // 准备测试数据
            Map<String, String> registerData = new HashMap<>();
            registerData.put("phoneNumber", "13812345678");
            registerData.put("password", "Test123");
            registerData.put("confirmPassword", "Test123");
            registerData.put("name", "张三");
            registerData.put("email", "test@example.com");
            registerData.put("verifyCode", "123456");

            // Mock服务调用
            when(emailService.verifyCode("test@example.com", "123456")).thenReturn(true);
            when(userService.register("13812345678", "Test123", "Test123", "张三", "test@example.com"))
                    .thenReturn(0); // 0表示用户已存在

            // 执行测试
            mockMvc.perform(post("/user/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerData)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("0"));

            // 验证服务调用
            verify(emailService, times(1)).verifyCode("test@example.com", "123456");
            verify(userService, times(1)).register("13812345678", "Test123", "Test123", "张三", "test@example.com");
        }
    }

    @Test
    @DisplayName("跨域配置测试")
    void testCorsConfiguration() throws Exception {
        // 测试OPTIONS请求（预检请求）
        mockMvc.perform(options("/user/login")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk());
    }
} 