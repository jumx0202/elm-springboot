package org.example;

import org.example.util.ValidationUtils;
import org.example.util.ValidationUtils.ValidationResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * ValidationUtils单元测试类
 * 包含各种黑盒测试方法：边界值分析、等价类划分
 */
@DisplayName("验证工具类测试")
class ValidationUtilsTest {

    @Nested
    @DisplayName("手机号验证测试")
    class PhoneNumberValidationTest {
        
        @Test
        @DisplayName("手机号为空的情况")
        void testPhoneNumberEmpty() {
            // 测试null值
            ValidationResult result = ValidationUtils.validatePhoneNumber(null);
            assertFalse(result.isSuccess());
            assertEquals("手机号不能为空", result.getMessage());
            
            // 测试空字符串
            result = ValidationUtils.validatePhoneNumber("");
            assertFalse(result.isSuccess());
            assertEquals("手机号不能为空", result.getMessage());
            
            // 测试空白字符串
            result = ValidationUtils.validatePhoneNumber("   ");
            assertFalse(result.isSuccess());
            assertEquals("手机号不能为空", result.getMessage());
        }
        
        @Test
        @DisplayName("手机号长度边界值测试")
        void testPhoneNumberLength() {
            // 边界值测试：10位（小于11位）
            ValidationResult result = ValidationUtils.validatePhoneNumber("1234567890");
            assertFalse(result.isSuccess());
            assertEquals("手机号长度不足，应为11位", result.getMessage());
            
            // 边界值测试：11位（正确长度）
            result = ValidationUtils.validatePhoneNumber("13812345678");
            assertTrue(result.isSuccess());
            
            // 边界值测试：12位（大于11位）
            result = ValidationUtils.validatePhoneNumber("138123456789");
            assertFalse(result.isSuccess());
            assertEquals("手机号长度超出，应为11位", result.getMessage());
        }
        
