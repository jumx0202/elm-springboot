package org.example;

import org.example.entity.User;
import org.example.entity.CartItem;
import org.example.entity.Food;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试数据生成器
 * 用于生成各种边界值和等价类的测试数据
 */
public class TestDataGenerator {

    /**
     * 生成手机号测试数据
     * 包含边界值和等价类数据
     */
    public static class PhoneNumberTestData {
        
        // 有效手机号（等价类：有效数据）
        public static String[] getValidPhoneNumbers() {
            return new String[]{
                "13812345678", // 移动号段
                "14712345678", // 联通号段
                "15912345678", // 移动号段
                "18612345678", // 移动号段
                "19912345678"  // 电信号段
            };
        }
        
        // 边界值：长度测试
        public static String[] getLengthBoundaryData() {
            return new String[]{
                "1381234567",    // 10位（小于11）
                "13812345678",   // 11位（正确）
                "138123456789"   // 12位（大于11）
            };
        }
        
        // 无效手机号（等价类：无效数据）
        public static String[] getInvalidPhoneNumbers() {
            return new String[]{
                "",              // 空字符串
                "12812345678",   // 第二位为2
                "10812345678",   // 第二位为0
                "2381234567",    // 不以1开头
                "1a812345678",   // 包含字母
                "138123456a8"    // 包含字母
            };
        }
    }

    /**
     * 生成邮箱测试数据
     */
    public static class EmailTestData {
        
        // 有效邮箱
        public static String[] getValidEmails() {
            return new String[]{
                "test@example.com",
                "user.name@domain.org",
                "user+tag@example.co.uk",
                "user123@test123.net",
                "a@b.co"  // 最短有效邮箱（5字符）
            };
        }
        
        // 长度边界值测试
        public static String[] getLengthBoundaryEmails() {
            return new String[]{
                "a@b",           // 4字符（太短）
                "a@b.c",         // 5字符（最短有效）
                generateLongEmail(100),  // 100字符（最大有效）
                generateLongEmail(101)   // 101字符（太长）
            };
        }
        
        // 无效邮箱
        public static String[] getInvalidEmails() {
            return new String[]{
                "",
                "invalid-email",
                "@domain.com",
                "user@",
                "user@domain",
                "user@domain."
            };
        }
        
