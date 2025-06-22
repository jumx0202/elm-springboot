#!/bin/bash

# 系统诊断脚本 - 快速检查测试后的系统状态
echo "🔍 饿了么外卖平台系统诊断"
echo "=================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "\n${BLUE}1. 检查Spring Boot应用状态${NC}"
echo "--------------------------------"

# 检查进程
SPRING_PID=$(ps aux | grep elemeSpringBoot | grep -v grep | awk '{print $2}')
if [[ -n "$SPRING_PID" ]]; then
    echo -e "${GREEN}✅ Spring Boot应用正在运行 (PID: $SPRING_PID)${NC}"
    
    # 显示应用资源使用
    APP_STATS=$(ps -p $SPRING_PID -o %cpu,%mem,vsz,rss | tail -1)
    echo "   资源使用: $APP_STATS"
else
    echo -e "${RED}❌ Spring Boot应用未运行${NC}"
fi

# 检查端口
echo -e "\n${BLUE}2. 检查端口占用情况${NC}"
echo "--------------------------------"
PORT_8080=$(lsof -i :8080 | grep LISTEN)
if [[ -n "$PORT_8080" ]]; then
    echo -e "${GREEN}✅ 端口8080被占用:${NC}"
    echo "$PORT_8080"
else
    echo -e "${RED}❌ 端口8080未被占用${NC}"
fi

# 测试API连接
echo -e "\n${BLUE}3. 测试API连接性${NC}"
echo "--------------------------------"

