package org.example;

import org.example.entity.User;
import org.example.service.EmailService;
import org.example.util.ValidationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 集成测试类
 * 测试各个组件之间的集成和协作
 */
@DisplayName("集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IntegrationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @Order(1)
    @DisplayName("用户注册流程集成测试")
    void testUserRegistrationFlow() {
        // 1. 验证手机号
        String phoneNumber = "13812345678";
        ValidationUtils.ValidationResult phoneResult = ValidationUtils.validatePhoneNumber(phoneNumber);
        assertTrue(phoneResult.isSuccess(), "手机号验证应该通过");

        // 2. 验证密码
        String password = "Test123";
        ValidationUtils.ValidationResult passwordResult = ValidationUtils.validatePassword(password);
        assertTrue(passwordResult.isSuccess(), "密码验证应该通过");

        // 3. 验证邮箱
        String email = "test@example.com";
        ValidationUtils.ValidationResult emailResult = ValidationUtils.validateEmail(email);
        assertTrue(emailResult.isSuccess(), "邮箱验证应该通过");

        // 4. 验证用户名
        String username = "张三";
        ValidationUtils.ValidationResult usernameResult = ValidationUtils.validateUsername(username);
        assertTrue(usernameResult.isSuccess(), "用户名验证应该通过");

        // 5. 验证性别
        String gender = "男";
        ValidationUtils.ValidationResult genderResult = ValidationUtils.validateGender(gender);
        assertTrue(genderResult.isSuccess(), "性别验证应该通过");

        // 6. 创建用户对象并验证
        User user = new User();
        user.setPhoneNumber(phoneNumber);
        user.setPassword(password);
        user.setName(username);
        user.setEmail(email);
        user.setGender(gender);
        user.setLoginAttempts(0);
        user.setAccountLocked(false);

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "用户对象验证应该通过");
    }

    @Test
    @Order(2)
    @DisplayName("用户验证失败场景集成测试")
    void testUserValidationFailureScenarios() {
        // 测试各种验证失败的组合场景

        // 场景1：手机号格式错误
        ValidationUtils.ValidationResult result1 = ValidationUtils.validatePhoneNumber("12812345678");
        assertFalse(result1.isSuccess());
        assertEquals("手机号格式不正确，应以1开头且第二位为3-9", result1.getMessage());

        // 场景2：密码过于简单
        ValidationUtils.ValidationResult result2 = ValidationUtils.validatePassword("123456");
        assertFalse(result2.isSuccess());
        assertTrue(result2.getMessage().contains("密码必须包含字母和数字"));

        // 场景3：邮箱格式错误
        ValidationUtils.ValidationResult result3 = ValidationUtils.validateEmail("invalid-email");
        assertFalse(result3.isSuccess());
        assertEquals("邮箱格式不正确", result3.getMessage());

        // 场景4：用户名包含非法字符
        ValidationUtils.ValidationResult result4 = ValidationUtils.validateUsername("user@123");
        assertFalse(result4.isSuccess());
        assertTrue(result4.getMessage().contains("用户名只能包含中文、英文、数字和下划线"));

        // 场景5：性别无效
        ValidationUtils.ValidationResult result5 = ValidationUtils.validateGender("其他");
        assertFalse(result5.isSuccess());
        assertEquals("性别只能是'男'、'女'或'未知'", result5.getMessage());
    }

    @Test
    @Order(3)
    @DisplayName("边界值测试集成")
    void testBoundaryValueIntegration() {
        // 测试各种边界值的组合

        // 手机号长度边界
        assertFalse(ValidationUtils.validatePhoneNumber("1234567890").isSuccess()); // 10位
        assertTrue(ValidationUtils.validatePhoneNumber("13812345678").isSuccess());  // 11位
        assertFalse(ValidationUtils.validatePhoneNumber("138123456789").isSuccess());// 12位

        // 密码长度边界
        assertFalse(ValidationUtils.validatePassword("Abc12").isSuccess());   // 5位
        assertTrue(ValidationUtils.validatePassword("Abc123").isSuccess());   // 6位
        assertTrue(ValidationUtils.validatePassword("Abc12345678901234567890").isSuccess()); // 20位
        assertFalse(ValidationUtils.validatePassword("Abc123456789012345678901").isSuccess()); // 21位

        // 用户名长度边界
        assertFalse(ValidationUtils.validateUsername("a").isSuccess());       // 1位
        assertTrue(ValidationUtils.validateUsername("ab").isSuccess());       // 2位
        assertTrue(ValidationUtils.validateUsername("a".repeat(20)).isSuccess()); // 20位
        assertFalse(ValidationUtils.validateUsername("a".repeat(21)).isSuccess()); // 21位

        // 邮箱长度边界
        assertFalse(ValidationUtils.validateEmail("a@b").isSuccess());        // 4位
        assertTrue(ValidationUtils.validateEmail("a@b.c").isSuccess());       // 5位
    }

    @Test
    @Order(4)
    @DisplayName("等价类划分测试集成")
    void testEquivalenceClassIntegration() {
        // 测试等价类的组合

        // 有效等价类组合
        String[] validPhones = {"13812345678", "14712345678", "15012345678"};
        String[] validPasswords = {"Test123", "Password1", "Abc123@"};
        String[] validUsernames = {"张三", "john123", "test_user"};
        String[] validEmails = {"test@example.com", "user@domain.org"};
        String[] validGenders = {"男", "女", "未知"};

        // 测试有效组合
        for (String phone : validPhones) {
            assertTrue(ValidationUtils.validatePhoneNumber(phone).isSuccess(),
                    "手机号 " + phone + " 应该有效");
        }

        for (String password : validPasswords) {
            assertTrue(ValidationUtils.validatePassword(password).isSuccess(),
                    "密码 " + password + " 应该有效");
        }

        for (String username : validUsernames) {
            assertTrue(ValidationUtils.validateUsername(username).isSuccess(),
                    "用户名 " + username + " 应该有效");
        }

        for (String email : validEmails) {
            assertTrue(ValidationUtils.validateEmail(email).isSuccess(),
                    "邮箱 " + email + " 应该有效");
        }

        for (String gender : validGenders) {
            assertTrue(ValidationUtils.validateGender(gender).isSuccess(),
                    "性别 " + gender + " 应该有效");
        }

        // 无效等价类组合
        String[] invalidPhones = {"12812345678", "1081234567", "138123456a"};
        String[] invalidPasswords = {"123456", "abcdef", "12345"};
        String[] invalidUsernames = {"a", "user@123", "test-user"};
        String[] invalidEmails = {"invalid-email", "@domain.com", "user@"};
        String[] invalidGenders = {"其他", "男性", "unknown"};

        // 测试无效组合
        for (String phone : invalidPhones) {
            assertFalse(ValidationUtils.validatePhoneNumber(phone).isSuccess(),
                    "手机号 " + phone + " 应该无效");
        }

        for (String password : invalidPasswords) {
            assertFalse(ValidationUtils.validatePassword(password).isSuccess(),
                    "密码 " + password + " 应该无效");
        }

        for (String username : invalidUsernames) {
            assertFalse(ValidationUtils.validateUsername(username).isSuccess(),
                    "用户名 " + username + " 应该无效");
        }

        for (String email : invalidEmails) {
            assertFalse(ValidationUtils.validateEmail(email).isSuccess(),
                    "邮箱 " + email + " 应该无效");
        }

        for (String gender : invalidGenders) {
            assertFalse(ValidationUtils.validateGender(gender).isSuccess(),
                    "性别 " + gender + " 应该无效");
        }
    }

    @Test
    @Order(5)
    @DisplayName("数值边界测试集成")
    void testNumericBoundaryIntegration() {
        // 测试数值类型的边界值

        // 商品数量边界测试
        assertFalse(ValidationUtils.validateQuantity(0).isSuccess());     // 小于最小值
        assertTrue(ValidationUtils.validateQuantity(1).isSuccess());      // 最小值
        assertTrue(ValidationUtils.validateQuantity(999).isSuccess());    // 最大值
        assertFalse(ValidationUtils.validateQuantity(1000).isSuccess());  // 大于最大值

        // 金额边界测试
        assertFalse(ValidationUtils.validateAmount(0.0).isSuccess());     // 小于最小值
        assertTrue(ValidationUtils.validateAmount(0.01).isSuccess());     // 最小值
        assertTrue(ValidationUtils.validateAmount(9999.99).isSuccess());  // 最大值
        assertFalse(ValidationUtils.validateAmount(10000.0).isSuccess()); // 大于最大值

        // 登录失败次数边界测试
        assertFalse(ValidationUtils.validateLoginAttempts(-1).isSuccess()); // 小于最小值
        assertTrue(ValidationUtils.validateLoginAttempts(0).isSuccess());   // 最小值
        assertTrue(ValidationUtils.validateLoginAttempts(5).isSuccess());   // 最大值
        assertFalse(ValidationUtils.validateLoginAttempts(6).isSuccess());  // 大于最大值
    }

    @Test
    @Order(6)
    @DisplayName("异常处理集成测试")
    void testExceptionHandlingIntegration() {
        // 测试各种空值和异常情况

        // 空值测试
        assertFalse(ValidationUtils.validatePhoneNumber(null).isSuccess());
        assertFalse(ValidationUtils.validatePassword(null).isSuccess());
        assertFalse(ValidationUtils.validateUsername(null).isSuccess());
        assertFalse(ValidationUtils.validateEmail(null).isSuccess());
        assertFalse(ValidationUtils.validateGender(null).isSuccess());

        // 空字符串测试
        assertFalse(ValidationUtils.validatePhoneNumber("").isSuccess());
        assertFalse(ValidationUtils.validatePassword("").isSuccess());
        assertFalse(ValidationUtils.validateUsername("").isSuccess());
        assertFalse(ValidationUtils.validateEmail("").isSuccess());
        assertFalse(ValidationUtils.validateGender("").isSuccess());

        // 空白字符串测试
        assertFalse(ValidationUtils.validatePhoneNumber("   ").isSuccess());
        assertFalse(ValidationUtils.validateUsername("   ").isSuccess());
        assertFalse(ValidationUtils.validateEmail("   ").isSuccess());
        assertFalse(ValidationUtils.validateGender("   ").isSuccess());

        // 空对象测试
        assertFalse(ValidationUtils.validateQuantity(null).isSuccess());
        assertFalse(ValidationUtils.validateAmount(null).isSuccess());
        assertFalse(ValidationUtils.validateLoginAttempts(null).isSuccess());
    }
} 