        private static String generateLongEmail(int length) {
            int domainLength = 9; // "@test.com"
            int usernameLength = length - domainLength;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < usernameLength; i++) {
                sb.append("a");
            }
            sb.append("@test.com");
            return sb.toString();
        }
    }

    /**
     * 生成密码测试数据
     */
    public static class PasswordTestData {
        
        // 有效密码
        public static String[] getValidPasswords() {
            return new String[]{
                "abc123",                    // 6字符（最短）
                "Password1",                 // 包含大小写和数字
                "test123@",                  // 包含特殊字符
                "abcdefghij1234567890"       // 20字符（最长）
            };
        }
        
        // 长度边界值测试
        public static String[] getLengthBoundaryPasswords() {
            return new String[]{
                "abc12",                     // 5字符（太短）
                "abc123",                    // 6字符（最短有效）
                "abcdefghij1234567890",      // 20字符（最长有效）
                "abcdefghij12345678901"      // 21字符（太长）
            };
        }
        
        // 无效密码（复杂度不够）
        public static String[] getInvalidPasswords() {
            return new String[]{
                "",
                "abcdef",        // 只有字母
                "123456",        // 只有数字
                "ABCDEF",        // 只有大写字母
                "abc123中文"      // 包含中文
            };
        }
    }

    /**
     * 生成数量测试数据
     */
    public static class QuantityTestData {
        
        // 边界值测试数据
        public static Integer[] getBoundaryQuantities() {
            return new Integer[]{
                0,      // 最小值-1（无效）
                1,      // 最小值（有效）
                999,    // 最大值（有效）
                1000    // 最大值+1（无效）
            };
        }
        
        // 有效数量
        public static Integer[] getValidQuantities() {
            return new Integer[]{1, 5, 10, 50, 100, 500, 999};
        }
        
        // 无效数量
        public static Integer[] getInvalidQuantities() {
            return new Integer[]{-1, 0, 1000, 9999};
        }
    }

    /**
     * 生成金额测试数据
     */
    public static class AmountTestData {
        
        // 边界值测试数据
        public static Double[] getBoundaryAmounts() {
            return new Double[]{
                0.00,      // 最小值-0.01（无效）
                0.01,      // 最小值（有效）
                9999.99,   // 最大值（有效）
                10000.00   // 最大值+0.01（无效）
            };
        }
        
        // 订单金额边界值测试
        public static Double[] getOrderAmountBoundary() {
            return new Double[]{
                4999.99,   // 最大订单金额-0.01（有效）
                5000.00,   // 最大订单金额（有效）
                5000.01    // 最大订单金额+0.01（无效）
            };
        }
    }

    /**
     * 生成用户测试对象
     */
    public static class UserTestData {
        
        // 创建有效用户
        public static User createValidUser() {
            User user = new User();
            user.setPhoneNumber("13812345678");
            user.setPassword("test123");
            user.setName("测试用户");
            user.setEmail("test@example.com");
            user.setGender("男");
            user.setLoginAttempts(0);
            user.setAccountLocked(false);
            return user;
        }
        
        // 创建边界值测试用户数组
        public static User[] createBoundaryUsers() {
            List<User> users = new ArrayList<>();
            
            // 最短用户名
            User userMinName = createValidUser();
            userMinName.setName("张三");  // 2字符
            users.add(userMinName);
            
            // 最长用户名
            User userMaxName = createValidUser();
            userMaxName.setName("a".repeat(20));  // 20字符
            users.add(userMaxName);
            
            // 最短密码
            User userMinPassword = createValidUser();
            userMinPassword.setPassword("abc123");  // 6字符
            users.add(userMinPassword);
            
            // 最长密码
            User userMaxPassword = createValidUser();
            userMaxPassword.setPassword("a1".repeat(10));  // 20字符
            users.add(userMaxPassword);
            
            return users.toArray(new User[0]);
        }
        
        // 创建登录失败次数测试用户
        public static User[] createLoginAttemptsUsers() {
            List<User> users = new ArrayList<>();
            
            for (int attempts = 0; attempts <= 6; attempts++) {
                User user = createValidUser();
                user.setPhoneNumber("1381234567" + attempts);
                user.setLoginAttempts(attempts);
                user.setAccountLocked(attempts >= 5);
                users.add(user);
            }
            
            return users.toArray(new User[0]);
        }
    }

    /**
     * 生成购物车项目测试数据
     */
    public static class CartItemTestData {
        
        // 创建有效购物车项目
        public static CartItem createValidCartItem() {
            CartItem item = new CartItem();
            item.setUserPhone("13812345678");
            item.setFoodId(1);
            item.setFoodName("测试商品");
            item.setQuantity(1);
            item.setUnitPrice(new BigDecimal("10.50"));
            item.setTotalPrice(new BigDecimal("10.50"));
            item.setBusinessId(1);
            item.setBusinessName("测试商家");
            item.setCreatedTime(LocalDateTime.now());
            item.setIsValid(1);
            return item;
        }
        
        // 创建数量边界值测试数据
        public static CartItem[] createQuantityBoundaryItems() {
            List<CartItem> items = new ArrayList<>();
            
            Integer[] quantities = {1, 999};  // 边界值
            
            for (Integer quantity : quantities) {
                CartItem item = createValidCartItem();
                item.setQuantity(quantity);
                item.setTotalPrice(item.getUnitPrice().multiply(new BigDecimal(quantity)));
                items.add(item);
            }
            
            return items.toArray(new CartItem[0]);
        }
        
        // 创建价格边界值测试数据
        public static CartItem[] createPriceBoundaryItems() {
            List<CartItem> items = new ArrayList<>();
            
            BigDecimal[] prices = {
                new BigDecimal("0.01"),     // 最小价格
                new BigDecimal("9999.99")   // 最大价格
            };
            
            for (BigDecimal price : prices) {
                CartItem item = createValidCartItem();
                item.setUnitPrice(price);
                item.setTotalPrice(price.multiply(new BigDecimal(item.getQuantity())));
                items.add(item);
            }
            
            return items.toArray(new CartItem[0]);
        }
        
        // 创建批量购物车数据（用于测试购物车数量限制）
        public static List<CartItem> createBatchCartItems(String userPhone, int count) {
            List<CartItem> items = new ArrayList<>();
            
            for (int i = 1; i <= count; i++) {
                CartItem item = createValidCartItem();
                item.setUserPhone(userPhone);
                item.setFoodId(i);
                item.setFoodName("商品" + i);
                items.add(item);
            }
            
            return items;
        }
    }

    /**
     * 生成商品测试数据
     */
    public static class FoodTestData {
        
        // 创建有效商品
        public static Food createValidFood() {
            Food food = new Food();
            food.setId(1);
            food.setName("测试商品");
            food.setText("美味的测试商品");
            food.setAmount("100");
            food.setDiscount("满50减10");
            food.setRedPrice(25.50);
            food.setGrayPrice("30.00");
            food.setBusiness(1);
            food.setImg("/images/food1.jpg");
            food.setSelling(1);
            return food;
        }
        
        // 创建价格边界值测试商品
        public static Food[] createPriceBoundaryFoods() {
            List<Food> foods = new ArrayList<>();
            
            Double[] prices = {0.01, 9999.99};  // 边界价格
            
            for (Double price : prices) {
                Food food = createValidFood();
                food.setRedPrice(price);
                foods.add(food);
            }
            
            return foods.toArray(new Food[0]);
        }
    }

    /**
     * 生成性别测试数据
     */
    public static class GenderTestData {
        
        // 有效性别
        public static String[] getValidGenders() {
            return new String[]{"男", "女", "未知"};
        }
        
        // 无效性别
        public static String[] getInvalidGenders() {
            return new String[]{"", "其他", "male", "female", "1", "0", "保密"};
        }
    }

    /**
     * 生成综合测试场景数据
     */
    public static class IntegrationTestData {
        
        // 创建完整的用户注册测试数据
        public static Object[][] createRegistrationTestData() {
            return new Object[][]{
                // {phoneNumber, password, confirmPassword, name, email, expectedResult}
                {"13812345678", "test123", "test123", "张三", "test@example.com", 0},  // 成功
                {"13812345678", "test123", "test456", "张三", "test@example.com", 1},  // 密码不一致
                {"13812345678", "test123", "test123", "张三", "test@example.com", 2},  // 手机号已存在
                {"1381234567", "test123", "test123", "张三", "test@example.com", 5},   // 手机号格式错误
                {"13812345678", "12345", "12345", "张三", "test@example.com", 3},      // 密码太短
                {"13812345678", "test123", "test123", "张三", "invalid-email", 4},     // 邮箱格式错误
                {"13812345678", "test123", "test123", "a", "test@example.com", 6}      // 用户名太短
            };
        }
        
        // 创建购物车操作测试数据
        public static Object[][] createCartOperationTestData() {
            return new Object[][]{
                // {userPhone, quantity, expectedResult}
                {"13812345678", 1, 0},      // 正常添加
                {"13812345678", 0, 3},      // 数量为0
                {"13812345678", 1000, 3},   // 数量超限
                {"1381234567", 1, 2},       // 手机号格式错误
                {"", 1, 2}                  // 手机号为空
            };
        }
    }

    /**
     * 性能测试数据生成器
     */
    public static class PerformanceTestData {
        
        // 生成大量用户数据
        public static List<User> generateLargeUserData(int count) {
            List<User> users = new ArrayList<>();
            
            for (int i = 0; i < count; i++) {
                User user = new User();
                user.setPhoneNumber("138" + String.format("%08d", i));
                user.setPassword("test123");
                user.setName("用户" + i);
                user.setEmail("user" + i + "@test.com");
                user.setGender(i % 3 == 0 ? "男" : (i % 3 == 1 ? "女" : "未知"));
                user.setLoginAttempts(i % 6);
                user.setAccountLocked(i % 6 >= 5);
                users.add(user);
            }
            
            return users;
        }
        
        // 生成大量购物车数据
        public static List<CartItem> generateLargeCartData(int count) {
            List<CartItem> items = new ArrayList<>();
            
            for (int i = 0; i < count; i++) {
                CartItem item = new CartItem();
                item.setUserPhone("138" + String.format("%08d", i % 1000));
                item.setFoodId(i % 100 + 1);
                item.setFoodName("商品" + (i % 100 + 1));
                item.setQuantity((i % 10) + 1);
                item.setUnitPrice(new BigDecimal(String.format("%.2f", (i % 100 + 1) * 0.5)));
                item.setBusinessId((i % 50) + 1);
                item.setBusinessName("商家" + ((i % 50) + 1));
                item.setCreatedTime(LocalDateTime.now());
                item.setIsValid(1);
                item.calculateTotalPrice();
                items.add(item);
            }
            
            return items;
        }
    }
} 