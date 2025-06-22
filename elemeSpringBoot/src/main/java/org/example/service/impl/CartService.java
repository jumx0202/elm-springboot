package org.example.service.impl;

import org.example.entity.CartItem;
import org.example.service.ICartService;
import org.example.util.ValidationUtils;
import org.example.util.ValidationUtils.ValidationResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 购物车服务实现类
 * 包含丰富的业务逻辑和验证，支持各种类型的测试
 */
@Service
public class CartService implements ICartService {
    
    // 使用内存存储模拟数据库，便于测试
    private final ConcurrentHashMap<Long, CartItem> cartItemStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    /**
     * 添加商品到购物车
     * 返回值说明（支持等价类划分测试）：
     * 0: 添加成功
     * 1: 商品信息无效
     * 2: 用户信息无效
     * 3: 数量超出限制
     * 4: 价格超出限制
     * 5: 购物车商品种类已满
     * -1: 系统异常
     */
    @Override
    public Integer addToCart(CartItem cartItem) {
        try {
            // 1. 基本验证
            if (cartItem == null) {
                return 1; // 商品信息无效
            }
            
            // 2. 用户手机号验证 - 支持边界值测试
            ValidationResult phoneValidation = ValidationUtils.validatePhoneNumber(cartItem.getUserPhone());
            if (!phoneValidation.isSuccess()) {
                System.out.println("用户手机号验证失败: " + phoneValidation.getMessage());
                return 2; // 用户信息无效
            }
            
            // 3. 商品数量验证 - 支持边界值测试（1-999）
            ValidationResult quantityValidation = ValidationUtils.validateQuantity(cartItem.getQuantity());
            if (!quantityValidation.isSuccess()) {
                System.out.println("商品数量验证失败: " + quantityValidation.getMessage());
                return 3; // 数量超出限制
            }
            
            // 4. 商品价格验证 - 支持边界值测试
            if (cartItem.getUnitPrice() == null || 
                cartItem.getUnitPrice().compareTo(new BigDecimal("0.01")) < 0 ||
                cartItem.getUnitPrice().compareTo(new BigDecimal("9999.99")) > 0) {
                return 4; // 价格超出限制
            }
            
            // 5. 检查用户购物车商品种类数量 - 支持边界值测试（最多50种）
            int userCartItemCount = countUserCartItems(cartItem.getUserPhone());
            ValidationResult cartCountValidation = ValidationUtils.validateCartItemCount(userCartItemCount + 1);
            if (!cartCountValidation.isSuccess()) {
                System.out.println("购物车商品种类验证失败: " + cartCountValidation.getMessage());
                return 5; // 购物车商品种类已满
            }
            
            // 6. 检查是否已存在相同商品 - 支持等价类测试
            CartItem existingItem = findExistingCartItem(cartItem.getUserPhone(), cartItem.getFoodId());
            if (existingItem != null) {
                // 更新数量而不是添加新项目
                int newQuantity = existingItem.getQuantity() + cartItem.getQuantity();
                return updateQuantity(existingItem.getId(), newQuantity);
            }
            
            // 7. 添加新商品到购物车
            cartItem.setId(idGenerator.getAndIncrement());
            cartItem.calculateTotalPrice();
            cartItem.setIsValid(1);
            
            cartItemStore.put(cartItem.getId(), cartItem);
            return 0; // 添加成功
            
        } catch (Exception e) {
            System.err.println("添加商品到购物车时发生异常: " + e.getMessage());
            e.printStackTrace();
            return -1; // 系统异常
        }
    }
    
