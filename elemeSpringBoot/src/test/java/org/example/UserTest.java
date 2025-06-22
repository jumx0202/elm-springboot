package org.example;

import org.example.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * User实体类单元测试
 * 测试各种验证注解和业务逻辑
 */
@DisplayName("用户实体类测试")
class UserTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("创建有效用户")
    void testCreateValidUser() {
        User user = new User();
        user.setPhoneNumber("13812345678");
        user.setPassword("Test123");
        user.setGender("男");
        user.setName("张三");
        user.setEmail("test@example.com");
        user.setLoginAttempts(0);
        user.setAccountLocked(false);

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "有效用户不应该有验证错误");
    }

    @Nested
    @DisplayName("手机号验证测试")
    class PhoneNumberValidationTest {

        @Test
        @DisplayName("手机号为空测试")
        void testPhoneNumberEmpty() {
            User user = createValidUser();
            user.setPhoneNumber(null);

            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("手机号不能为空")));

            user.setPhoneNumber("");
            violations = validator.validate(user);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("手机号不能为空")));
        }

        @Test
        @DisplayName("手机号格式错误测试")
        void testPhoneNumberInvalidFormat() {
            User user = createValidUser();
            
            // 无效手机号格式
            String[] invalidPhones = {
                "12812345678", // 第二位不是3-9
                "1081234567",  // 长度不足
                "138123456789", // 长度超出
                "1381234567a"   // 包含字母
            };

            for (String phone : invalidPhones) {
                user.setPhoneNumber(phone);
                Set<ConstraintViolation<User>> violations = validator.validate(user);
                assertFalse(violations.isEmpty(), "手机号 " + phone + " 应该验证失败");
            }
        }

        @Test
        @DisplayName("手机号格式正确测试")
        void testPhoneNumberValidFormat() {
            User user = createValidUser();
            
            String[] validPhones = {
                "13812345678", "14712345678", "15012345678",
                "16612345678", "17712345678", "18812345678", "19912345678"
            };

            for (String phone : validPhones) {
                user.setPhoneNumber(phone);
                Set<ConstraintViolation<User>> violations = validator.validate(user);
                assertTrue(violations.isEmpty(), "手机号 " + phone + " 应该验证通过");
            }
        }
    }

    @Nested
    @DisplayName("密码验证测试")
    class PasswordValidationTest {

        @Test
        @DisplayName("密码为空测试")
        void testPasswordEmpty() {
            User user = createValidUser();
            user.setPassword(null);

            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("密码不能为空")));

            user.setPassword("");
            violations = validator.validate(user);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("密码不能为空")));
        }

        @Test
        @DisplayName("密码长度测试")
        void testPasswordLength() {
            User user = createValidUser();

            // 密码过短
            user.setPassword("Ab1");
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("密码长度必须在6-20位之间")));

            // 密码过长
            user.setPassword("Ab1234567890123456789");
            violations = validator.validate(user);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("密码长度必须在6-20位之间")));
        }

        @Test
        @DisplayName("密码复杂度测试")
        void testPasswordComplexity() {
            User user = createValidUser();

            // 只有字母
            user.setPassword("abcdef");
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("密码必须包含字母和数字")));

            // 只有数字
            user.setPassword("123456");
            violations = validator.validate(user);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("密码必须包含字母和数字")));

            // 有效密码
            String[] validPasswords = {"Test123", "Password1", "Abc123@"};
            for (String password : validPasswords) {
                user.setPassword(password);
                violations = validator.validate(user);
                assertTrue(violations.isEmpty(), "密码 " + password + " 应该验证通过");
            }
        }
    }

    @Nested
    @DisplayName("性别验证测试")
    class GenderValidationTest {

        @Test
        @DisplayName("有效性别测试")
        void testValidGender() {
            User user = createValidUser();
            String[] validGenders = {"男", "女", "未知"};

            for (String gender : validGenders) {
                user.setGender(gender);
                Set<ConstraintViolation<User>> violations = validator.validate(user);
                assertTrue(violations.isEmpty(), "性别 " + gender + " 应该验证通过");
            }
        }

        @Test
        @DisplayName("无效性别测试")
        void testInvalidGender() {
            User user = createValidUser();
            String[] invalidGenders = {"其他", "男性", "女性", "unknown"};

            for (String gender : invalidGenders) {
                user.setGender(gender);
                Set<ConstraintViolation<User>> violations = validator.validate(user);
                assertFalse(violations.isEmpty(), "性别 " + gender + " 应该验证失败");
                assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("性别只能是男、女或未知")));
            }
        }
    }

    @Nested
    @DisplayName("用户名验证测试")
    class NameValidationTest {

        @Test
        @DisplayName("用户名为空测试")
        void testNameEmpty() {
            User user = createValidUser();
            user.setName(null);

            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("用户名不能为空")));

            user.setName("");
            violations = validator.validate(user);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("用户名不能为空")));
        }

        @Test
        @DisplayName("用户名长度测试")
        void testNameLength() {
            User user = createValidUser();

            // 用户名过短
            user.setName("a");
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("用户名长度必须在2-20位之间")));

            // 用户名过长
            user.setName("a".repeat(21));
            violations = validator.validate(user);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("用户名长度必须在2-20位之间")));
        }

        @Test
        @DisplayName("用户名格式测试")
        void testNameFormat() {
            User user = createValidUser();

            // 有效用户名
            String[] validNames = {"张三", "John", "user123", "test_user", "用户1"};
            for (String name : validNames) {
                user.setName(name);
                Set<ConstraintViolation<User>> violations = validator.validate(user);
                assertTrue(violations.isEmpty(), "用户名 " + name + " 应该验证通过");
            }

            // 无效用户名
            String[] invalidNames = {"user@123", "test-user", "user#123"};
            for (String name : invalidNames) {
                user.setName(name);
                Set<ConstraintViolation<User>> violations = validator.validate(user);
                assertFalse(violations.isEmpty(), "用户名 " + name + " 应该验证失败");
                assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("用户名只能包含中文、英文、数字和下划线")));
            }
        }
    }

    @Nested
    @DisplayName("邮箱验证测试")
    class EmailValidationTest {

        @Test
        @DisplayName("邮箱为空测试")
        void testEmailEmpty() {
            User user = createValidUser();
            user.setEmail(null);

            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("邮箱不能为空")));

            user.setEmail("");
            violations = validator.validate(user);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("邮箱不能为空")));
        }

        @Test
        @DisplayName("邮箱格式测试")
        void testEmailFormat() {
            User user = createValidUser();

            // 有效邮箱
            String[] validEmails = {"test@example.com", "user@domain.org", "test123@test.net"};
            for (String email : validEmails) {
                user.setEmail(email);
                Set<ConstraintViolation<User>> violations = validator.validate(user);
                assertTrue(violations.isEmpty(), "邮箱 " + email + " 应该验证通过");
            }

            // 无效邮箱
            String[] invalidEmails = {"invalid-email", "@domain.com", "user@", "user@domain"};
            for (String email : invalidEmails) {
                user.setEmail(email);
                Set<ConstraintViolation<User>> violations = validator.validate(user);
                assertFalse(violations.isEmpty(), "邮箱 " + email + " 应该验证失败");
                assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("邮箱格式不正确")));
            }
        }
    }

    @Nested
    @DisplayName("登录失败次数验证测试")
    class LoginAttemptsValidationTest {

        @Test
        @DisplayName("登录失败次数边界值测试")
        void testLoginAttemptsBoundary() {
            User user = createValidUser();

            // 负数测试
            user.setLoginAttempts(-1);
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("登录失败次数不能为负数")));

            // 超出限制测试
            user.setLoginAttempts(11);
            violations = validator.validate(user);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("登录失败次数超出限制")));

            // 有效值测试
            for (int i = 0; i <= 10; i++) {
                user.setLoginAttempts(i);
                violations = validator.validate(user);
                assertTrue(violations.isEmpty(), "登录失败次数 " + i + " 应该验证通过");
            }
        }
    }

    @Test
    @DisplayName("账户锁定状态测试")
    void testAccountLocked() {
        User user = createValidUser();

        // 测试默认值
        assertFalse(user.getAccountLocked(), "账户默认应该是未锁定状态");

        // 测试设置锁定状态
        user.setAccountLocked(true);
        assertTrue(user.getAccountLocked(), "应该能够设置账户锁定状态");

        // 验证锁定状态不影响其他验证
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "账户锁定状态不应该影响其他字段验证");
    }

    @Test
    @DisplayName("用户对象相等性测试")
    void testUserEquality() {
        User user1 = createValidUser();
        User user2 = createValidUser();

        // 由于使用了Lombok的@Data注解，会自动生成equals和hashCode方法
        assertEquals(user1, user2, "相同内容的用户对象应该相等");
        assertEquals(user1.hashCode(), user2.hashCode(), "相同内容的用户对象应该有相同的hashCode");

        // 修改一个字段后应该不相等
        user2.setName("李四");
        assertNotEquals(user1, user2, "不同内容的用户对象应该不相等");
    }

    /**
     * 创建一个有效的用户对象
     */
    private User createValidUser() {
        User user = new User();
        user.setPhoneNumber("13812345678");
        user.setPassword("Test123");
        user.setGender("男");
        user.setName("张三");
        user.setEmail("test@example.com");
        user.setLoginAttempts(0);
        user.setAccountLocked(false);
        return user;
    }
}