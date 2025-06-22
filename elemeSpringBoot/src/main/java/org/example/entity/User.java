package org.example.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "user")
@Data  // Lombok 注解，自动生成 getter/setter
public class User {
    @Id
    @Column(name = "phone_number")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phoneNumber;
    
    @Column
    @NotBlank(message = "密码不能为空")
    @Length(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{6,20}$", 
             message = "密码必须包含字母和数字")
    private String password;
    
    @Column
    @Pattern(regexp = "^(男|女|未知)$", message = "性别只能是男、女或未知")
    private String gender;
    
    @Column
    @NotBlank(message = "用户名不能为空")
    @Length(min = 2, max = 20, message = "用户名长度必须在2-20位之间")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5A-Za-z0-9_]{2,20}$", 
             message = "用户名只能包含中文、英文、数字和下划线")
    private String name;
    
    @Column
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Length(min = 5, max = 100, message = "邮箱长度必须在5-100位之间")
    private String email;
    
    // 新增字段用于测试
    @Column(name = "login_attempts")
    @Min(value = 0, message = "登录失败次数不能为负数")
    @Max(value = 10, message = "登录失败次数超出限制")
    private Integer loginAttempts = 0;
    
    @Column(name = "account_locked")
    private Boolean accountLocked = false;
    
    // 如果不使用 Lombok，需要手动添加 getter/setter
    /*
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // ... 其他 getter/setter
    */
}