    /**
     * 更新购物车商品数量
     * 包含复杂的条件判断，支持白盒测试
     */
    @Override
    public Integer updateQuantity(Long itemId, Integer quantity) {
        try {
            // 1. 参数验证
            if (itemId == null || itemId <= 0) {
                return 1; // 商品ID无效
            }
            
            // 2. 数量验证 - 支持边界值测试
            ValidationResult quantityValidation = ValidationUtils.validateQuantity(quantity);
            if (!quantityValidation.isSuccess()) {
                return 2; // 数量无效
            }
            
            // 3. 查找购物车项目
            CartItem cartItem = cartItemStore.get(itemId);
            if (cartItem == null) {
                return 3; // 商品不存在
            }
            
            // 4. 检查商品是否有效
            if (!cartItem.isValidCartItem()) {
                return 4; // 商品已失效
            }
            
            // 5. 多重条件判断 - 支持白盒测试的分支覆盖
            if (quantity.equals(cartItem.getQuantity())) {
                return 0; // 数量未变化，直接返回成功
            } else if (quantity > cartItem.getQuantity()) {
                // 增加数量的情况
                int increment = quantity - cartItem.getQuantity();
                if (!cartItem.canIncreaseQuantity(increment)) {
                    return 5; // 超出最大数量限制
                }
            } else {
                // 减少数量的情况
                int decrement = cartItem.getQuantity() - quantity;
                if (!cartItem.canDecreaseQuantity(decrement)) {
                    return 6; // 低于最小数量限制
                }
            }
            
            // 6. 更新数量和总价
            cartItem.setQuantity(quantity);
            cartItem.calculateTotalPrice();
            
            return 0; // 更新成功
            
        } catch (Exception e) {
            System.err.println("更新购物车商品数量时发生异常: " + e.getMessage());
            return -1; // 系统异常
        }
    }
    
    /**
     * 从购物车移除商品
     */
    @Override
    public Boolean removeFromCart(Long itemId) {
        try {
            if (itemId == null || itemId <= 0) {
                return false; // 商品ID无效
            }
            
            CartItem removedItem = cartItemStore.remove(itemId);
            if (removedItem == null) {
                return false; // 商品不存在
            }
            
            return true; // 移除成功
            
        } catch (Exception e) {
            System.err.println("从购物车移除商品时发生异常: " + e.getMessage());
            return false; // 系统异常
        }
    }
    
    /**
     * 获取用户购物车列表
     * 包含条件筛选逻辑，支持白盒测试
     */
    @Override
    public List<CartItem> getCartItems(String userPhoneNumber) {
        List<CartItem> userCartItems = new ArrayList<>();
        
        try {
            // 1. 手机号验证
            ValidationResult phoneValidation = ValidationUtils.validatePhoneNumber(userPhoneNumber);
            if (!phoneValidation.isSuccess()) {
                return userCartItems; // 返回空列表
            }
            
            // 2. 遍历查找用户的购物车项目
            for (CartItem item : cartItemStore.values()) {
                // 多重条件判断 - 支持白盒测试
                if (item != null && 
                    item.getUserPhone() != null && 
                    item.getUserPhone().equals(userPhoneNumber) && 
                    item.getIsValid() != null && 
                    item.getIsValid() == 1) {
                    userCartItems.add(item);
                }
            }
            
            return userCartItems;
            
        } catch (Exception e) {
            System.err.println("获取用户购物车列表时发生异常: " + e.getMessage());
            return userCartItems; // 返回空列表
        }
    }
    
    /**
     * 清空用户购物车
     */
    @Override
    public Boolean clearCart(String userPhoneNumber) {
        try {
            ValidationResult phoneValidation = ValidationUtils.validatePhoneNumber(userPhoneNumber);
            if (!phoneValidation.isSuccess()) {
                return false; // 手机号无效
            }
            
            List<Long> itemsToRemove = new ArrayList<>();
            
            // 找到所有需要删除的项目
            for (CartItem item : cartItemStore.values()) {
                if (item != null && 
                    item.getUserPhone() != null && 
                    item.getUserPhone().equals(userPhoneNumber)) {
                    itemsToRemove.add(item.getId());
                }
            }
            
            // 删除项目
            for (Long itemId : itemsToRemove) {
                cartItemStore.remove(itemId);
            }
            
            return true; // 清空成功
            
        } catch (Exception e) {
            System.err.println("清空用户购物车时发生异常: " + e.getMessage());
            return false; // 系统异常
        }
    }
    
