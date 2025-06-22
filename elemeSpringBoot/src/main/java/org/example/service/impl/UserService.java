package org.example.service.impl;

import jakarta.annotation.Resource;
import org.example.entity.User;
import org.example.mapper.IUserMapper;
import org.example.service.IUserService;
import org.example.util.ValidationUtils;
import org.example.util.ValidationUtils.ValidationResult;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserService implements IUserService {
    //资源
    @Resource
    private IUserMapper userMapper;
    
    /**
     * 用户登录 - 包含复杂的业务逻辑用于白盒测试
     * 支持多种分支和条件覆盖测试
     */
    @Override
    public User login(String phoneNumber, String password) {
        try {
            // 1. 参数验证 - 支持边界值测试
            ValidationResult phoneValidation = ValidationUtils.validatePhoneNumber(phoneNumber);
            if (!phoneValidation.isSuccess()) {
                System.out.println("手机号验证失败: " + phoneValidation.getMessage());
                return null;
            }
            
            ValidationResult passwordValidation = ValidationUtils.validatePassword(password);
            if (!passwordValidation.isSuccess()) {
                System.out.println("密码验证失败: " + passwordValidation.getMessage());
                return null;
            }
            
            // 2. 查询用户
            User user = userMapper.findByPhoneNumberAndPassword(phoneNumber, password);
            if (user == null) {
                // 增加登录失败次数
                incrementLoginFailureCount(phoneNumber);
                return null;
            }
            
            // 3. 检查账户是否被锁定 - 支持等价类划分测试
            if (isAccountLocked(user)) {
                System.out.println("账户已被锁定");
                return null;
            }
            
            // 4. 检查登录失败次数 - 支持边界值测试
            ValidationResult attemptValidation = ValidationUtils.validateLoginAttempts(user.getLoginAttempts());
            if (!attemptValidation.isSuccess()) {
                lockUserAccount(phoneNumber);
                System.out.println("登录失败次数过多，账户已被锁定");
                return null;
            }
            
            // 5. 登录成功，重置失败次数
            resetLoginFailureCount(phoneNumber);
            
            return user;
            
        } catch (Exception e) {
            System.err.println("登录过程中发生异常: " + e.getMessage());
            return null;
        }
    }

    /**
     * 用户注册 - 包含全面的验证逻辑
     * 返回值支持等价类划分测试：
     * 0: 注册成功
     * 1: 密码不一致
     * 2: 手机号已注册
     * 3: 密码长度不足
     * 4: 邮箱格式错误
     * 5: 手机号格式错误
     * 6: 用户名格式错误
     * -1: 系统异常
     */
    @Override
    public Integer register(String phoneNumber, String password, String confirmPassword, 
                          String name, String email) {
        try {
            // 1. 手机号验证 - 支持边界值和格式测试
            ValidationResult phoneValidation = ValidationUtils.validatePhoneNumber(phoneNumber);
            if (!phoneValidation.isSuccess()) {
                System.out.println("手机号验证失败: " + phoneValidation.getMessage());
                return 5; // 手机号格式错误
            }
            
            // 2. 检查手机号是否已注册 - 支持等价类测试
            if (userMapper.existsByPhoneNumber(phoneNumber)) {
                return 2; // 手机号已注册
            }
            
            // 3. 密码验证 - 支持边界值测试
            ValidationResult passwordValidation = ValidationUtils.validatePassword(password);
            if (!passwordValidation.isSuccess()) {
                System.out.println("密码验证失败: " + passwordValidation.getMessage());
                return 3; // 密码长度不足或格式错误
            }
            
            // 4. 确认密码验证
            ValidationResult confirmValidation = ValidationUtils.validatePasswordConfirmation(password, confirmPassword);
            if (!confirmValidation.isSuccess()) {
                return 1; // 密码不一致
            }
            
            // 5. 邮箱验证 - 支持边界值和格式测试
            ValidationResult emailValidation = ValidationUtils.validateEmail(email);
            if (!emailValidation.isSuccess()) {
                System.out.println("邮箱验证失败: " + emailValidation.getMessage());
                return 4; // 邮箱格式错误
            }
            
            // 6. 用户名验证 - 支持边界值测试
            ValidationResult nameValidation = ValidationUtils.validateUsername(name);
            if (!nameValidation.isSuccess()) {
                System.out.println("用户名验证失败: " + nameValidation.getMessage());
                return 6; // 用户名格式错误
            }
            
            // 7. 创建新用户
            User user = new User();
            user.setPhoneNumber(phoneNumber);
            user.setPassword(password);
            user.setName(name);
            user.setEmail(email);
            user.setGender("未知"); // 默认值
            user.setLoginAttempts(0);
            user.setAccountLocked(false);
            
            userMapper.save(user);
            return 0; // 注册成功
            
        } catch (Exception e) {
            System.err.println("注册过程中发生异常: " + e.getMessage());
            e.printStackTrace();
            return -1; // 系统异常
        }
    }
    
    /**
     * 增加登录失败次数 - 支持边界值测试
     * 测试点：失败次数0-4次时的处理，第5次失败时的锁定逻辑
     */
    private void incrementLoginFailureCount(String phoneNumber) {
        try {
            User user = userMapper.findByPhoneNumber(phoneNumber);
            if (user != null) {
                int currentAttempts = user.getLoginAttempts() != null ? user.getLoginAttempts() : 0;
                int newAttempts = currentAttempts + 1;
                
                user.setLoginAttempts(newAttempts);
                
                // 边界值检查：如果达到最大失败次数，锁定账户
                if (newAttempts >= ValidationUtils.MAX_LOGIN_ATTEMPTS) {
                    user.setAccountLocked(true);
                    System.out.println("用户账户因登录失败次数过多被锁定");
                }
                
                userMapper.update(user);
            }
        } catch (Exception e) {
            System.err.println("更新登录失败次数时发生异常: " + e.getMessage());
        }
    }
    
    /**
     * 重置登录失败次数
     */
    private void resetLoginFailureCount(String phoneNumber) {
        try {
            User user = userMapper.findByPhoneNumber(phoneNumber);
            if (user != null) {
                user.setLoginAttempts(0);
                user.setAccountLocked(false);
                userMapper.update(user);
            }
        } catch (Exception e) {
            System.err.println("重置登录失败次数时发生异常: " + e.getMessage());
        }
    }
    
    /**
     * 锁定用户账户
     */
    private void lockUserAccount(String phoneNumber) {
        try {
            User user = userMapper.findByPhoneNumber(phoneNumber);
            if (user != null) {
                user.setAccountLocked(true);
                userMapper.update(user);
            }
        } catch (Exception e) {
            System.err.println("锁定用户账户时发生异常: " + e.getMessage());
        }
    }
    
    /**
     * 检查账户是否被锁定 - 支持等价类划分测试
     * 等价类：已锁定、未锁定、状态未知
     */
    private boolean isAccountLocked(User user) {
        if (user == null) {
            return true; // 用户不存在，视为锁定
        }
        
        Boolean locked = user.getAccountLocked();
        return locked != null && locked;
    }
    
    /**
     * 验证用户输入的完整性 - 用于单元测试
     * 这个方法包含多个条件分支，适合白盒测试
     */
    public boolean validateUserInput(String phoneNumber, String password, String email, String name) {
        // 多重条件判断 - 支持条件覆盖测试
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        
        if (password == null || password.length() < 6) {
            return false;
        }
        
        if (email == null || !email.contains("@")) {
            return false;
        }
        
        if (name == null || name.trim().length() < 2) {
            return false;
        }
        
        // 复合条件 - 支持条件组合覆盖测试
        return ValidationUtils.validatePhoneNumber(phoneNumber).isSuccess() && 
               ValidationUtils.validatePassword(password).isSuccess() &&
               ValidationUtils.validateEmail(email).isSuccess() &&
               ValidationUtils.validateUsername(name).isSuccess();
    }
    
    /**
     * 计算用户信用等级 - 用于复杂的白盒测试
     * 包含多个嵌套条件和循环
     */
    public String calculateUserCreditLevel(String phoneNumber) {
        try {
            User user = userMapper.findByPhoneNumber(phoneNumber);
            if (user == null) {
                return "无信用记录";
            }
            
            int loginAttempts = user.getLoginAttempts() != null ? user.getLoginAttempts() : 0;
            boolean isLocked = user.getAccountLocked() != null ? user.getAccountLocked() : false;
            
            // 复杂的条件判断逻辑 - 支持白盒测试的分支覆盖
            if (isLocked) {
                return "信用不良";
            } else if (loginAttempts == 0) {
                return "信用优秀";
            } else if (loginAttempts <= 2) {
                return "信用良好";
            } else if (loginAttempts <= 4) {
                return "信用一般";
            } else {
                return "信用较差";
            }
            
        } catch (Exception e) {
            System.err.println("计算用户信用等级时发生异常: " + e.getMessage());
            return "计算异常";
        }
    }
}
