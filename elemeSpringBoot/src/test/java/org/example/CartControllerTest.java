package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.controller.CartController;
import org.example.entity.CartItem;
import org.example.service.ICartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CartController单元测试类
 * 测试购物车控制器的各种功能
 */
@WebMvcTest(CartController.class)
@DisplayName("购物车控制器测试")
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ICartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("添加商品到购物车测试")
    class AddToCartTest {

        @Test
        @DisplayName("成功添加商品到购物车")
        void testAddToCartSuccess() throws Exception {
            // 准备测试数据 - 使用Map避免Lombok问题
            Map<String, Object> cartItemData = new HashMap<>();
            cartItemData.put("userPhone", "13812345678");
            cartItemData.put("foodId", 1);
            cartItemData.put("quantity", 2);
            cartItemData.put("price", 25.50);

            // Mock服务调用
            when(cartService.addToCart(any(CartItem.class))).thenReturn(0);

            // 执行测试
            mockMvc.perform(post("/cart/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(cartItemData)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("添加成功"));

            // 验证服务调用
            verify(cartService, times(1)).addToCart(any(CartItem.class));
        }

        @Test
        @DisplayName("添加商品失败 - 商品信息无效")
        void testAddToCartInvalidFood() throws Exception {
            Map<String, Object> cartItemData = new HashMap<>();
            cartItemData.put("userPhone", "13812345678");
            cartItemData.put("foodId", -1); // 无效商品ID
            cartItemData.put("quantity", 2);

            when(cartService.addToCart(any(CartItem.class))).thenReturn(1);

            mockMvc.perform(post("/cart/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(cartItemData)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value("商品信息无效"));
        }

        @Test
        @DisplayName("添加商品失败 - 用户信息无效")
        void testAddToCartInvalidUser() throws Exception {
            Map<String, Object> cartItemData = new HashMap<>();
            cartItemData.put("userPhone", "12812345678"); // 无效手机号
            cartItemData.put("foodId", 1);
            cartItemData.put("quantity", 2);

            when(cartService.addToCart(any(CartItem.class))).thenReturn(2);

            mockMvc.perform(post("/cart/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(cartItemData)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value("用户信息无效"));
        }

        @Test
        @DisplayName("添加商品失败 - 数量超出限制")
        void testAddToCartQuantityLimit() throws Exception {
            Map<String, Object> cartItemData = new HashMap<>();
            cartItemData.put("userPhone", "13812345678");
            cartItemData.put("foodId", 1);
            cartItemData.put("quantity", 1000); // 超出限制

            when(cartService.addToCart(any(CartItem.class))).thenReturn(3);

            mockMvc.perform(post("/cart/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(cartItemData)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value("商品数量超出限制(1-999)"));
        }

        @Test
        @DisplayName("添加商品失败 - 价格超出限制")
        void testAddToCartPriceLimit() throws Exception {
            Map<String, Object> cartItemData = new HashMap<>();
            cartItemData.put("userPhone", "13812345678");
            cartItemData.put("foodId", 1);
            cartItemData.put("quantity", 1);
            cartItemData.put("price", 10000.0); // 超出限制

            when(cartService.addToCart(any(CartItem.class))).thenReturn(4);

            mockMvc.perform(post("/cart/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(cartItemData)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("商品价格超出限制(0.01-9999.99)"));
        }

        @Test
        @DisplayName("添加商品失败 - 购物车已满")
        void testAddToCartFull() throws Exception {
            Map<String, Object> cartItemData = new HashMap<>();
            cartItemData.put("userPhone", "13812345678");
            cartItemData.put("foodId", 1);
            cartItemData.put("quantity", 1);

            when(cartService.addToCart(any(CartItem.class))).thenReturn(5);

            mockMvc.perform(post("/cart/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(cartItemData)))
                    .andExpect(status().isTooManyRequests())
                    .andExpect(jsonPath("$.code").value(429))
                    .andExpect(jsonPath("$.message").value("购物车商品种类已达上限(50种)"));
        }

        @Test
        @DisplayName("添加商品异常 - 请求参数为空")
        void testAddToCartNullRequest() throws Exception {
            mockMvc.perform(post("/cart/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("null"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value("请求参数不能为空"));
        }

        @Test
        @DisplayName("添加商品异常 - 服务层抛出异常")
        void testAddToCartServiceException() throws Exception {
            Map<String, Object> cartItemData = new HashMap<>();
            cartItemData.put("userPhone", "13812345678");
            cartItemData.put("foodId", 1);

            when(cartService.addToCart(any(CartItem.class)))
                    .thenThrow(new RuntimeException("数据库连接失败"));

            mockMvc.perform(post("/cart/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(cartItemData)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("服务器内部错误: 数据库连接失败"));
        }
    }

    @Nested
    @DisplayName("更新购物车商品数量测试")
    class UpdateQuantityTest {

        @Test
        @DisplayName("成功更新商品数量")
        void testUpdateQuantitySuccess() throws Exception {
            when(cartService.updateQuantity(1L, 5)).thenReturn(0);

            mockMvc.perform(put("/cart/update/1")
                    .param("quantity", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("更新成功"));

            verify(cartService, times(1)).updateQuantity(1L, 5);
        }

        @Test
        @DisplayName("更新失败 - 商品ID无效")
        void testUpdateQuantityInvalidId() throws Exception {
            mockMvc.perform(put("/cart/update/0")
                    .param("quantity", "5"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value("商品ID无效"));

            verify(cartService, never()).updateQuantity(any(), any());
        }

        @Test
        @DisplayName("更新失败 - 数量为空")
        void testUpdateQuantityNullQuantity() throws Exception {
            mockMvc.perform(put("/cart/update/1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("更新失败 - 数量无效")
        void testUpdateQuantityInvalidQuantity() throws Exception {
            when(cartService.updateQuantity(1L, 1000)).thenReturn(2);

            mockMvc.perform(put("/cart/update/1")
                    .param("quantity", "1000"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value("数量无效(1-999)"));
        }

        @Test
        @DisplayName("更新失败 - 商品不存在")
        void testUpdateQuantityNotFound() throws Exception {
            when(cartService.updateQuantity(999L, 5)).thenReturn(3);

            mockMvc.perform(put("/cart/update/999")
                    .param("quantity", "5"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.message").value("商品不存在"));
        }

        @Test
        @DisplayName("更新失败 - 商品已失效")
        void testUpdateQuantityGone() throws Exception {
            when(cartService.updateQuantity(1L, 5)).thenReturn(4);

            mockMvc.perform(put("/cart/update/1")
                    .param("quantity", "5"))
                    .andExpect(status().isGone())
                    .andExpect(jsonPath("$.code").value(410))
                    .andExpect(jsonPath("$.message").value("商品已失效"));
        }
    }

    @Nested
    @DisplayName("获取购物车列表测试")
    class GetCartItemsTest {

        @Test
        @DisplayName("成功获取购物车列表")
        void testGetCartItemsSuccess() throws Exception {
            List<Map<String, Object>> mockCart = Arrays.asList(
                    createMockCartItem(1, "汉堡", 2, 25.0),
                    createMockCartItem(2, "可乐", 1, 5.0)
            );

            when(cartService.getCartItems("13812345678")).thenReturn(Arrays.asList());

            mockMvc.perform(get("/cart/list")
                    .param("userPhone", "13812345678")
                    .param("page", "1")
                    .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.items").isArray())
                    .andExpect(jsonPath("$.data.items.length()").value(2));
        }

        @Test
        @DisplayName("获取购物车列表 - 使用默认分页参数")
        void testGetCartItemsDefaultParams() throws Exception {
            when(cartService.getCartItems("13812345678")).thenReturn(Arrays.asList());

            mockMvc.perform(get("/cart/list")
                    .param("userPhone", "13812345678"))
                    .andExpect(status().isOk());

            verify(cartService, times(1)).getCartItems("13812345678");
        }

        @Test
        @DisplayName("获取购物车列表失败 - 手机号无效")
        void testGetCartItemsInvalidPhone() throws Exception {
            when(cartService.getCartItems("invalid")).thenReturn(null);

            mockMvc.perform(get("/cart/list")
                    .param("userPhone", "invalid")
                    .param("page", "1")
                    .param("size", "10"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value("用户手机号无效"));
        }
    }

    @Nested
    @DisplayName("删除购物车商品测试")
    class RemoveFromCartTest {

        @Test
        @DisplayName("成功删除购物车商品")
        void testRemoveFromCartSuccess() throws Exception {
            when(cartService.removeFromCart(1L)).thenReturn(0);

            mockMvc.perform(delete("/cart/remove/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("删除成功"));

            verify(cartService, times(1)).removeFromCart(1L);
        }

        @Test
        @DisplayName("删除失败 - 商品不存在")
        void testRemoveFromCartNotFound() throws Exception {
            when(cartService.removeFromCart(999L)).thenReturn(1);

            mockMvc.perform(delete("/cart/remove/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.message").value("商品不存在"));
        }

        @Test
        @DisplayName("删除失败 - 无效商品ID")
        void testRemoveFromCartInvalidId() throws Exception {
            mockMvc.perform(delete("/cart/remove/0"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value("商品ID无效"));

            verify(cartService, never()).removeFromCart(any());
        }
    }

    @Nested
    @DisplayName("清空购物车测试")
    class ClearCartTest {

        @Test
        @DisplayName("成功清空购物车")
        void testClearCartSuccess() throws Exception {
            when(cartService.clearCart("13812345678")).thenReturn(0);

            mockMvc.perform(delete("/cart/clear")
                    .param("userPhone", "13812345678"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("清空成功"));
        }

        @Test
        @DisplayName("清空失败 - 用户不存在")
        void testClearCartUserNotFound() throws Exception {
            when(cartService.clearCart("19999999999")).thenReturn(1);

            mockMvc.perform(delete("/cart/clear")
                    .param("userPhone", "19999999999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.message").value("用户不存在"));
        }
    }

    @Nested
    @DisplayName("购物车验证测试")
    class ValidateCartTest {

        @Test
        @DisplayName("购物车验证通过")
        void testValidateCartSuccess() throws Exception {
            when(cartService.validateCartForCheckout("13812345678")).thenReturn(0);

            mockMvc.perform(get("/cart/validate")
                    .param("userPhone", "13812345678"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("购物车验证通过"));
        }

        @Test
        @DisplayName("购物车验证失败 - 购物车为空")
        void testValidateCartEmpty() throws Exception {
            when(cartService.validateCartForCheckout("13812345678")).thenReturn(1);

            mockMvc.perform(get("/cart/validate")
                    .param("userPhone", "13812345678"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value("购物车为空"));
        }

        @Test
        @DisplayName("购物车验证失败 - 包含失效商品")
        void testValidateCartInvalidItems() throws Exception {
            when(cartService.validateCartForCheckout("13812345678")).thenReturn(2);

            mockMvc.perform(get("/cart/validate")
                    .param("userPhone", "13812345678"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value("购物车包含失效商品"));
        }
    }

    /**
     * 创建模拟购物车商品数据
     */
    private Map<String, Object> createMockCartItem(Integer foodId, String foodName, Integer quantity, Double price) {
        Map<String, Object> item = new HashMap<>();
        item.put("foodId", foodId);
        item.put("foodName", foodName);
        item.put("quantity", quantity);
        item.put("price", price);
        item.put("totalPrice", quantity * price);
        return item;
    }
}