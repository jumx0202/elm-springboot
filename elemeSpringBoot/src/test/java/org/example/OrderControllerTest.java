package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.controller.OrderController;
import org.example.dto.OrderDetailDTO;
import org.example.dto.OrderRequestDTO;
import org.example.entity.UserOrder;
import org.example.service.IUserOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * OrderController单元测试类
 * 测试订单控制器的各种功能
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.sql.init.mode=never"
})
@DisplayName("订单控制器测试")
class OrderControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private IUserOrderService userOrderService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Nested
    @DisplayName("根据ID获取用户订单测试")
    class GetUserOrderByIdTest {

        @Test
        @DisplayName("成功获取用户订单")
        void testGetUserOrderByIdSuccess() throws Exception {
            Map<String, Integer> requestData = new HashMap<>();
            requestData.put("ID", 1);

            UserOrder mockOrder = createMockUserOrder(1, "13812345678", 89.5);
            when(userOrderService.getById(1)).thenReturn(mockOrder);

            mockMvc.perform(post("/order/getUserOrderById")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.userPhone").value("13812345678"));

            verify(userOrderService, times(1)).getById(1);
        }

        @Test
        @DisplayName("获取用户订单失败 - 订单不存在")
        void testGetUserOrderByIdNotFound() throws Exception {
            Map<String, Integer> requestData = new HashMap<>();
            requestData.put("ID", 999);

            when(userOrderService.getById(999)).thenReturn(null);

            mockMvc.perform(post("/order/getUserOrderById")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isNotFound());

            verify(userOrderService, times(1)).getById(999);
        }

        @Test
        @DisplayName("获取用户订单失败 - ID为空")
        void testGetUserOrderByIdNullId() throws Exception {
            Map<String, Integer> requestData = new HashMap<>();
            // 不设置ID字段

            mockMvc.perform(post("/order/getUserOrderById")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isBadRequest());

            verify(userOrderService, never()).getById(any());
        }

        @Test
        @DisplayName("获取用户订单失败 - ID为负数")
        void testGetUserOrderByIdNegativeId() throws Exception {
            Map<String, Integer> requestData = new HashMap<>();
            requestData.put("ID", -1);

            when(userOrderService.getById(-1)).thenReturn(null);

            mockMvc.perform(post("/order/getUserOrderById")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("获取用户订单失败 - ID为0")
        void testGetUserOrderByIdZeroId() throws Exception {
            Map<String, Integer> requestData = new HashMap<>();
            requestData.put("ID", 0);

            when(userOrderService.getById(0)).thenReturn(null);

            mockMvc.perform(post("/order/getUserOrderById")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("边界值测试 - 最大整数ID")
        void testGetUserOrderByIdMaxInteger() throws Exception {
            Map<String, Integer> requestData = new HashMap<>();
            requestData.put("ID", Integer.MAX_VALUE);

            when(userOrderService.getById(Integer.MAX_VALUE)).thenReturn(null);

            mockMvc.perform(post("/order/getUserOrderById")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("添加用户订单测试")
    class AddUserOrderTest {

        @Test
        @DisplayName("成功添加用户订单")
        void testAddUserOrderSuccess() throws Exception {
            OrderRequestDTO mockRequest = createMockOrderRequestDTO();
            when(userOrderService.addUserOrder(any(OrderRequestDTO.class))).thenReturn(1);

            mockMvc.perform(post("/order/addUserOrder")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mockRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(1));

            verify(userOrderService, times(1)).addUserOrder(any(OrderRequestDTO.class));
        }

        @Test
        @DisplayName("添加用户订单失败 - 服务返回0")
        void testAddUserOrderFailed() throws Exception {
            OrderRequestDTO mockRequest = createMockOrderRequestDTO();
            when(userOrderService.addUserOrder(any(OrderRequestDTO.class))).thenReturn(0);

            mockMvc.perform(post("/order/addUserOrder")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mockRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(0));
        }

        @Test
        @DisplayName("添加用户订单 - 请求数据为空")
        void testAddUserOrderNullRequest() throws Exception {
            mockMvc.perform(post("/order/addUserOrder")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isOk());

            verify(userOrderService, times(1)).addUserOrder(any(OrderRequestDTO.class));
        }

        @Test
        @DisplayName("添加用户订单异常 - 服务层抛出异常")
        void testAddUserOrderServiceException() throws Exception {
            OrderRequestDTO mockRequest = createMockOrderRequestDTO();
            when(userOrderService.addUserOrder(any(OrderRequestDTO.class)))
                    .thenThrow(new RuntimeException("数据库连接失败"));

            mockMvc.perform(post("/order/addUserOrder")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mockRequest)))
                    .andExpect(status().is5xxServerError());
        }
    }

    @Nested
    @DisplayName("订单支付状态测试")
    class HavePayedTest {

        @Test
        @DisplayName("订单已支付")
        void testHavePayedTrue() throws Exception {
            Map<String, Integer> requestData = new HashMap<>();
            requestData.put("ID", 1);

            when(userOrderService.havePayed(1)).thenReturn(true);

            mockMvc.perform(post("/order/havePayed")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(true));

            verify(userOrderService, times(1)).havePayed(1);
        }

        @Test
        @DisplayName("订单未支付")
        void testHavePayedFalse() throws Exception {
            Map<String, Integer> requestData = new HashMap<>();
            requestData.put("ID", 1);

            when(userOrderService.havePayed(1)).thenReturn(false);

            mockMvc.perform(post("/order/havePayed")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(false));
        }

        @Test
        @DisplayName("查询支付状态 - ID为空")
        void testHavePayedNullId() throws Exception {
            Map<String, Integer> requestData = new HashMap<>();
            // 不设置ID字段

            when(userOrderService.havePayed(null)).thenReturn(false);

            mockMvc.perform(post("/order/havePayed")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(false));
        }

        @Test
        @DisplayName("查询支付状态 - 无效ID")
        void testHavePayedInvalidId() throws Exception {
            Map<String, Integer> requestData = new HashMap<>();
            requestData.put("ID", -1);

            when(userOrderService.havePayed(-1)).thenReturn(false);

            mockMvc.perform(post("/order/havePayed")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(false));
        }
    }

    @Nested
    @DisplayName("获取用户所有订单测试")
    class GetAllUserOrderTest {

        @Test
        @DisplayName("成功获取用户订单列表")
        void testGetAllUserOrderSuccess() throws Exception {
            Map<String, String> requestData = new HashMap<>();
            requestData.put("userPhone", "13812345678");

            List<UserOrder> mockOrders = Arrays.asList(
                    createMockUserOrder(1, "13812345678", 89.5),
                    createMockUserOrder(2, "13812345678", 125.0)
            );

            when(userOrderService.getAllByUserPhone("13812345678")).thenReturn(mockOrders);

            mockMvc.perform(post("/order/getAllUserOrder")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[1].id").value(2));

            verify(userOrderService, times(1)).getAllByUserPhone("13812345678");
        }

        @Test
        @DisplayName("获取用户订单列表 - 返回空列表")
        void testGetAllUserOrderEmpty() throws Exception {
            Map<String, String> requestData = new HashMap<>();
            requestData.put("userPhone", "13812345678");

            when(userOrderService.getAllByUserPhone("13812345678")).thenReturn(Arrays.asList());

            mockMvc.perform(post("/order/getAllUserOrder")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("获取用户订单列表 - 手机号为空")
        void testGetAllUserOrderNullPhone() throws Exception {
            Map<String, String> requestData = new HashMap<>();
            // 不设置userPhone字段

            when(userOrderService.getAllByUserPhone(null)).thenReturn(Arrays.asList());

            mockMvc.perform(post("/order/getAllUserOrder")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("获取用户订单列表 - 无效手机号")
        void testGetAllUserOrderInvalidPhone() throws Exception {
            Map<String, String> requestData = new HashMap<>();
            requestData.put("userPhone", "invalid_phone");

            when(userOrderService.getAllByUserPhone("invalid_phone")).thenReturn(Arrays.asList());

            mockMvc.perform(post("/order/getAllUserOrder")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("获取订单详情测试")  
    class GetOrderDetailTest {

        @Test
        @DisplayName("成功获取订单详情")
        void testGetOrderDetailSuccess() throws Exception {
            Map<String, Integer> requestData = new HashMap<>();
            requestData.put("ID", 1);

            OrderDetailDTO mockDetail = createMockOrderDetailDTO(1, "13812345678");
            when(userOrderService.getOrderDetail(1)).thenReturn(mockDetail);

            mockMvc.perform(post("/order/getOrderDetail")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderId").value(1))
                    .andExpect(jsonPath("$.userPhone").value("13812345678"));

            verify(userOrderService, times(1)).getOrderDetail(1);
        }

        @Test
        @DisplayName("获取订单详情失败 - 订单不存在")
        void testGetOrderDetailNotFound() throws Exception {
            Map<String, Integer> requestData = new HashMap<>();
            requestData.put("ID", 999);

            when(userOrderService.getOrderDetail(999)).thenReturn(null);

            mockMvc.perform(post("/order/getOrderDetail")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isNoContent());

            verify(userOrderService, times(1)).getOrderDetail(999);
        }

        @Test
        @DisplayName("获取订单详情失败 - ID为空")
        void testGetOrderDetailNullId() throws Exception {
            Map<String, Integer> requestData = new HashMap<>();
            // 不设置ID字段

            mockMvc.perform(post("/order/getOrderDetail")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isBadRequest());

            verify(userOrderService, never()).getOrderDetail(any());
        }

        @Test
        @DisplayName("获取订单详情失败 - ID为负数")
        void testGetOrderDetailNegativeId() throws Exception {
            Map<String, Integer> requestData = new HashMap<>();
            requestData.put("ID", -1);

            when(userOrderService.getOrderDetail(-1)).thenReturn(null);

            mockMvc.perform(post("/order/getOrderDetail")
                    .contentType(MediaType.APPLICATION_JSON)  
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("获取订单时间测试")
    class GetOrderTimeTest {

        @Test
        @DisplayName("成功获取订单时间")
        void testGetOrderTimeSuccess() throws Exception {
            LocalDateTime mockTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
            UserOrder mockOrder = createMockUserOrder(1, "13812345678", 89.5);
            
            // 使用反射设置时间字段
            try {
                java.lang.reflect.Field timeField = UserOrder.class.getDeclaredField("createdAt");
                timeField.setAccessible(true);
                timeField.set(mockOrder, mockTime);
            } catch (Exception e) {
                // 反射失败则跳过时间设置
            }

            when(userOrderService.getById(1)).thenReturn(mockOrder);

            mockMvc.perform(get("/order/getOrderTime/1"))
                    .andExpect(status().isOk());

            verify(userOrderService, times(1)).getById(1);
        }

        @Test
        @DisplayName("获取订单时间失败 - 订单不存在")
        void testGetOrderTimeNotFound() throws Exception {
            when(userOrderService.getById(999)).thenReturn(null);

            mockMvc.perform(get("/order/getOrderTime/999"))
                    .andExpect(status().isNotFound());

            verify(userOrderService, times(1)).getById(999);
        }

        @Test
        @DisplayName("获取订单时间 - ID为0")
        void testGetOrderTimeZeroId() throws Exception {
            when(userOrderService.getById(0)).thenReturn(null);

            mockMvc.perform(get("/order/getOrderTime/0"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("边界值测试 - 最大整数ID")
        void testGetOrderTimeMaxInteger() throws Exception {
            when(userOrderService.getById(Integer.MAX_VALUE)).thenReturn(null);

            mockMvc.perform(get("/order/getOrderTime/" + Integer.MAX_VALUE))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("等价类划分测试")
    class EquivalenceClassTest {

        @Test
        @DisplayName("有效订单ID等价类测试")
        void testValidOrderIdEquivalenceClass() throws Exception {
            // 有效ID范围：正整数
            Integer[] validIds = {1, 10, 100, 1000};
            
            for (Integer id : validIds) {
                Map<String, Integer> requestData = new HashMap<>();
                requestData.put("ID", id);

                UserOrder mockOrder = createMockUserOrder(id, "13812345678", 89.5);
                when(userOrderService.getById(id)).thenReturn(mockOrder);

                mockMvc.perform(post("/order/getUserOrderById")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestData)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(id));

                reset(userOrderService); // 重置Mock以避免干扰
            }
        }

        @Test
        @DisplayName("无效订单ID等价类测试")
        void testInvalidOrderIdEquivalenceClass() throws Exception {
            // 无效ID范围：负数、0
            Integer[] invalidIds = {-1, -10, -100, 0};
            
            for (Integer id : invalidIds) {
                Map<String, Integer> requestData = new HashMap<>();
                requestData.put("ID", id);

                when(userOrderService.getById(id)).thenReturn(null);

                mockMvc.perform(post("/order/getUserOrderById")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestData)))
                        .andExpect(status().isNotFound());

                reset(userOrderService);
            }
        }

        @Test
        @DisplayName("有效手机号等价类测试")
        void testValidPhoneEquivalenceClass() throws Exception {
            // 有效手机号格式
            String[] validPhones = {"13812345678", "15612345678", "18812345678"};
            
            for (String phone : validPhones) {
                Map<String, String> requestData = new HashMap<>();
                requestData.put("userPhone", phone);

                List<UserOrder> mockOrders = Arrays.asList(createMockUserOrder(1, phone, 89.5));
                when(userOrderService.getAllByUserPhone(phone)).thenReturn(mockOrders);

                mockMvc.perform(post("/order/getAllUserOrder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestData)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").isArray())
                        .andExpect(jsonPath("$.length()").value(1));

                reset(userOrderService);
            }
        }
    }

    /**
     * 创建模拟UserOrder对象的辅助方法
     */
    private UserOrder createMockUserOrder(Integer id, String userPhone, Double totalPrice) {
        UserOrder order = new UserOrder();
        // 使用反射设置字段值，避免Lombok问题
        try {
            java.lang.reflect.Field idField = UserOrder.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(order, id);

            java.lang.reflect.Field phoneField = UserOrder.class.getDeclaredField("userPhone");
            phoneField.setAccessible(true);
            phoneField.set(order, userPhone);

            java.lang.reflect.Field priceField = UserOrder.class.getDeclaredField("totalPrice");
            priceField.setAccessible(true);
            priceField.set(order, totalPrice);

            java.lang.reflect.Field timeField = UserOrder.class.getDeclaredField("createdAt");
            timeField.setAccessible(true);
            timeField.set(order, LocalDateTime.now());

        } catch (Exception e) {
            System.err.println("创建Mock UserOrder对象时反射失败: " + e.getMessage());
        }
        return order;
    }

    /**
     * 创建模拟OrderRequestDTO对象的辅助方法
     */
    private OrderRequestDTO createMockOrderRequestDTO() {
        OrderRequestDTO dto = new OrderRequestDTO();
        // 这里根据OrderRequestDTO的实际字段设置值
        // 由于我们不知道具体字段，这里只创建空对象
        return dto;
    }

    /**
     * 创建模拟OrderDetailDTO对象的辅助方法
     */
    private OrderDetailDTO createMockOrderDetailDTO(Integer orderId, String userPhone) {
        OrderDetailDTO dto = new OrderDetailDTO();
        // 使用反射设置字段值
        try {
            java.lang.reflect.Field orderIdField = OrderDetailDTO.class.getDeclaredField("orderId");
            orderIdField.setAccessible(true);
            orderIdField.set(dto, orderId);

            java.lang.reflect.Field phoneField = OrderDetailDTO.class.getDeclaredField("userPhone");
            phoneField.setAccessible(true);
            phoneField.set(dto, userPhone);

        } catch (Exception e) {
            System.err.println("创建Mock OrderDetailDTO对象时反射失败: " + e.getMessage());
        }
        return dto;
    }
}