echo "测试商家列表API..."
API_RESPONSE=$(curl -s --connect-timeout 5 -w "%{http_code}" http://localhost:8080/api/business/getAll -o /tmp/api_test.json 2>/dev/null)
if [[ "$API_RESPONSE" == "200" ]]; then
    echo -e "${GREEN}✅ 商家列表API正常 (状态码: 200)${NC}"
    BUSINESS_COUNT=$(jq '. | length' /tmp/api_test.json 2>/dev/null || echo "解析失败")
    echo "   返回商家数量: $BUSINESS_COUNT"
else
    echo -e "${RED}❌ 商家列表API异常 (状态码: $API_RESPONSE)${NC}"
fi

# 测试其他API
echo "测试菜品API..."
FOOD_RESPONSE=$(curl -s --connect-timeout 5 -w "%{http_code}" -X POST -H "Content-Type: application/json" -d '{"ID":1}' http://localhost:8080/api/food/getFoodById -o /dev/null 2>/dev/null)
if [[ "$FOOD_RESPONSE" == "200" ]]; then
    echo -e "${GREEN}✅ 菜品API正常 (状态码: 200)${NC}"
else
    echo -e "${RED}❌ 菜品API异常 (状态码: $FOOD_RESPONSE)${NC}"
fi

# 检查系统资源
echo -e "\n${BLUE}4. 系统资源使用情况${NC}"
echo "--------------------------------"

# 内存使用
if command -v vm_stat &> /dev/null; then
    MEMORY_INFO=$(vm_stat)
    PAGE_SIZE=$(vm_stat | grep "page size" | awk '{print $8}')
    PAGES_FREE=$(echo "$MEMORY_INFO" | grep "Pages free" | awk '{print $3}' | sed 's/\.//')
    PAGES_ACTIVE=$(echo "$MEMORY_INFO" | grep "Pages active" | awk '{print $3}' | sed 's/\.//')
    PAGES_INACTIVE=$(echo "$MEMORY_INFO" | grep "Pages inactive" | awk '{print $3}' | sed 's/\.//')
    PAGES_WIRED=$(echo "$MEMORY_INFO" | grep "Pages wired down" | awk '{print $4}' | sed 's/\.//')
    
    if [[ -n "$PAGE_SIZE" && -n "$PAGES_FREE" ]]; then
        TOTAL_PAGES=$((PAGES_FREE + PAGES_ACTIVE + PAGES_INACTIVE + PAGES_WIRED))
        USED_PAGES=$((PAGES_ACTIVE + PAGES_INACTIVE + PAGES_WIRED))
        MEMORY_USAGE=$(echo "scale=1; $USED_PAGES * 100 / $TOTAL_PAGES" | bc 2>/dev/null || echo "计算失败")
        
        echo "内存使用率: $MEMORY_USAGE%"
        if (( $(echo "$MEMORY_USAGE > 85" | bc -l 2>/dev/null) )); then
            echo -e "${RED}⚠️  内存使用率过高${NC}"
        else
            echo -e "${GREEN}✅ 内存使用正常${NC}"
        fi
    fi
fi

# CPU负载
if command -v uptime &> /dev/null; then
    LOAD_AVG=$(uptime | awk -F'load averages:' '{print $2}' | sed 's/^ *//')
    echo "系统负载: $LOAD_AVG"
    
    LOAD_1MIN=$(echo $LOAD_AVG | awk '{print $1}')
    if (( $(echo "$LOAD_1MIN > 4.0" | bc -l 2>/dev/null) )); then
        echo -e "${YELLOW}⚠️  系统负载较高${NC}"
    else
        echo -e "${GREEN}✅ 系统负载正常${NC}"
    fi
fi

# 磁盘使用
DISK_USAGE=$(df -h / | tail -1 | awk '{print $5}' | sed 's/%//')
echo "磁盘使用率: $DISK_USAGE%"
if [[ $DISK_USAGE -gt 90 ]]; then
    echo -e "${RED}⚠️  磁盘空间不足${NC}"
else
    echo -e "${GREEN}✅ 磁盘空间充足${NC}"
fi

# 检查数据库连接（如果有MySQL）
echo -e "\n${BLUE}5. 数据库状态检查${NC}"
echo "--------------------------------"

# 检查MySQL进程
MYSQL_PID=$(ps aux | grep mysql | grep -v grep | head -1 | awk '{print $2}')
if [[ -n "$MYSQL_PID" ]]; then
    echo -e "${GREEN}✅ MySQL进程运行中 (PID: $MYSQL_PID)${NC}"
else
    echo -e "${YELLOW}⚠️  未检测到MySQL进程${NC}"
fi

# 检查MySQL端口
MYSQL_PORT=$(lsof -i :3306 | grep LISTEN)
if [[ -n "$MYSQL_PORT" ]]; then
    echo -e "${GREEN}✅ MySQL端口3306正常监听${NC}"
else
    echo -e "${YELLOW}⚠️  MySQL端口3306未监听${NC}"
fi

# 检查测试报告
echo -e "\n${BLUE}6. 测试报告检查${NC}"
echo "--------------------------------"

if [[ -f "reports/comprehensive-load-report/index.html" ]]; then
    echo -e "${GREEN}✅ HTML性能报告已生成${NC}"
    echo "   文件路径: reports/comprehensive-load-report/index.html"
else
    echo -e "${RED}❌ HTML性能报告未找到${NC}"
fi

if [[ -d "reports/system-monitoring" ]]; then
    MONITORING_FILES=$(ls reports/system-monitoring/*.log 2>/dev/null | wc -l)
    echo -e "${GREEN}✅ 系统监控报告已生成 ($MONITORING_FILES 个文件)${NC}"
    echo "   目录路径: reports/system-monitoring/"
else
    echo -e "${RED}❌ 系统监控报告未找到${NC}"
fi

# 总结和建议
echo -e "\n${BLUE}7. 诊断总结与建议${NC}"
echo "=================================="

# 基于检查结果给出建议
if [[ -z "$SPRING_PID" ]]; then
    echo -e "${RED}🔧 立即行动: 启动Spring Boot应用${NC}"
    echo "   建议: cd ../.. && mvn spring-boot:run"
fi

if [[ "$API_RESPONSE" != "200" ]]; then
    echo -e "${RED}🔧 检查应用配置和数据库连接${NC}"
fi

if [[ -n "$MEMORY_USAGE" ]] && (( $(echo "$MEMORY_USAGE > 85" | bc -l 2>/dev/null) )); then
    echo -e "${YELLOW}🔧 考虑释放内存或增加系统内存${NC}"
fi

echo -e "\n${GREEN}📊 查看详细测试报告:${NC}"
echo "   浏览器打开: file://$(pwd)/reports/comprehensive-load-report/index.html"
echo -e "\n${GREEN}📋 查看完整分析:${NC}"
echo "   cat REPORT-ANALYSIS-GUIDE.md"

# 清理临时文件
rm -f /tmp/api_test.json 