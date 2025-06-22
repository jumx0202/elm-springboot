package org.example.util;

import java.util.regex.Pattern;

/**
 * 验证工具类 - 用于各种输入验证和边界检查
 * 支持黑盒测试的边界值分析和等价类划分
 */
public class ValidationUtils {

    // 正则表达式常量
    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{6,20}$";
    private static final String USERNAME_REGEX = "^[\\u4e00-\\u9fa5A-Za-z0-9_]{2,20}$";
    
    // 编译后的Pattern对象，提高性能
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);
    private static final Pattern USERNAME_PATTERN = Pattern.compile(USERNAME_REGEX);

    // 业务常量 - 边界值
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 20;
    public static final int MIN_USERNAME_LENGTH = 2;
    public static final int MAX_USERNAME_LENGTH = 20;
    public static final int MIN_EMAIL_LENGTH = 5;
    public static final int MAX_EMAIL_LENGTH = 100;
    public static final int PHONE_LENGTH = 11;
    public static final int MIN_ADDRESS_LENGTH = 5;
    public static final int MAX_ADDRESS_LENGTH = 200;
    public static final int MIN_QUANTITY = 1;
    public static final int MAX_QUANTITY = 999;
    public static final double MIN_AMOUNT = 0.01;
    public static final double MAX_AMOUNT = 9999.99;
    public static final double MAX_ORDER_AMOUNT = 5000.00;
    public static final int MAX_CART_ITEMS = 50;
    public static final int MAX_LOGIN_ATTEMPTS = 5;

    /**
     * 验证手机号格式和长度
     * 边界值测试点：10位、11位、12位
     */
    public static ValidationResult validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return ValidationResult.error("手机号不能为空");
        }
        
        phoneNumber = phoneNumber.trim();
        
        // 长度检查 - 支持边界值测试
        if (phoneNumber.length() < PHONE_LENGTH) {
            return ValidationResult.error("手机号长度不足，应为11位");
        }
        if (phoneNumber.length() > PHONE_LENGTH) {
            return ValidationResult.error("手机号长度超出，应为11位");
        }
        
        // 格式检查
        if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
            return ValidationResult.error("手机号格式不正确，应以1开头且第二位为3-9");
        }
        
        return ValidationResult.success();
    }

    /**
     * 验证邮箱格式和长度
     * 边界值测试点：4字符、5字符、100字符、101字符
     */
    public static ValidationResult validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return ValidationResult.error("邮箱不能为空");
        }
        
        email = email.trim();
        
        // 长度检查
        if (email.length() < MIN_EMAIL_LENGTH) {
            return ValidationResult.error("邮箱长度过短，至少5个字符");
        }
        if (email.length() > MAX_EMAIL_LENGTH) {
            return ValidationResult.error("邮箱长度过长，最多100个字符");
        }
        
        // 格式检查
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ValidationResult.error("邮箱格式不正确");
        }
        
        return ValidationResult.success();
    }

    /**
     * 验证密码强度和长度
     * 边界值测试点：5字符、6字符、20字符、21字符
     */
    public static ValidationResult validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return ValidationResult.error("密码不能为空");
        }
        
        // 长度检查
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return ValidationResult.error("密码长度过短，至少6个字符");
        }
        if (password.length() > MAX_PASSWORD_LENGTH) {
            return ValidationResult.error("密码长度过长，最多20个字符");
        }
        
        // 复杂度检查 - 必须包含字母和数字
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            return ValidationResult.error("密码必须包含字母和数字，可包含特殊字符@$!%*#?&");
        }
        
        return ValidationResult.success();
    }

    /**
     * 验证用户名格式和长度
     * 边界值测试点：1字符、2字符、20字符、21字符
     */
    public static ValidationResult validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return ValidationResult.error("用户名不能为空");
        }
        
        username = username.trim();
        
        // 长度检查
        if (username.length() < MIN_USERNAME_LENGTH) {
            return ValidationResult.error("用户名长度过短，至少2个字符");
        }
        if (username.length() > MAX_USERNAME_LENGTH) {
            return ValidationResult.error("用户名长度过长，最多20个字符");
        }
        
        // 格式检查 - 只允许中文、英文、数字和下划线
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return ValidationResult.error("用户名只能包含中文、英文、数字和下划线");
        }
        
        return ValidationResult.success();
    }

    /**
     * 验证性别值
     * 等价类测试：有效值、无效值
     */
    public static ValidationResult validateGender(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            return ValidationResult.error("性别不能为空");
        }
        
        gender = gender.trim();
        if (!gender.equals("男") && !gender.equals("女") && !gender.equals("未知")) {
            return ValidationResult.error("性别只能是'男'、'女'或'未知'");
        }
        
        return ValidationResult.success();
    }

    /**
     * 验证地址长度
     * 边界值测试点：4字符、5字符、200字符、201字符
     */
    public static ValidationResult validateAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return ValidationResult.error("地址不能为空");
        }
        
        address = address.trim();
        
        if (address.length() < MIN_ADDRESS_LENGTH) {
            return ValidationResult.error("地址长度过短，至少5个字符");
        }
        if (address.length() > MAX_ADDRESS_LENGTH) {
            return ValidationResult.error("地址长度过长，最多200个字符");
        }
        
        return ValidationResult.success();
    }

    /**
     * 验证商品数量
     * 边界值测试点：0、1、999、1000
     */
    public static ValidationResult validateQuantity(Integer quantity) {
        if (quantity == null) {
            return ValidationResult.error("商品数量不能为空");
        }
        
        if (quantity < MIN_QUANTITY) {
            return ValidationResult.error("商品数量不能小于1");
        }
        if (quantity > MAX_QUANTITY) {
            return ValidationResult.error("商品数量不能大于999");
        }
        
        return ValidationResult.success();
    }

    /**
     * 验证金额
     * 边界值测试点：0、0.01、9999.99、10000
     */
    public static ValidationResult validateAmount(Double amount) {
        if (amount == null) {
            return ValidationResult.error("金额不能为空");
        }
        
        if (amount < MIN_AMOUNT) {
            return ValidationResult.error("金额不能小于0.01元");
        }
        if (amount > MAX_AMOUNT) {
            return ValidationResult.error("金额不能大于9999.99元");
        }
        
        return ValidationResult.success();
    }

    /**
     * 验证订单总金额
     * 边界值测试点：0、0.01、5000、5000.01
     */
    public static ValidationResult validateOrderAmount(Double amount) {
        ValidationResult basicResult = validateAmount(amount);
        if (!basicResult.isSuccess()) {
            return basicResult;
        }
        
        if (amount > MAX_ORDER_AMOUNT) {
            return ValidationResult.error("单次订单金额不能超过5000元");
        }
        
        return ValidationResult.success();
    }

    /**
     * 验证购物车商品种类数量
     * 边界值测试点：0、1、50、51
     */
    public static ValidationResult validateCartItemCount(Integer count) {
        if (count == null) {
            return ValidationResult.error("购物车商品数量不能为空");
        }
        
        if (count < 0) {
            return ValidationResult.error("购物车商品数量不能为负数");
        }
        if (count > MAX_CART_ITEMS) {
            return ValidationResult.error("购物车商品种类不能超过50种");
        }
        
        return ValidationResult.success();
    }

    /**
     * 验证两次密码是否一致
     */
    public static ValidationResult validatePasswordConfirmation(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) {
            return ValidationResult.error("密码确认失败，密码不能为空");
        }
        
        if (!password.equals(confirmPassword)) {
            return ValidationResult.error("两次输入的密码不一致");
        }
        
        return ValidationResult.success();
    }

    /**
     * 验证登录失败次数
     * 边界值测试点：4次、5次、6次
     */
    public static ValidationResult validateLoginAttempts(Integer attempts) {
        if (attempts == null) {
            attempts = 0;
        }
        
        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            return ValidationResult.error("登录失败次数过多，账户已被锁定");
        }
        
        return ValidationResult.success();
    }

    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private boolean success;
        private String message;

        private ValidationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
} 