    /**
     * 计算购物车总金额
     * 包含循环和累加逻辑，支持白盒测试
     */
    @Override
    public Double calculateTotalAmount(String userPhoneNumber) {
        try {
            ValidationResult phoneValidation = ValidationUtils.validatePhoneNumber(userPhoneNumber);
            if (!phoneValidation.isSuccess()) {
                return 0.0;
            }
            
            BigDecimal totalAmount = BigDecimal.ZERO;
            int itemCount = 0;
            
            // 遍历计算总金额 - 支持白盒测试的循环覆盖
            for (CartItem item : cartItemStore.values()) {
                if (item != null && 
                    item.getUserPhone() != null && 
                    item.getUserPhone().equals(userPhoneNumber) && 
                    item.getIsValid() != null && 
                    item.getIsValid() == 1 &&
                    item.getTotalPrice() != null) {
                    
                    totalAmount = totalAmount.add(item.getTotalPrice());
                    itemCount++;
                }
            }
            
            // 边界值检查
            if (totalAmount.compareTo(new BigDecimal(ValidationUtils.MAX_ORDER_AMOUNT)) > 0) {
                System.out.println("购物车总金额超出单次订单限制");
                return -1.0; // 表示超出限制
            }
            
            return totalAmount.doubleValue();
            
        } catch (Exception e) {
            System.err.println("计算购物车总金额时发生异常: " + e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * 验证购物车是否可以结算
     * 复杂的验证逻辑，支持白盒测试的多条件分支
     */
    @Override
    public Boolean validateCartForCheckout(String userPhoneNumber) {
        try {
            // 1. 手机号验证
            ValidationResult phoneValidation = ValidationUtils.validatePhoneNumber(userPhoneNumber);
            if (!phoneValidation.isSuccess()) {
                return false;
            }
            
            // 2. 获取购物车项目
            List<CartItem> cartItems = getCartItems(userPhoneNumber);
            if (cartItems.isEmpty()) {
                return false; // 购物车为空
            }
            
            // 3. 检查每个项目的有效性
            for (CartItem item : cartItems) {
                if (!item.isValidCartItem()) {
                    return false; // 存在无效项目
                }
            }
            
            // 4. 检查总金额
            Double totalAmount = calculateTotalAmount(userPhoneNumber);
            if (totalAmount == null || totalAmount <= 0) {
                return false; // 总金额异常
            }
            
            // 5. 检查是否超出订单限制
            ValidationResult amountValidation = ValidationUtils.validateOrderAmount(totalAmount);
            if (!amountValidation.isSuccess()) {
                return false; // 超出订单金额限制
            }
            
            return true; // 可以结算
            
        } catch (Exception e) {
            System.err.println("验证购物车结算条件时发生异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 私有辅助方法：统计用户购物车商品种类数量
     * 用于边界值测试
     */
    private int countUserCartItems(String userPhoneNumber) {
        int count = 0;
        for (CartItem item : cartItemStore.values()) {
            if (item != null && 
                item.getUserPhone() != null && 
                item.getUserPhone().equals(userPhoneNumber) && 
                item.getIsValid() != null && 
                item.getIsValid() == 1) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 私有辅助方法：查找现有的购物车项目
     * 用于等价类划分测试
     */
    private CartItem findExistingCartItem(String userPhoneNumber, Integer foodId) {
        for (CartItem item : cartItemStore.values()) {
            if (item != null && 
                item.getUserPhone() != null && 
                item.getUserPhone().equals(userPhoneNumber) && 
                item.getFoodId() != null && 
                item.getFoodId().equals(foodId) && 
                item.getIsValid() != null && 
                item.getIsValid() == 1) {
                return item;
            }
        }
        return null;
    }
    
    /**
     * 获取购物车统计信息 - 用于系统测试
     */
    public String getCartStatistics(String userPhoneNumber) {
        try {
            List<CartItem> cartItems = getCartItems(userPhoneNumber);
            int itemCount = cartItems.size();
            Double totalAmount = calculateTotalAmount(userPhoneNumber);
            
            // 复杂的字符串构建逻辑
            StringBuilder stats = new StringBuilder();
            stats.append("用户: ").append(userPhoneNumber).append("\n");
            stats.append("商品种类: ").append(itemCount).append("\n");
            stats.append("总金额: ").append(totalAmount).append("元\n");
            
            if (itemCount == 0) {
                stats.append("状态: 购物车为空");
            } else if (totalAmount > ValidationUtils.MAX_ORDER_AMOUNT) {
                stats.append("状态: 超出订单限制");
            } else {
                stats.append("状态: 可以结算");
            }
            
            return stats.toString();
            
        } catch (Exception e) {
            return "统计信息获取失败: " + e.getMessage();
        }
    }
} 