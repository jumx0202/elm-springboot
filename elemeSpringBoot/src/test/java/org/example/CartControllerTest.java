package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.CartItem;
import org.example.service.ICartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CartController单元测试类
 * 测试购物车控制器的各种功能
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.sql.init.mode=never"
})
@DisplayName("购物车控制器测试")
class CartControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private ICartService cartService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("成功添加商品到购物车")
    void testAddToCartSuccess() throws Exception {
        CartItem cartItem = createMockCartItem("13912345678", 1, 2, new BigDecimal("25.50"));
        
        when(cartService.addToCart(any(CartItem.class))).thenReturn(0);

        mockMvc.perform(post("/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartItem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("添加成功"));

        verify(cartService, times(1)).addToCart(any(CartItem.class));
    }

    @Test
    @DisplayName("获取购物车列表成功")
    void testGetCartItemsSuccess() throws Exception {
        List<CartItem> cartItems = Arrays.asList(
            createMockCartItem("13912345678", 1, 2, new BigDecimal("25.50")),
            createMockCartItem("13912345678", 2, 1, new BigDecimal("15.00"))
        );

        when(cartService.getCartItems("13912345678")).thenReturn(cartItems);

        mockMvc.perform(get("/cart/list")
                .param("userPhone", "13912345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.items").isArray());

        verify(cartService, times(1)).getCartItems("13912345678");
    }

    @Test
    @DisplayName("删除购物车商品成功")
    void testRemoveFromCartSuccess() throws Exception {
        when(cartService.removeFromCart(1L)).thenReturn(true);

        mockMvc.perform(delete("/cart/remove/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"));

        verify(cartService, times(1)).removeFromCart(1L);
    }

    @Test
    @DisplayName("清空购物车成功")
    void testClearCartSuccess() throws Exception {
        when(cartService.clearCart("13912345678")).thenReturn(true);

        mockMvc.perform(delete("/cart/clear")
                .param("userPhone", "13912345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("清空成功"));

        verify(cartService, times(1)).clearCart("13912345678");
    }

    @Test
    @DisplayName("验证购物车成功")
    void testValidateCartSuccess() throws Exception {
        when(cartService.validateCartForCheckout("13912345678")).thenReturn(true);

        mockMvc.perform(get("/cart/validate")
                .param("userPhone", "13912345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("验证完成"));

        verify(cartService, times(1)).validateCartForCheckout("13912345678");
    }

    private CartItem createMockCartItem(String userPhone, Integer foodId, Integer quantity, BigDecimal unitPrice) {
        CartItem cartItem = new CartItem();
        cartItem.setUserPhone(userPhone);
        cartItem.setFoodId(foodId);
        cartItem.setQuantity(quantity);
        cartItem.setUnitPrice(unitPrice);
        cartItem.setFoodName("测试商品");
        cartItem.setBusinessId(1);
        cartItem.setBusinessName("测试商家");
        // 设置必需字段以通过验证
        cartItem.setTotalPrice(unitPrice.multiply(new BigDecimal(quantity)));
        cartItem.setCreatedTime(java.time.LocalDateTime.now());
        cartItem.setIsValid(1);
        return cartItem;
    }
}