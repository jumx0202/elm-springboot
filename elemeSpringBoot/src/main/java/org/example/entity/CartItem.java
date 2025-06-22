package org.example.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车项目实体类
 * 支持各种边界值测试和业务逻辑验证
 */
@Entity
@Table(name = "cart_item")
@Data
public class CartItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 用户手机号
    @Column(name = "user_phone", nullable = false)
    @NotBlank(message = "用户手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String userPhone;
    
    // 商品ID
    @Column(name = "food_id", nullable = false)
    @NotNull(message = "商品ID不能为空")
    @Min(value = 1, message = "商品ID必须大于0")
    private Integer foodId;
    
    // 商品名称
    @Column(name = "food_name", nullable = false)
    @NotBlank(message = "商品名称不能为空")
    @Length(min = 1, max = 50, message = "商品名称长度必须在1-50字符之间")
    private String foodName;
    
    // 商品数量 - 重要的边界值测试点
    @Column(name = "quantity", nullable = false)
    @NotNull(message = "商品数量不能为空")
    @Min(value = 1, message = "商品数量不能小于1")
    @Max(value = 999, message = "商品数量不能大于999")
    private Integer quantity;
    
    // 单价
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "商品单价不能为空")
    @DecimalMin(value = "0.01", message = "商品单价不能小于0.01元")
    @DecimalMax(value = "9999.99", message = "商品单价不能大于9999.99元")
    private BigDecimal unitPrice;
    
    // 总价 - 自动计算，但也需要验证
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "总价不能为空")
    @DecimalMin(value = "0.01", message = "总价不能小于0.01元")
    @DecimalMax(value = "999999.99", message = "总价不能大于999999.99元")
    private BigDecimal totalPrice;
    
    // 商家ID
    @Column(name = "business_id", nullable = false)
    @NotNull(message = "商家ID不能为空")
    @Min(value = 1, message = "商家ID必须大于0")
    private Integer businessId;
    
    // 商家名称
    @Column(name = "business_name")
    @Length(min = 1, max = 50, message = "商家名称长度必须在1-50字符之间")
    private String businessName;
    
    // 备注
    @Column(name = "remarks")
    @Length(max = 200, message = "备注不能超过200字符")
    private String remarks;
    
    // 添加时间
    @Column(name = "created_time", nullable = false)
    @NotNull(message = "创建时间不能为空")
    private LocalDateTime createdTime;
    
    // 更新时间
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
    
    // 是否有效 (0:无效 1:有效)
    @Column(name = "is_valid", nullable = false)
    @NotNull(message = "有效状态不能为空")
    @Min(value = 0, message = "有效状态只能是0或1")
    @Max(value = 1, message = "有效状态只能是0或1")
    private Integer isValid = 1;
    
    /**
     * 业务方法：计算总价
     * 用于白盒测试的条件分支覆盖
     */
    public void calculateTotalPrice() {
        if (this.quantity != null && this.unitPrice != null) {
            this.totalPrice = this.unitPrice.multiply(new BigDecimal(this.quantity));
        }
    }
    
    /**
     * 业务方法：验证购物车项目是否有效
     * 用于白盒测试的复杂条件判断
     */
    public boolean isValidCartItem() {
        // 多条件判断 - 支持白盒测试
        if (this.quantity == null || this.quantity <= 0) {
            return false;
        }
        
        if (this.unitPrice == null || this.unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        if (this.foodId == null || this.foodId <= 0) {
            return false;
        }
        
        if (this.userPhone == null || this.userPhone.trim().isEmpty()) {
            return false;
        }
        
        if (this.businessId == null || this.businessId <= 0) {
            return false;
        }
        
        // 检查总价是否正确
        BigDecimal expectedTotal = this.unitPrice.multiply(new BigDecimal(this.quantity));
        if (this.totalPrice == null || this.totalPrice.compareTo(expectedTotal) != 0) {
            return false;
        }
        
        return this.isValid != null && this.isValid == 1;
    }
    
    /**
     * 业务方法：获取购物车项目状态描述
     * 用于等价类划分测试
     */
    public String getStatusDescription() {
        if (this.isValid == null) {
            return "状态未知";
        }
        
        switch (this.isValid) {
            case 0:
                return "已失效";
            case 1:
                return "有效";
            default:
                return "状态异常";
        }
    }
    
    /**
     * 业务方法：检查是否可以增加数量
     * 用于边界值测试
     */
    public boolean canIncreaseQuantity(int increment) {
        if (this.quantity == null || increment <= 0) {
            return false;
        }
        
        int newQuantity = this.quantity + increment;
        return newQuantity <= 999; // 最大数量限制
    }
    
    /**
     * 业务方法：检查是否可以减少数量
     * 用于边界值测试
     */
    public boolean canDecreaseQuantity(int decrement) {
        if (this.quantity == null || decrement <= 0) {
            return false;
        }
        
        int newQuantity = this.quantity - decrement;
        return newQuantity >= 1; // 最小数量限制
    }
    
    @PrePersist
    public void prePersist() {
        this.createdTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
        calculateTotalPrice();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedTime = LocalDateTime.now();
        calculateTotalPrice();
    }
} 