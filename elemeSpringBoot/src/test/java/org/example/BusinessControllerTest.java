package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.Business;
import org.example.service.IBusinessService;
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

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * BusinessController单元测试类
 * 测试商家控制器的各种功能
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.sql.init.mode=never"
})
@DisplayName("商家控制器测试")
class BusinessControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private IBusinessService businessService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("成功获取商家列表")
    void testGetAllSuccess() throws Exception {
        Business business1 = createMockBusiness(1, "麦当劳", "快餐");
        Business business2 = createMockBusiness(2, "肯德基", "快餐");
        List<Business> businessList = Arrays.asList(business1, business2);

        when(businessService.getAll()).thenReturn(businessList);

        mockMvc.perform(post("/business/getAll")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].businessName").value("麦当劳"))
                .andExpect(jsonPath("$[1].businessName").value("肯德基"));

        verify(businessService, times(1)).getAll();
    }

    @Test
    @DisplayName("获取商家列表 - 返回空列表")
    void testGetAllEmpty() throws Exception {
        when(businessService.getAll()).thenReturn(new ArrayList<>());

        mockMvc.perform(post("/business/getAll")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(businessService, times(1)).getAll();
    }

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
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.businessName").value("麦当劳"));

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
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("商家不存在"));

        verify(businessService, times(1)).findBusinessById(999);
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

    private Business createMockBusiness(Integer id, String name, String type) {
        Business business = new Business();
        business.setId(id);
        business.setBusinessName(name);
        business.setType(type);
        business.setRating("4.5");
        business.setSales("100");
        business.setDistance("1.2km");
        business.setMinOrder("20");
        business.setComment("很好吃");
        business.setDiscounts("满50减10-满100减20");
        business.setDiscount("9折");
        business.setNotice("营业中");
        business.setSidebarItems("热销/新品/饮品");
        business.setImgLogo("/images/logo.jpg");
        business.setDelivery("5");
        return business;
    }
} 