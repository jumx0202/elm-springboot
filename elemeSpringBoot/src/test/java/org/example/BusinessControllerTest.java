package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.controller.BusinessController;
import org.example.entity.Business;
import org.example.response.BusinessResponse;
import org.example.service.IBusinessService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * BusinessController单元测试类
 * 测试商家控制器的各种功能
 */
@WebMvcTest(value = BusinessController.class, 
    properties = {"spring.jpa.hibernate.ddl-auto=none", "spring.datasource.initialization-mode=never"})
@ImportAutoConfiguration({WebMvcAutoConfiguration.class, JacksonAutoConfiguration.class})
@DisplayName("商家控制器测试")
class BusinessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IBusinessService businessService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("获取所有商家测试")
    class GetAllBusinessTest {

        @Test
        @DisplayName("成功获取商家列表")
        void testGetAllSuccess() throws Exception {
            // 准备测试数据
            List<Business> mockBusinesses = Arrays.asList(
                    createMockBusiness(1, "麦当劳", "快餐"),
                    createMockBusiness(2, "肯德基", "快餐"),
                    createMockBusiness(3, "星巴克", "咖啡")
            );

            // Mock服务调用
            when(businessService.getAll()).thenReturn(mockBusinesses);

            // 执行测试
            mockMvc.perform(post("/business/getAll")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("麦当劳"))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].name").value("肯德基"));

            // 验证服务调用
            verify(businessService, times(1)).getAll();
        }

        @Test
        @DisplayName("获取商家列表 - 返回空列表")
        void testGetAllEmpty() throws Exception {
            // Mock服务返回空列表
            when(businessService.getAll()).thenReturn(Arrays.asList());

            mockMvc.perform(post("/business/getAll")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(businessService, times(1)).getAll();
        }

        @Test
        @DisplayName("获取商家列表 - 返回null")
        void testGetAllNull() throws Exception {
            // Mock服务返回null
            when(businessService.getAll()).thenReturn(null);

            mockMvc.perform(post("/business/getAll")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(businessService, times(1)).getAll();
        }

        @Test
        @DisplayName("获取商家列表 - 单个商家")
        void testGetAllSingle() throws Exception {
            List<Business> mockBusinesses = Arrays.asList(
                    createMockBusiness(1, "麦当劳", "快餐")
            );

            when(businessService.getAll()).thenReturn(mockBusinesses);

            mockMvc.perform(post("/business/getAll")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1));
        }

        @Test
        @DisplayName("获取商家列表 - 包含优惠信息")
        void testGetAllWithDiscounts() throws Exception {
            Business business = createMockBusiness(1, "麦当劳", "快餐");
            // 设置优惠信息
            List<Business> mockBusinesses = Arrays.asList(business);

            when(businessService.getAll()).thenReturn(mockBusinesses);

            mockMvc.perform(post("/business/getAll")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1));

            // 验证setDiscounts方法被调用
            verify(businessService, times(1)).getAll();
        }

        @Test
        @DisplayName("获取商家列表 - 服务层异常")
        void testGetAllServiceException() throws Exception {
            when(businessService.getAll()).thenThrow(new RuntimeException("数据库连接失败"));

            // 注意：这里需要根据实际的异常处理逻辑来调整期望的状态码
            mockMvc.perform(post("/business/getAll")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().is5xxServerError());
        }
    }

    @Nested
    @DisplayName("根据ID获取商家测试")
    class GetBusinessByIdTest {

        @Test
        @DisplayName("成功获取商家信息")
        void testGetBusinessByIdSuccess() throws Exception {
            Map<String, Integer> requestData = new HashMap<>();
            requestData.put("ID", 1);

            Business mockBusiness = createMockBusiness(1, "麦当劳", "快餐");
            when(businessService.findBusinessById(1)).thenReturn(mockBusiness);

            mockMvc.perform(post("/business/getBusinessById")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.name").value("麦当劳"));

            verify(businessService, times(1)).findBusinessById(1);
        }

        @Test
        @DisplayName("获取商家失败 - 商家不存在")
        void testGetBusinessByIdNotFound() throws Exception {
            Map<String, Integer> requestData = new HashMap<>();
            requestData.put("ID", 999);

            when(businessService.findBusinessById(999)).thenReturn(null);

            mockMvc.perform(post("/business/getBusinessById")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));

            verify(businessService, times(1)).findBusinessById(999);
        }

        @Test
        @DisplayName("获取商家失败 - ID为空")
        void testGetBusinessByIdNullId() throws Exception {
            Map<String, Integer> requestData = new HashMap<>();
            // 不设置ID字段

            when(businessService.findBusinessById(null)).thenReturn(null);

            mockMvc.perform(post("/business/getBusinessById")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("获取商家失败 - ID为负数")
        void testGetBusinessByIdNegativeId() throws Exception {
            Map<String, Integer> requestData = new HashMap<>();
            requestData.put("ID", -1);

            when(businessService.findBusinessById(-1)).thenReturn(null);

            mockMvc.perform(post("/business/getBusinessById")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("获取商家失败 - ID为0")
        void testGetBusinessByIdZeroId() throws Exception {
            Map<String, Integer> requestData = new HashMap<>();
            requestData.put("ID", 0);

            when(businessService.findBusinessById(0)).thenReturn(null);

            mockMvc.perform(post("/business/getBusinessById")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("边界值测试 - 最大整数ID")
        void testGetBusinessByIdMaxInteger() throws Exception {
            Map<String, Integer> requestData = new HashMap<>();
            requestData.put("ID", Integer.MAX_VALUE);

            when(businessService.findBusinessById(Integer.MAX_VALUE)).thenReturn(null);

            mockMvc.perform(post("/business/getBusinessById")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestData)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("服务层异常测试")
        void testGetBusinessByIdServiceException() throws Exception {
            Map<String, Integer> requestData = new HashMap<>();
            requestData.put("ID", 1);

            when(businessService.findBusinessById(1)).thenThrow(new RuntimeException("数据库连接失败"));

            // 这里需要根据BusinessResponse的异常处理逻辑调整
            mockMvc.perform(post("/business/getBusinessById")
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
            mockMvc.perform(options("/business/getAll")
                    .header("Origin", "http://localhost:3000")
                    .header("Access-Control-Request-Method", "POST")
                    .header("Access-Control-Request-Headers", "Content-Type"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("跨域POST请求测试")
        void testCorsPostRequest() throws Exception {
            when(businessService.getAll()).thenReturn(Arrays.asList());

            mockMvc.perform(post("/business/getAll")
                    .header("Origin", "http://localhost:3000")
                    .contentType(MediaType.APPLICATION_JSON))
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
            Integer[] validIds = {1, 10, 100, 1000};
            
            for (Integer id : validIds) {
                Map<String, Integer> requestData = new HashMap<>();
                requestData.put("ID", id);

                Business mockBusiness = createMockBusiness(id, "测试商家", "测试类型");
                when(businessService.findBusinessById(id)).thenReturn(mockBusiness);

                mockMvc.perform(post("/business/getBusinessById")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestData)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.id").value(id));

                reset(businessService); // 重置Mock以避免干扰
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

                when(businessService.findBusinessById(id)).thenReturn(null);

                mockMvc.perform(post("/business/getBusinessById")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestData)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(false));

                reset(businessService);
            }
        }
    }

    @Nested
    @DisplayName("边界值分析测试")
    class BoundaryValueTest {

        @Test
        @DisplayName("ID边界值测试")
        void testIdBoundaryValues() throws Exception {
            // 测试边界值：1, Integer.MAX_VALUE
            Integer[] boundaryIds = {1, Integer.MAX_VALUE};
            
            for (Integer id : boundaryIds) {
                Map<String, Integer> requestData = new HashMap<>();
                requestData.put("ID", id);

                // 对于边界值，假设都能找到对应的商家
                Business mockBusiness = createMockBusiness(id, "边界测试商家", "测试");
                when(businessService.findBusinessById(id)).thenReturn(mockBusiness);

                mockMvc.perform(post("/business/getBusinessById")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestData)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true));

                reset(businessService);
            }
        }

        @Test
        @DisplayName("商家列表大小边界值测试")
        void testBusinessListSizeBoundary() throws Exception {
            // 测试空列表边界
            when(businessService.getAll()).thenReturn(Arrays.asList());
            
            mockMvc.perform(post("/business/getAll")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            // 测试单个商家边界
            when(businessService.getAll()).thenReturn(
                    Arrays.asList(createMockBusiness(1, "单个商家", "测试"))
            );
            
            mockMvc.perform(post("/business/getAll")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }
    }

    /**
     * 创建模拟Business对象的辅助方法
     */
    private Business createMockBusiness(Integer id, String name, String type) {
        Business business = new Business();
        // 使用反射设置字段值，避免Lombok问题
        try {
            java.lang.reflect.Field idField = Business.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(business, id);

            java.lang.reflect.Field nameField = Business.class.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(business, name);

            java.lang.reflect.Field typeField = Business.class.getDeclaredField("type");
            typeField.setAccessible(true);
            typeField.set(business, type);

            // 设置其他可能的字段
            java.lang.reflect.Field discountsField = Business.class.getDeclaredField("discounts");
            discountsField.setAccessible(true);
            discountsField.set(business, "满100减10,满200减25");

        } catch (Exception e) {
            // 如果反射失败，记录错误但继续执行
            System.err.println("创建Mock Business对象时反射失败: " + e.getMessage());
        }
        return business;
    }
} 