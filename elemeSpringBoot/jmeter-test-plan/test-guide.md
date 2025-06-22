# 🍔 饿了么外卖平台JMeter测试指南

## 🚨 常见问题解决

### 问题1：Connection refused (连接被拒绝)
**错误信息：** `Connect to localhost:8080 failed: Connection refused`

**原因：** Spring Boot应用没有启动

**解决方案：**
```bash
# 1. 启动Spring Boot应用
cd elemeSpringBoot
mvn spring-boot:run

# 2. 等待应用启动完成（看到类似以下日志）
# Started ElemeSpringBootApplication in 3.456 seconds
```

### 问题2：404 Not Found 
**错误信息：** `responseCode,404`

**原因：** 请求路径缺少 `/api` 前缀

**解决方案：** 确保所有请求路径都包含 `/api` 前缀
- ✅ 正确：`http://localhost:8080/api/business/getAll`
- ❌ 错误：`http://localhost:8080/business/getAll`

### 问题3：Could not find the TestPlan class
**原因：** JMX文件缺少TestPlan元素

**解决方案：** 已在最新版本中修复，请使用更新后的JMX文件

## 📋 完整测试流程

### 步骤1：启动被测系统
```bash
# 进入项目目录
cd elemeSpringBoot

# 启动Spring Boot应用
mvn spring-boot:run

# 验证启动成功
curl -X POST http://localhost:8080/api/business/getAll \
  -H "Content-Type: application/json" \
  -d "{}"
```

### 步骤2：验证系统可用性
```bash
# 方法1：使用curl测试
curl -v -X POST http://localhost:8080/api/business/getAll \
  -H "Content-Type: application/json" \
  -d "{}"

# 方法2：使用浏览器访问 (如果有GET接口)
# http://localhost:8080/api/business/getAll

# 预期结果：返回200状态码和商家数据
```

### 步骤3：执行JMeter测试
```bash
# 进入测试目录
cd jmeter-test-plan

# 执行简单连接测试
jmeter -n -t scenarios/simple-test.jmx -l simple-results.jtl

# 执行用户认证测试
jmeter -n -t scenarios/user-auth-test.jmx -l auth-results.jtl

# 执行商家菜品测试
jmeter -n -t scenarios/business-food-test.jmx -l business-results.jtl

# 使用自动化脚本（推荐）
./run-test.sh simple
```

### 步骤4：查看测试结果
```bash
# 查看JTL结果文件
cat simple-results.jtl

# 生成HTML报告
jmeter -g simple-results.jtl -o html-report/

# 在浏览器中打开报告
open html-report/index.html
```

## 🔧 故障排除

### 检查网络连接
```bash
# 检查端口是否被占用
lsof -i :8080

# 检查防火墙设置
# macOS
sudo pfctl -s all

# 测试本地连接
telnet localhost 8080
```

### 检查Java和Maven
```bash
# 检查Java版本
java -version

# 检查Maven版本
mvn -version

# 清理并重新编译
mvn clean compile
```

### 检查数据库连接
```bash
# 检查MySQL是否运行
brew services list | grep mysql

# 启动MySQL（如果未启动）
brew services start mysql

# 检查数据库连接
mysql -u root -p -e "SHOW DATABASES;"
```

## 📊 性能指标说明

### JTL文件字段含义
- `timeStamp`: 请求时间戳
- `elapsed`: 响应时间(毫秒)
- `responseCode`: HTTP状态码
- `success`: 是否成功(true/false)
- `bytes`: 响应字节数
- `sentBytes`: 发送字节数

### 正常响应示例
```
1750521564453,32,获取商家列表,200,OK,基础测试组 1-1,text,true,,1024,185,1,1,http://localhost:8080/api/business/getAll,30,0,8
```

### 错误响应示例
```
1750521564453,32,获取商家列表,404,Not Found,基础测试组 1-1,text,false,HTTP状态码应为200,615,185,1,1,http://localhost:8080/business/getAll,30,0,8
```

## 🎯 测试目标和预期结果

### 功能测试预期结果
| 接口 | 状态码 | 响应时间 | 错误率 |
|------|--------|----------|--------|
| 商家列表 | 200 | < 1000ms | 0% |
| 商家详情 | 200 | < 800ms | 0% |
| 用户登录 | 200 | < 2000ms | 0% |
| 菜品查询 | 200 | < 1500ms | 0% |

### 性能测试预期结果
| 指标 | 目标值 |
|------|--------|
| 并发用户数 | 50-200 |
| 平均响应时间 | < 2秒 |
| 95%响应时间 | < 5秒 |
| 错误率 | < 1% |
| 吞吐量 | > 100 req/sec |

## 📝 测试报告模板

### 测试环境
- 操作系统：macOS 24.2.0
- JMeter版本：5.6.3
- Java版本：JDK 21
- 被测系统：Spring Boot 3.3.0
- 数据库：MySQL 8.0

### 测试结果摘要
- 测试时间：[填写测试时间]
- 总请求数：[填写总数]
- 成功率：[填写成功率]
- 平均响应时间：[填写时间]
- 最大响应时间：[填写时间]

### 发现的问题
1. [问题描述]
   - 严重级别：[高/中/低]
   - 复现步骤：[详细步骤]
   - 建议解决方案：[解决建议]

## 🔄 持续集成脚本

### GitHub Actions配置
```yaml
name: Performance Tests
on: [push, pull_request]
jobs:
  performance-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 21
        uses: actions/setup-java@v2
        with:
          java-version: '21'
      - name: Start Application
        run: |
          cd elemeSpringBoot
          mvn spring-boot:run &
          sleep 30
      - name: Run JMeter Tests
        run: |
          cd elemeSpringBoot/jmeter-test-plan
          ./run-test.sh all
      - name: Upload Results
        uses: actions/upload-artifact@v2
        with:
          name: jmeter-results
          path: elemeSpringBoot/jmeter-test-plan/reports/
```

## 📞 技术支持

如果遇到其他问题，请检查：
1. ✅ Spring Boot应用是否正常启动
2. ✅ 数据库连接是否正常
3. ✅ 端口8080是否被占用
4. ✅ JMeter版本是否兼容
5. ✅ 测试数据文件是否存在

更多问题请查看项目README或联系开发团队。 