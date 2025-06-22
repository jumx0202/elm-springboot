package org.example.service;

import org.example.entity.CartItem;
import java.util.List;

/**
 * 购物车服务接口
 * 定义购物车相关的业务操作
 */
public interface ICartService {
    
    /**
     * 添加商品到购物车
     * @param cartItem 购物车项目
     * @return 添加结果状态码
     */
    Integer addToCart(CartItem cartItem);
    
    /**
     * 更新购物车商品数量
     * @param itemId 商品项目ID
     * @param quantity 新数量
     * @return 更新结果状态码
     */
    Integer updateQuantity(Long itemId, Integer quantity);
    
    /**
     * 从购物车移除商品
     * @param itemId 商品项目ID
     * @return 移除是否成功
     */
    Boolean removeFromCart(Long itemId);
    
    /**
     * 获取用户购物车列表
     * @param userPhoneNumber 用户手机号
     * @return 购物车项目列表
     */
    List<CartItem> getCartItems(String userPhoneNumber);
    
    /**
     * 清空用户购物车
     * @param userPhoneNumber 用户手机号
     * @return 清空是否成功
     */
    Boolean clearCart(String userPhoneNumber);
    
    /**
     * 计算购物车总金额
     * @param userPhoneNumber 用户手机号
     * @return 总金额
     */
    Double calculateTotalAmount(String userPhoneNumber);
    
    /**
     * 验证购物车是否可以结算
     * @param userPhoneNumber 用户手机号
     * @return 验证结果
     */
    Boolean validateCartForCheckout(String userPhoneNumber);
} 