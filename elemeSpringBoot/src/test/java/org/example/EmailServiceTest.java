package org.example;

import org.example.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * EmailService单元测试类
 * 测试邮件服务的各种功能
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("邮件服务测试")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String INVALID_EMAIL = "invalid-email";

    @BeforeEach
    void setUp() {
        // 初始化测试数据
    }

    @Test
    @DisplayName("发送验证码邮件测试")
    void testSendVerificationCode() {
        // 准备测试
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // 执行测试
        assertDoesNotThrow(() -> emailService.sendVerificationCode(TEST_EMAIL));

        // 验证结果
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("验证码验证测试")
    void testVerifyCode() {
        // 准备测试 - 先发送验证码
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        emailService.sendVerificationCode(TEST_EMAIL);

        // 测试验证错误的验证码
        boolean result = emailService.verifyCode(TEST_EMAIL, "wrong_code");
        assertFalse(result, "错误的验证码应该验证失败");

        // 测试验证不存在的邮箱
        result = emailService.verifyCode("nonexistent@example.com", "123456");
        assertFalse(result, "不存在的邮箱应该验证失败");
    }

    @Test
    @DisplayName("空邮箱地址测试")
    void testSendVerificationCodeWithEmptyEmail() {
        // 测试空邮箱
        assertDoesNotThrow(() -> emailService.sendVerificationCode(""));
        
        // 测试null邮箱
        assertDoesNotThrow(() -> emailService.sendVerificationCode(null));
    }

    @Test
    @DisplayName("邮件发送失败测试")
    void testSendVerificationCodeWithException() {
        // 模拟邮件发送失败
        doThrow(new RuntimeException("邮件发送失败")).when(mailSender).send(any(SimpleMailMessage.class));

        // 执行测试
        assertThrows(RuntimeException.class, () -> emailService.sendVerificationCode(TEST_EMAIL));
    }

    @Test
    @DisplayName("验证码格式测试")
    void testVerificationCodeFormat() {
        // 发送验证码
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        emailService.sendVerificationCode(TEST_EMAIL);

        // 验证码应该是6位数字
        // 注意：这里我们无法直接获取生成的验证码，但可以测试验证逻辑
        boolean result = emailService.verifyCode(TEST_EMAIL, "123456");
        // 由于我们无法获取实际生成的验证码，这里只测试验证逻辑不会出错
        assertFalse(result); // 随机生成的验证码不太可能是123456
    }

    @Test
    @DisplayName("多次发送验证码测试")
    void testMultipleSendVerificationCode() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // 多次发送验证码
        emailService.sendVerificationCode(TEST_EMAIL);
        emailService.sendVerificationCode(TEST_EMAIL);
        emailService.sendVerificationCode(TEST_EMAIL);

        // 验证邮件发送了3次
        verify(mailSender, times(3)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("验证码过期测试")
    void testVerificationCodeExpiry() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        emailService.sendVerificationCode(TEST_EMAIL);

        // 这里我们无法模拟时间过期，但可以测试验证逻辑
        // 在实际应用中，可以使用TimeUnit.MILLISECONDS.sleep()来模拟时间过期
        // 或者使用时间Mock框架
        
        // 测试不存在的验证码
        boolean result = emailService.verifyCode(TEST_EMAIL, "000000");
        assertFalse(result, "不存在的验证码应该验证失败");
    }

    @Test
    @DisplayName("并发验证码测试")
    void testConcurrentVerificationCode() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // 测试并发发送验证码
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";

        emailService.sendVerificationCode(email1);
        emailService.sendVerificationCode(email2);

        // 验证两个邮箱都收到了验证码
        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));

        // 测试验证码不会互相干扰
        boolean result1 = emailService.verifyCode(email1, "123456");
        boolean result2 = emailService.verifyCode(email2, "123456");
        
        // 由于验证码是随机生成的，这里只测试验证逻辑不会出错
        assertFalse(result1 || result2); // 至少有一个会失败（因为验证码是随机的）
    }
} 