        @Test
        @DisplayName("手机号格式测试")
        void testPhoneNumberFormat() {
            // 有效格式测试
            String[] validPhones = {
                "13812345678", "14712345678", "15012345678", 
                "16612345678", "17712345678", "18812345678", "19912345678"
            };
            
            for (String phone : validPhones) {
                ValidationResult result = ValidationUtils.validatePhoneNumber(phone);
                assertTrue(result.isSuccess(), "手机号 " + phone + " 应该是有效的");
            }
            
            // 无效格式测试
            String[] invalidPhones = {
                "12812345678", // 第二位为2
                "10812345678", // 第二位为0
                "1a812345678", // 包含字母
                "138123456a8", // 末尾包含字母
                "038123456789" // 不以1开头
            };
            
            for (String phone : invalidPhones) {
                ValidationResult result = ValidationUtils.validatePhoneNumber(phone);
                assertFalse(result.isSuccess(), "手机号 " + phone + " 应该是无效的");
                assertEquals("手机号格式不正确，应以1开头且第二位为3-9", result.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("邮箱验证测试")
    class EmailValidationTest {
        
        @Test
        @DisplayName("邮箱长度边界值测试")
        void testEmailLength() {
            // 边界值测试：4字符（小于5字符）
            ValidationResult result = ValidationUtils.validateEmail("a@b");
            assertFalse(result.isSuccess());
            assertEquals("邮箱长度过短，至少5个字符", result.getMessage());
            
            // 边界值测试：5字符（最小长度）
            result = ValidationUtils.validateEmail("a@b.c");
            assertTrue(result.isSuccess());
            
            // 边界值测试：100字符（最大长度）
            String longEmail = "a".repeat(92) + "@test.com"; // 92 + 8 = 100
            result = ValidationUtils.validateEmail(longEmail);
            assertTrue(result.isSuccess());
            
            // 边界值测试：101字符（超过最大长度）
            String tooLongEmail = "a".repeat(93) + "@test.com"; // 93 + 8 = 101
            result = ValidationUtils.validateEmail(tooLongEmail);
            assertFalse(result.isSuccess());
            assertEquals("邮箱长度过长，最多100个字符", result.getMessage());
        }
        
        @Test
        @DisplayName("邮箱格式测试")
        void testEmailFormat() {
            // 有效邮箱格式
            String[] validEmails = {
                "test@example.com",
                "user.name@domain.org",
                "user+tag@example.co.uk",
                "user123@test123.net"
            };
            
            for (String email : validEmails) {
                ValidationResult result = ValidationUtils.validateEmail(email);
                assertTrue(result.isSuccess(), "邮箱 " + email + " 应该是有效的");
            }
            
            // 无效邮箱格式
            String[] invalidEmails = {
                "invalid-email",
                "@domain.com",
                "user@",
                "user@domain",
                "user@domain."
            };
            
            for (String email : invalidEmails) {
                ValidationResult result = ValidationUtils.validateEmail(email);
                assertFalse(result.isSuccess(), "邮箱 " + email + " 应该是无效的");
                assertEquals("邮箱格式不正确", result.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("密码验证测试")
    class PasswordValidationTest {
        
        @Test
        @DisplayName("密码长度边界值测试")
        void testPasswordLength() {
            // 边界值测试：5字符（小于6字符）
            ValidationResult result = ValidationUtils.validatePassword("ab12");
            assertFalse(result.isSuccess());
            assertEquals("密码长度过短，至少6个字符", result.getMessage());
            
            // 边界值测试：6字符（最小长度）
            result = ValidationUtils.validatePassword("abc123");
            assertTrue(result.isSuccess());
            
            // 边界值测试：20字符（最大长度）
            result = ValidationUtils.validatePassword("abcdefghij1234567890");
            assertTrue(result.isSuccess());
            
            // 边界值测试：21字符（超过最大长度）
            result = ValidationUtils.validatePassword("abcdefghij12345678901");
            assertFalse(result.isSuccess());
            assertEquals("密码长度过长，最多20个字符", result.getMessage());
        }
        
        @Test
        @DisplayName("密码复杂度测试")
        void testPasswordComplexity() {
            // 有效密码（包含字母和数字）
            String[] validPasswords = {
                "abc123",
                "Password1",
                "test123@",
                "Aa1#$%&*"
            };
            
            for (String password : validPasswords) {
                ValidationResult result = ValidationUtils.validatePassword(password);
                assertTrue(result.isSuccess(), "密码 " + password + " 应该是有效的");
            }
            
            // 无效密码
            String[] invalidPasswords = {
                "abcdef",    // 只有字母
                "123456",    // 只有数字
                "ABC123",    // 没有小写字母
                "abc123中文"  // 包含中文
            };
            
            for (String password : invalidPasswords) {
                ValidationResult result = ValidationUtils.validatePassword(password);
                assertFalse(result.isSuccess(), "密码 " + password + " 应该是无效的");
                assertEquals("密码必须包含字母和数字，可包含特殊字符@$!%*#?&", result.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("商品数量验证测试")
    class QuantityValidationTest {
        
        @Test
        @DisplayName("商品数量边界值测试")
        void testQuantityBoundary() {
            // 边界值测试：0（小于1）
            ValidationResult result = ValidationUtils.validateQuantity(0);
            assertFalse(result.isSuccess());
            assertEquals("商品数量不能小于1", result.getMessage());
            
            // 边界值测试：1（最小值）
            result = ValidationUtils.validateQuantity(1);
            assertTrue(result.isSuccess());
            
            // 边界值测试：999（最大值）
            result = ValidationUtils.validateQuantity(999);
            assertTrue(result.isSuccess());
            
            // 边界值测试：1000（超过最大值）
            result = ValidationUtils.validateQuantity(1000);
            assertFalse(result.isSuccess());
            assertEquals("商品数量不能大于999", result.getMessage());
        }
        
        @Test
        @DisplayName("商品数量空值测试")
        void testQuantityNull() {
            ValidationResult result = ValidationUtils.validateQuantity(null);
            assertFalse(result.isSuccess());
            assertEquals("商品数量不能为空", result.getMessage());
        }
    }

    @Nested
    @DisplayName("金额验证测试")
    class AmountValidationTest {
        
        @Test
        @DisplayName("金额边界值测试")
        void testAmountBoundary() {
            // 边界值测试：0（小于0.01）
            ValidationResult result = ValidationUtils.validateAmount(0.0);
            assertFalse(result.isSuccess());
            assertEquals("金额不能小于0.01元", result.getMessage());
            
            // 边界值测试：0.01（最小值）
            result = ValidationUtils.validateAmount(0.01);
            assertTrue(result.isSuccess());
            
            // 边界值测试：9999.99（最大值）
            result = ValidationUtils.validateAmount(9999.99);
            assertTrue(result.isSuccess());
            
            // 边界值测试：10000.00（超过最大值）
            result = ValidationUtils.validateAmount(10000.00);
            assertFalse(result.isSuccess());
            assertEquals("金额不能大于9999.99元", result.getMessage());
        }
    }

    @Nested
    @DisplayName("订单金额验证测试")
    class OrderAmountValidationTest {
        
        @Test
        @DisplayName("订单金额边界值测试")
        void testOrderAmountBoundary() {
            // 边界值测试：5000.00（最大订单金额）
            ValidationResult result = ValidationUtils.validateOrderAmount(5000.00);
            assertTrue(result.isSuccess());
            
            // 边界值测试：5000.01（超过最大订单金额）
            result = ValidationUtils.validateOrderAmount(5000.01);
            assertFalse(result.isSuccess());
            assertEquals("单次订单金额不能超过5000元", result.getMessage());
        }
    }

    @Nested
    @DisplayName("购物车商品数量验证测试")
    class CartItemCountValidationTest {
        
        @Test
        @DisplayName("购物车商品数量边界值测试")
        void testCartItemCountBoundary() {
            // 边界值测试：0（最小值）
            ValidationResult result = ValidationUtils.validateCartItemCount(0);
            assertTrue(result.isSuccess());
            
            // 边界值测试：50（最大值）
            result = ValidationUtils.validateCartItemCount(50);
            assertTrue(result.isSuccess());
            
            // 边界值测试：51（超过最大值）
            result = ValidationUtils.validateCartItemCount(51);
            assertFalse(result.isSuccess());
            assertEquals("购物车商品种类不能超过50种", result.getMessage());
        }
    }

    @Nested
    @DisplayName("登录失败次数验证测试")
    class LoginAttemptsValidationTest {
        
        @Test
        @DisplayName("登录失败次数边界值测试")
        void testLoginAttemptsBoundary() {
            // 边界值测试：4次（未达到限制）
            ValidationResult result = ValidationUtils.validateLoginAttempts(4);
            assertTrue(result.isSuccess());
            
            // 边界值测试：5次（达到限制）
            result = ValidationUtils.validateLoginAttempts(5);
            assertFalse(result.isSuccess());
            assertEquals("登录失败次数过多，账户已被锁定", result.getMessage());
            
            // 边界值测试：6次（超过限制）
            result = ValidationUtils.validateLoginAttempts(6);
            assertFalse(result.isSuccess());
            assertEquals("登录失败次数过多，账户已被锁定", result.getMessage());
        }
    }

    @Nested
    @DisplayName("等价类划分测试")
    class EquivalenceClassTest {
        
        @Test
        @DisplayName("性别验证等价类测试")
        void testGenderEquivalenceClass() {
            // 有效等价类
            String[] validGenders = {"男", "女", "未知"};
            for (String gender : validGenders) {
                ValidationResult result = ValidationUtils.validateGender(gender);
                assertTrue(result.isSuccess(), "性别 " + gender + " 应该是有效的");
            }
            
            // 无效等价类
            String[] invalidGenders = {"", "其他", "male", "female", "1", "0"};
            for (String gender : invalidGenders) {
                ValidationResult result = ValidationUtils.validateGender(gender);
                assertFalse(result.isSuccess(), "性别 " + gender + " 应该是无效的");
            }
        }
    }

    @Nested
    @DisplayName("综合测试场景")
    class IntegrationTest {
        
        @Test
        @DisplayName("密码确认测试")
        void testPasswordConfirmation() {
            // 密码一致
            ValidationResult result = ValidationUtils.validatePasswordConfirmation("abc123", "abc123");
            assertTrue(result.isSuccess());
            
            // 密码不一致
            result = ValidationUtils.validatePasswordConfirmation("abc123", "abc456");
            assertFalse(result.isSuccess());
            assertEquals("两次输入的密码不一致", result.getMessage());
            
            // 空值情况
            result = ValidationUtils.validatePasswordConfirmation(null, "abc123");
            assertFalse(result.isSuccess());
            assertEquals("密码确认失败，密码不能为空", result.getMessage());
        }
    }
} 