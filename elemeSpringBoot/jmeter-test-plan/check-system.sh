#!/bin/bash

# 系统状态检查脚本
# 用于诊断JMeter测试环境是否正常

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[✓]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[!]${NC} $1"
}

log_error() {
    echo -e "${RED}[✗]${NC} $1"
}

# 检查端口占用
check_port() {
    local port=$1
    log_info "检查端口 $port 状态..."
    
    if lsof -i :$port >/dev/null 2>&1; then
        local process=$(lsof -i :$port | tail -n 1 | awk '{print $1, $2}')
        log_success "端口 $port 被占用: $process"
        return 0
    else
        log_error "端口 $port 未被占用"
        return 1
    fi
}

# 检查Java环境
check_java() {
    log_info "检查Java环境..."
    
    if command -v java >/dev/null 2>&1; then
        local java_version=$(java -version 2>&1 | head -n 1)
        log_success "Java已安装: $java_version"
    else
        log_error "Java未安装或未在PATH中"
        return 1
    fi
}

# 检查Maven环境
check_maven() {
    log_info "检查Maven环境..."
    
    if command -v mvn >/dev/null 2>&1; then
        local maven_version=$(mvn -version | head -n 1)
        log_success "Maven已安装: $maven_version"
    else
        log_error "Maven未安装或未在PATH中"
        return 1
    fi
}

# 检查JMeter环境
check_jmeter() {
    log_info "检查JMeter环境..."
    
    if command -v jmeter >/dev/null 2>&1; then
        local jmeter_version=$(jmeter -version 2>&1 | head -n 1)
        log_success "JMeter已安装: $jmeter_version"
    else
        log_warning "JMeter未在PATH中，将尝试使用相对路径"
        if [ -f "../apache-jmeter-5.6.3/bin/jmeter" ]; then
            log_success "找到JMeter: ../apache-jmeter-5.6.3/bin/jmeter"
        else
            log_error "JMeter未找到"
            return 1
        fi
    fi
}

# 检查数据库连接
check_database() {
    log_info "检查MySQL连接..."
    
    if command -v mysql >/dev/null 2>&1; then
        # 从application.properties读取数据库配置
        local props_file="../src/main/resources/application.properties"
        if [ -f "$props_file" ]; then
            local db_url=$(grep "spring.datasource.url" "$props_file" | cut -d'=' -f2)
            local db_user=$(grep "spring.datasource.username" "$props_file" | cut -d'=' -f2)
            local db_pass=$(grep "spring.datasource.password" "$props_file" | cut -d'=' -f2)
            
            log_info "数据库配置: $db_url"
            log_info "用户名: $db_user"
            
            # 尝试连接数据库（不输入密码，仅测试连接性）
            if mysql -h 127.0.0.1 -u "$db_user" -e "SELECT 1;" >/dev/null 2>&1; then
                log_success "数据库连接正常"
            else
                log_warning "数据库连接可能有问题，请检查MySQL服务和密码"
            fi
        else
            log_warning "未找到application.properties文件"
        fi
    else
        log_warning "MySQL客户端未安装，跳过数据库检查"
    fi
}

# 检查网络连接
check_network() {
    log_info "检查网络连接性..."
    
    # 检查本地环回
    if curl -s --connect-timeout 5 http://127.0.0.1:8080 >/dev/null 2>&1; then
        log_success "127.0.0.1:8080 可访问"
    else
        log_error "127.0.0.1:8080 不可访问"
    fi
    
    # 检查localhost
    if curl -s --connect-timeout 5 http://localhost:8080 >/dev/null 2>&1; then
        log_success "localhost:8080 可访问"
    else
        log_error "localhost:8080 不可访问"
    fi
}

# 检查Spring Boot应用
check_spring_boot() {
    log_info "检查Spring Boot应用状态..."
    
    local api_url="http://localhost:8080/api/business/getAll"
    
    if curl -s --connect-timeout 5 -X POST "$api_url" \
         -H "Content-Type: application/json" \
         -d "{}" >/dev/null 2>&1; then
        log_success "Spring Boot应用运行正常"
        
        # 检查响应内容
        local response=$(curl -s -X POST "$api_url" \
                        -H "Content-Type: application/json" \
                        -d "{}")
        echo "API响应: $response"
    else
        log_error "Spring Boot应用未运行或API不可访问"
        log_info "请尝试启动应用: cd ../.. && mvn spring-boot:run"
        return 1
    fi
}

# 检查测试文件
check_test_files() {
    log_info "检查测试文件..."
    
    local files=(
        "scenarios/simple-test.jmx"
        "scenarios/user-auth-test.jmx" 
        "scenarios/business-food-test.jmx"
        "test-data/users.csv"
    )
    
    for file in "${files[@]}"; do
        if [ -f "$file" ]; then
            log_success "测试文件存在: $file"
        else
            log_error "测试文件缺失: $file"
        fi
    done
}

# 主检查函数
main() {
    echo "=================================================="
    echo "🔍 饿了么外卖平台 - 系统状态检查"
    echo "=================================================="
    
    local all_good=true
    
    # 执行各项检查
    check_java || all_good=false
    echo
    
    check_maven || all_good=false
    echo
    
    check_jmeter || all_good=false
    echo
    
    check_port 8080 || all_good=false
    echo
    
    check_database
    echo
    
    check_network
    echo
    
    check_spring_boot || all_good=false
    echo
    
    check_test_files || all_good=false
    echo
    
    # 总结
    echo "=================================================="
    if [ "$all_good" = true ]; then
        log_success "✅ 系统检查完成，所有核心组件正常！"
        echo "您可以运行: ./run-test.sh simple"
    else
        log_warning "⚠️  发现一些问题，请根据上述提示解决"
        echo "常见解决方案："
        echo "1. 启动Spring Boot: cd ../.. && mvn spring-boot:run"
        echo "2. 启动MySQL: brew services start mysql"
        echo "3. 安装JMeter: brew install jmeter"
    fi
    echo "=================================================="
}

# 运行检查
main "$@" 