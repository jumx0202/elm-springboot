# 饿了么外卖系统 - 单元测试说明文档

## 概述

本项目完成了《软件质量保证与测试》期末大作业中的"单元测试"模块，使用JUnit 5框架实现了全面的单元测试，包括黑盒测试、白盒测试、性能测试和集成测试。

## 测试结构

### 1. 测试类组织

```
src/test/java/org/example/
├── ValidationUtilsTest.java     # 验证工具类测试
├── EmailServiceTest.java        # 邮件服务测试  
├── UserTest.java               # 用户实体测试
├── UserControllerTest.java     # 用户控制器测试
├── IntegrationTest.java        # 集成测试
├── PerformanceTest.java        # 性能测试
└── TestSuite.java              # 测试套件（可选）
```

### 2. 测试资源

```
src/test/resources/
├── validation_test_data.csv    # 验证测试数据（预留）
├── user_test_data.csv         # 用户测试数据（预留）
└── README_测试说明.md         # 本说明文档
```

## 测试方法详解

### 1. 黑盒测试

#### 边界值分析
- **手机号测试**: 10位、11位、12位长度边界
- **密码测试**: 5位、6位、20位、21位长度边界  
- **用户名测试**: 1位、2位、20位、21位长度边界
- **邮箱测试**: 4位、5位、100位、101位长度边界
- **数量测试**: 0、1、999、1000边界值
- **金额测试**: 0.00、0.01、9999.99、10000.00边界值

#### 等价类划分
- **有效等价类**: 符合格式和业务规则的输入
- **无效等价类**: 不符合格式或业务规则的输入
- **性别验证**: "男"、"女"、"未知"为有效类，其他为无效类

### 2. 白盒测试

#### 语句覆盖
- 测试所有验证方法的执行路径
- 覆盖异常处理分支
- 覆盖边界检查逻辑

#### 分支覆盖  
- 测试if-else分支
- 测试循环分支
- 测试异常捕获分支

### 3. 集成测试

#### 组件协作测试
- 验证工具类与实体类的集成
- 服务层与控制器层的集成
- 邮件服务与用户注册流程的集成

#### 端到端测试
- 完整的用户注册流程测试
- 用户登录流程测试
- 数据验证流程测试

### 4. 性能测试

#### 单一验证性能
- 每种验证方法执行10,000次的耗时测试
- 平均响应时间统计
- 内存使用情况监控

#### 并发测试
- 多线程并发验证测试
- 线程安全性验证
- 并发性能对比

#### 批量测试  
- 大批量数据验证性能
- 内存泄漏检测
- 性能回归测试

## 运行测试

### 1. 单独运行测试类

```bash
# 运行验证工具类测试
mvn test -Dtest=ValidationUtilsTest

# 运行邮件服务测试  
mvn test -Dtest=EmailServiceTest

# 运行用户实体测试
mvn test -Dtest=UserTest

# 运行控制器测试
mvn test -Dtest=UserControllerTest

# 运行集成测试
mvn test -Dtest=IntegrationTest

# 运行性能测试
mvn test -Dtest=PerformanceTest
```

### 2. 运行所有测试

```bash
# 运行所有测试
mvn test

# 运行测试并生成报告
mvn test surefire-report:report
```

### 3. 在IDE中运行

- **IntelliJ IDEA**: 右键点击测试类或方法，选择"Run"
- **Eclipse**: 右键点击测试类，选择"Run As" -> "JUnit Test"
- **VS Code**: 点击测试方法上方的"Run Test"按钮

## 测试覆盖率

### 预期覆盖率指标

- **行覆盖率**: > 90%
- **分支覆盖率**: > 85%  
- **方法覆盖率**: > 95%
- **类覆盖率**: 100%

### 生成覆盖率报告

```bash
# 使用JaCoCo生成覆盖率报告
mvn clean test jacoco:report
```

报告位置: `target/site/jacoco/index.html`

## 测试数据说明

### 1. 有效测试数据

