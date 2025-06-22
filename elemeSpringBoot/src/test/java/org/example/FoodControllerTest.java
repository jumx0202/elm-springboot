package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.controller.FoodController;
import org.example.dto.IdsWrapper;
import org.example.entity.Food;
import org.example.service.IFoodService;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * FoodController单元测试类
 * 测试食物控制器的各种功能
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.sql.init.mode=never"
})
@DisplayName("食物控制器测试")
class FoodControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private IFoodService foodService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Nested
    @DisplayName("根据ID列表获取商品测试")
    class GetAllByIdsTest {

        @Test
        @DisplayName("成功获取商品列表")
        void testGetAllByIdsSuccess() throws Exception {
            // 准备测试数据
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("ids", Arrays.asList(1, 2, 3));

            List<Food> mockFoods = Arrays.asList(
                    createMockFood(1, "汉堡", 25.0),
                    createMockFood(2, "可乐", 5.0),
                    createMockFood(3, "薯条", 15.0)
            );

            // Mock服务调用
            when(foodService.getFoodsByIds(new Integer[]{1, 2, 3})).thenReturn(mockFoods);

            // 执行测试
            mockMvc.perform(post("/food/getAllByIds")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("汉堡"))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].name").value("可乐"));

            // 验证服务调用
            verify(foodService, times(1)).getFoodsByIds(any(Integer[].class));
        }

        @Test
        @DisplayName("获取商品列表 - 返回空列表")
        void testGetAllByIdsEmpty() throws Exception {
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("ids", Arrays.asList(999, 1000));

            // Mock服务返回空列表
            when(foodService.getFoodsByIds(new Integer[]{999, 1000})).thenReturn(Arrays.asList());

            mockMvc.perform(post("/food/getAllByIds")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isNoContent());

            verify(foodService, times(1)).getFoodsByIds(any(Integer[].class));
        }

        @Test
        @DisplayName("获取商品列表 - 单个ID")
        void testGetAllByIdsSingle() throws Exception {
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("ids", Arrays.asList(1));

            List<Food> mockFoods = Arrays.asList(createMockFood(1, "汉堡", 25.0));
            when(foodService.getFoodsByIds(new Integer[]{1})).thenReturn(mockFoods);

            mockMvc.perform(post("/food/getAllByIds")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1));
        }

        @Test
        @DisplayName("获取商品列表 - 边界值测试")
        void testGetAllByIdsBoundary() throws Exception {
            // 测试大量ID的情况
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("ids", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

            when(foodService.getFoodsByIds(any(Integer[].class))).thenReturn(Arrays.asList());

            mockMvc.perform(post("/food/getAllByIds")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("获取商品列表 - 包含负数ID")
        void testGetAllByIdsWithNegativeIds() throws Exception {
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("ids", Arrays.asList(-1, 0, 1));

            when(foodService.getFoodsByIds(any(Integer[].class))).thenReturn(Arrays.asList());

            mockMvc.perform(post("/food/getAllByIds")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("获取商品列表 - 包含重复ID")
        void testGetAllByIdsWithDuplicates() throws Exception {
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("ids", Arrays.asList(1, 1, 2, 2, 3));

            List<Food> mockFoods = Arrays.asList(
                    createMockFood(1, "汉堡", 25.0),
                    createMockFood(2, "可乐", 5.0),
                    createMockFood(3, "薯条", 15.0)
            );

            when(foodService.getFoodsByIds(any(Integer[].class))).thenReturn(mockFoods);

            mockMvc.perform(post("/food/getAllByIds")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(3));
        }
    }

    @Nested
    @DisplayName("根据单个ID获取商品测试")
    class GetFoodByIdTest {

        @Test
        @DisplayName("成功获取商品信息")
        void testGetFoodByIdSuccess() throws Exception {
            Map<String, Integer> requestData = new HashMap<>();
            requestData.put("ID", 1);

            Food mockFood = createMockFood(1, "汉堡", 25.0);
            when(foodService.getById(1)).thenReturn(mockFood);

            mockMvc.perform(post("/food/getFoodById")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("汉堡"))
                    .andExpect(jsonPath("$.price").value(25.0));

            verify(foodService, times(1)).getById(1);
        }

        @Test
        @DisplayName("获取商品失败 - 商品不存在")
        void testGetFoodByIdNotFound() throws Exception {
            Map<String, Integer> requestData = new HashMap<>();
            requestData.put("ID", 999);

            when(foodService.getById(999)).thenReturn(null);

            mockMvc.perform(post("/food/getFoodById")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isNotFound());

            verify(foodService, times(1)).getById(999);
        }

        @Test
        @DisplayName("获取商品失败 - ID为空")
        void testGetFoodByIdNullId() throws Exception {
            Map<String, Integer> requestData = new HashMap<>();
            // 不设置ID字段

            mockMvc.perform(post("/food/getFoodById")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isBadRequest());

            verify(foodService, never()).getById(any());
        }

        @Test
        @DisplayName("获取商品失败 - ID为负数")
        void testGetFoodByIdNegativeId() throws Exception {
            Map<String, Integer> requestData = new HashMap<>();
            requestData.put("ID", -1);

            when(foodService.getById(-1)).thenReturn(null);

            mockMvc.perform(post("/food/getFoodById")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("获取商品失败 - ID为0")
        void testGetFoodByIdZeroId() throws Exception {
            Map<String, Integer> requestData = new HashMap<>();
            requestData.put("ID", 0);

            when(foodService.getById(0)).thenReturn(null);

            mockMvc.perform(post("/food/getFoodById")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("边界值测试 - 最大整数ID")
        void testGetFoodByIdMaxInteger() throws Exception {
            Map<String, Integer> requestData = new HashMap<>();
            requestData.put("ID", Integer.MAX_VALUE);

            when(foodService.getById(Integer.MAX_VALUE)).thenReturn(null);

            mockMvc.perform(post("/food/getFoodById")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("服务层异常测试")
        void testGetFoodByIdServiceException() throws Exception {
            Map<String, Integer> requestData = new HashMap<>();
            requestData.put("ID", 1);

            when(foodService.getById(1)).thenThrow(new RuntimeException("数据库连接失败"));

            // 注意：这里需要根据实际的异常处理逻辑来调整期望的状态码
            mockMvc.perform(post("/food/getFoodById")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().is5xxServerError());
        }
    }

    @Nested
    @DisplayName("跨域配置测试")
    class CorsTest {

        @Test
        @DisplayName("CORS预检请求测试")
        void testCorsPreflightRequest() throws Exception {
            mockMvc.perform(options("/food/getAllByIds")
                    .header("Origin", "http://localhost:3000")
                    .header("Access-Control-Request-Method", "POST")
                    .header("Access-Control-Request-Headers", "Content-Type"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("跨域POST请求测试")
        void testCorsPostRequest() throws Exception {
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("ids", Arrays.asList(1));

            when(foodService.getFoodsByIds(any(Integer[].class))).thenReturn(Arrays.asList());

            mockMvc.perform(post("/food/getAllByIds")
                    .header("Origin", "http://localhost:3000")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isNoContent())
                    .andExpect(header().string("Access-Control-Allow-Origin", "*"));
        }
    }

    @Nested
    @DisplayName("等价类划分测试")
    class EquivalenceClassTest {

        @Test
        @DisplayName("有效ID等价类测试")
        void testValidIdEquivalenceClass() throws Exception {
            // 有效ID范围：正整数
            Integer[] validIds = {1, 10, 100, 1000, Integer.MAX_VALUE};
            
            for (Integer id : validIds) {
                Map<String, Integer> requestData = new HashMap<>();
                requestData.put("ID", id);

                Food mockFood = createMockFood(id, "测试商品", 10.0);
                when(foodService.getById(id)).thenReturn(mockFood);

                mockMvc.perform(post("/food/getFoodById")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestData)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(id));

                reset(foodService); // 重置Mock以避免干扰
            }
        }

        @Test
        @DisplayName("无效ID等价类测试")
        void testInvalidIdEquivalenceClass() throws Exception {
            // 无效ID范围：负数、0、null
            Integer[] invalidIds = {-1, -10, -100, 0};
            
            for (Integer id : invalidIds) {
                Map<String, Integer> requestData = new HashMap<>();
                requestData.put("ID", id);

                when(foodService.getById(id)).thenReturn(null);

                mockMvc.perform(post("/food/getFoodById")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestData)))
                        .andExpect(status().isNotFound());

                reset(foodService);
            }
        }
    }

    /**
     * 创建模拟Food对象的辅助方法
     */
    private Food createMockFood(Integer id, String name, Double price) {
        Food food = new Food();
        // 注意：这里直接赋值字段，避免使用setter方法
        try {
            // 使用反射设置字段值，避免Lombok问题
            java.lang.reflect.Field idField = Food.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(food, id);

            java.lang.reflect.Field nameField = Food.class.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(food, name);

            java.lang.reflect.Field priceField = Food.class.getDeclaredField("price");
            priceField.setAccessible(true);
            priceField.set(food, price);
        } catch (Exception e) {
            // 如果反射失败，创建一个基本的Food对象
            // 在实际测试中，可能需要根据Food类的具体实现调整
        }
        return food;
    }
} 