```java
// 有效手机号
String[] validPhones = {"13812345678", "14712345678", "15012345678"};

// 有效密码
String[] validPasswords = {"Test123", "Password1", "Abc123@"};

// 有效用户名  
String[] validUsernames = {"张三", "john123", "test_user"};

// 有效邮箱
String[] validEmails = {"test@example.com", "user@domain.org"};

// 有效性别
String[] validGenders = {"男", "女", "未知"};
```

### 2. 无效测试数据

```java
// 无效手机号
String[] invalidPhones = {"12812345678", "1081234567", "138123456a"};

// 无效密码
String[] invalidPasswords = {"123456", "abcdef", "12345"};

// 无效用户名
String[] invalidUsernames = {"a", "user@123", "test-user"};

// 无效邮箱  
String[] invalidEmails = {"invalid-email", "@domain.com", "user@"};

// 无效性别
String[] invalidGenders = {"其他", "男性", "unknown"};
```

## 测试注解说明

### JUnit 5 注解

- `@Test`: 标记测试方法
- `@DisplayName`: 提供测试的中文描述
- `@BeforeEach`: 每个测试方法执行前运行
- `@AfterEach`: 每个测试方法执行后运行  
- `@Nested`: 创建嵌套测试类
- `@RepeatedTest`: 重复执行测试
- `@Timeout`: 设置测试超时时间
- `@Order`: 设置测试执行顺序

### Spring Boot 测试注解

- `@WebMvcTest`: Web层测试
- `@MockBean`: Mock Spring Bean
- `@Autowired`: 注入测试依赖

### Mockito 注解

- `@Mock`: 创建Mock对象
- `@InjectMocks`: 注入Mock依赖
- `@ExtendWith(MockitoExtension.class)`: 启用Mockito

## 断言方法

### 常用断言

```java
// 基本断言
assertTrue(condition, "条件应该为真");
assertFalse(condition, "条件应该为假");
assertEquals(expected, actual, "值应该相等");
assertNotEquals(unexpected, actual, "值应该不相等");
assertNull(object, "对象应该为null");
assertNotNull(object, "对象不应该为null");

// 异常断言
assertThrows(Exception.class, () -> {
    // 可能抛出异常的代码
}, "应该抛出异常");

assertDoesNotThrow(() -> {
    // 不应该抛出异常的代码  
}, "不应该抛出异常");
```

## Mock使用说明

### 基本Mock操作

```java
// 设置Mock行为
when(mockService.method(parameter)).thenReturn(result);
when(mockService.method(parameter)).thenThrow(new Exception());

// 验证Mock调用
verify(mockService, times(1)).method(parameter);
verify(mockService, never()).method(any());
```

## 常见问题解决

### 1. Lombok相关问题

如果遇到setter方法找不到的错误：
- 确保IDE安装了Lombok插件
- 在IDE中启用注解处理
- 重新编译项目

### 2. Spring Boot测试问题

如果Web测试失败：
- 检查`@WebMvcTest`注解是否正确
- 确保Mock了所有依赖的Bean
- 检查请求路径和参数是否正确

### 3. 数据库相关问题

如果需要数据库测试：
- 使用`@DataJpaTest`进行JPA测试
- 使用`@Sql`注解加载测试数据
- 考虑使用H2内存数据库

## 最佳实践

### 1. 测试命名

- 使用描述性的方法名
- 使用`@DisplayName`提供中文说明
- 遵循Given-When-Then模式

### 2. 测试组织

- 使用`@Nested`组织相关测试
- 一个测试方法只测试一个场景
- 保持测试的独立性

### 3. 测试数据

- 使用有意义的测试数据
- 避免魔法数字和字符串
- 考虑使用测试数据构建器模式

### 4. 断言

- 提供有意义的失败消息
- 使用专门的断言方法
- 优先使用业务相关的断言

## 总结

本单元测试模块全面覆盖了系统的核心功能，采用了多种测试方法和技术：

1. **完整的测试覆盖**: 包含工具类、服务类、实体类、控制器的全面测试
2. **多种测试方法**: 黑盒测试、白盒测试、集成测试、性能测试
3. **标准化流程**: 使用JUnit 5标准框架和最佳实践
4. **详细的文档**: 完整的测试说明和使用指南

通过这些测试，可以有效保证系统的质量和稳定性，满足《软件质量保证与测试》课程的要求。