#!/bin/bash

# 饿了么外卖平台JMeter自动化测试脚本
# 使用方法: ./run-test.sh [test-type]
# test-type: all | auth | business | load

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置参数
BASE_URL="http://localhost:8080/api"
JMETER_HOME=${JMETER_HOME:-"../apache-jmeter-5.6.3"}
RESULTS_DIR="./results"
REPORT_DIR="./reports"

# 函数定义
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查系统可用性
check_system() {
    log_info "检查系统可用性..."
    
    # 检查JMeter是否可用
    if ! command -v jmeter &> /dev/null; then
        if [ ! -f "$JMETER_HOME/bin/jmeter" ]; then
            log_error "JMeter未找到，请安装JMeter或设置JMETER_HOME环境变量"
            exit 1
        else
            export PATH="$JMETER_HOME/bin:$PATH"
        fi
    fi
    
    # 检查被测系统
    log_info "检查被测系统连接性..."
    if ! curl -s --connect-timeout 5 "$BASE_URL/business/getAll" > /dev/null; then
        log_warning "无法连接到被测系统 $BASE_URL"
        log_info "请确保Spring Boot应用正在运行在端口8080"
        read -p "是否继续执行测试? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    else
        log_success "系统连接正常"
    fi
}

# 创建结果目录
prepare_dirs() {
    log_info "准备测试目录..."
    mkdir -p "$RESULTS_DIR"
    mkdir -p "$REPORT_DIR"
    
    # 清理旧的结果文件
    rm -f "$RESULTS_DIR"/*.jtl
    rm -rf "$REPORT_DIR"/*
}

# 执行用户认证测试
run_auth_test() {
    log_info "执行用户认证功能测试..."
    
    local test_file="scenarios/user-auth-test.jmx"
    local result_file="$RESULTS_DIR/user-auth-$(date +%Y%m%d_%H%M%S).jtl"
    local report_dir="$REPORT_DIR/user-auth-report"
    
    if [ ! -f "$test_file" ]; then
        log_error "测试文件不存在: $test_file"
        return 1
    fi
    
    jmeter -n -t "$test_file" \
           -l "$result_file" \
           -e -o "$report_dir" \
           -JTHREAD_COUNT=10 \
           -JRAMP_PERIOD=30 \
           -JLOOP_COUNT=5
    
    if [ $? -eq 0 ]; then
        log_success "用户认证测试完成，报告生成在: $report_dir"
    else
        log_error "用户认证测试失败"
        return 1
    fi
}

# 执行商家菜品测试
run_business_test() {
    log_info "执行商家菜品查询测试..."
    
    local test_file="scenarios/business-food-test.jmx"
    local result_file="$RESULTS_DIR/business-food-$(date +%Y%m%d_%H%M%S).jtl"
    local report_dir="$REPORT_DIR/business-food-report"
    
    if [ ! -f "$test_file" ]; then
        log_error "测试文件不存在: $test_file"
        return 1
    fi
    
    jmeter -n -t "$test_file" \
           -l "$result_file" \
           -e -o "$report_dir" \
           -JTHREAD_COUNT=20 \
           -JRAMP_PERIOD=60 \
           -JLOOP_COUNT=10
    
    if [ $? -eq 0 ]; then
        log_success "商家菜品测试完成，报告生成在: $report_dir"
    else
        log_error "商家菜品测试失败"
        return 1
    fi
}

# 执行简单连接测试
run_simple_test() {
    log_info "执行简单连接测试..."
    
    local test_file="scenarios/simple-test.jmx"
    local result_file="$RESULTS_DIR/simple-test-$(date +%Y%m%d_%H%M%S).jtl"
    local report_dir="$REPORT_DIR/simple-test-report"
    
    if [ ! -f "$test_file" ]; then
        log_error "测试文件不存在: $test_file"
        return 1
    fi
    
    jmeter -n -t "$test_file" \
           -l "$result_file" \
           -e -o "$report_dir" \
           -JTHREAD_COUNT=1 \
           -JRAMP_PERIOD=1 \
           -JLOOP_COUNT=1
    
    if [ $? -eq 0 ]; then
        log_success "简单连接测试完成，报告生成在: $report_dir"
        
        # 检查是否有错误
        local error_count=$(grep -c "false" "$result_file" 2>/dev/null || echo "0")
        error_count=${error_count:-0}
        if [ "$error_count" -gt 0 ]; then
            log_warning "发现 $error_count 个错误，请检查系统是否正常运行"
            log_info "请确保："
            echo "  1. Spring Boot应用在端口8080运行"
            echo "  2. 可以访问 http://localhost:8080/api/business/getAll"
        else
            log_success "所有测试通过！"
        fi
    else
        log_error "简单连接测试失败"
        return 1
    fi
}

# 执行负载测试
run_load_test() {
    log_info "执行系统负载测试..."
    
    local test_file="eleme-system-test.jmx"
    local result_file="$RESULTS_DIR/load-test-$(date +%Y%m%d_%H%M%S).jtl"
    local report_dir="$REPORT_DIR/load-test-report"
    
    if [ ! -f "$test_file" ]; then
        log_error "测试文件不存在: $test_file"
        return 1
    fi
    
    log_warning "开始负载测试，这可能需要较长时间..."
    
    jmeter -n -t "$test_file" \
           -l "$result_file" \
           -e -o "$report_dir" \
           -JTHREAD_COUNT=50 \
           -JRAMP_PERIOD=120 \
           -JLOOP_COUNT=20
    
    if [ $? -eq 0 ]; then
        log_success "负载测试完成，报告生成在: $report_dir"
    else
        log_error "负载测试失败"
        return 1
    fi
}

# 执行订单流程测试
run_order_test() {
    log_info "执行完整订单流程测试..."
    
    local test_file="scenarios/order-flow-test.jmx"
    local result_file="$RESULTS_DIR/order-flow-$(date +%Y%m%d_%H%M%S).jtl"
    local report_dir="$REPORT_DIR/order-flow-report"
    
    if [ ! -f "$test_file" ]; then
        log_error "测试文件不存在: $test_file"
        return 1
    fi
    
    jmeter -n -t "$test_file" \
           -l "$result_file" \
           -e -o "$report_dir" \
           -JTHREAD_COUNT=3 \
           -JRAMP_PERIOD=5 \
           -JLOOP_COUNT=1
    
    if [ $? -eq 0 ]; then
        log_success "订单流程测试完成，报告生成在: $report_dir"
    else
        log_error "订单流程测试失败"
        return 1
    fi
}

# 执行购物车操作测试
run_cart_test() {
    log_info "执行购物车操作测试..."
    
    local test_file="scenarios/cart-operations-test.jmx"
    local result_file="$RESULTS_DIR/cart-operations-$(date +%Y%m%d_%H%M%S).jtl"
    local report_dir="$REPORT_DIR/cart-operations-report"
    
    if [ ! -f "$test_file" ]; then
        log_error "测试文件不存在: $test_file"
        return 1
    fi
    
    jmeter -n -t "$test_file" \
           -l "$result_file" \
           -e -o "$report_dir" \
           -JTHREAD_COUNT=4 \
           -JRAMP_PERIOD=8 \
           -JLOOP_COUNT=2
    
    if [ $? -eq 0 ]; then
        log_success "购物车操作测试完成，报告生成在: $report_dir"
    else
        log_error "购物车操作测试失败"
        return 1
    fi
}

# 执行安全测试
run_security_test() {
    log_info "执行安全漏洞测试..."
    
    local test_file="scenarios/security-test.jmx"
    local result_file="$RESULTS_DIR/security-test-$(date +%Y%m%d_%H%M%S).jtl"
    local report_dir="$REPORT_DIR/security-test-report"
    
    if [ ! -f "$test_file" ]; then
        log_error "测试文件不存在: $test_file"
        return 1
    fi
    
    jmeter -n -t "$test_file" \
           -l "$result_file" \
           -e -o "$report_dir" \
           -JTHREAD_COUNT=2 \
           -JRAMP_PERIOD=3 \
           -JLOOP_COUNT=1
    
    if [ $? -eq 0 ]; then
        log_success "安全测试完成，报告生成在: $report_dir"
        log_info "请检查安全测试报告，确保系统防护有效"
    else
        log_error "安全测试失败"
        return 1
    fi
}

# 执行性能测试
run_performance_test() {
    log_info "执行性能压力测试..."
    
    local test_file="scenarios/performance-test.jmx"
    local result_file="$RESULTS_DIR/performance-test-$(date +%Y%m%d_%H%M%S).jtl"
    local report_dir="$REPORT_DIR/performance-test-report"
    
    if [ ! -f "$test_file" ]; then
        log_error "测试文件不存在: $test_file"
        return 1
    fi
    
    log_warning "开始性能压力测试，这将产生高负载..."
    
    jmeter -n -t "$test_file" \
           -l "$result_file" \
           -e -o "$report_dir" \
           -JTHREAD_COUNT=50 \
           -JRAMP_PERIOD=30 \
           -JLOOP_COUNT=10
    
    if [ $? -eq 0 ]; then
        log_success "性能测试完成，报告生成在: $report_dir"
    else
        log_error "性能测试失败"
        return 1
    fi
}

# 执行综合负载测试
run_comprehensive_load_test() {
    log_info "执行综合负载测试 (日常、峰值、混合负载)..."
    
    local test_file="scenarios/load-testing-comprehensive.jmx"
    local result_file="$RESULTS_DIR/comprehensive-load-$(date +%Y%m%d_%H%M%S).jtl"
    local report_dir="$REPORT_DIR/comprehensive-load-report"
    
    if [ ! -f "$test_file" ]; then
        log_error "测试文件不存在: $test_file"
        return 1
    fi
    
    log_warning "开始综合负载测试，包含以下阶段："
    echo "  1️⃣ 日常负载测试 (20用户, 5分钟)"
    echo "  2️⃣ 峰值负载测试 (100用户, 10分钟)"
    echo "  3️⃣ 混合负载测试 (50用户, 15分钟)"
    echo "  📊 总测试时间约30分钟"
    
    # 启动系统资源监控
    local monitor_pid=""
    if [[ -f "./monitor-system-resources.sh" ]]; then
        log_info "🖥️  启动系统资源监控..."
        chmod +x "./monitor-system-resources.sh"
        ./monitor-system-resources.sh &
        monitor_pid=$!
        log_success "系统监控已启动 (PID: $monitor_pid)"
        sleep 2
    fi
    
    # 运行综合负载测试
    jmeter -n -t "$test_file" \
           -l "$result_file" \
           -e -o "$report_dir" \
           -JDAILY_USERS=20 \
           -JPEAK_USERS=100 \
           -JBASE_URL="$BASE_URL"
    
    # 停止系统资源监控
    if [[ -n "$monitor_pid" ]]; then
        log_info "🛑 停止系统资源监控..."
        kill $monitor_pid 2>/dev/null
        wait $monitor_pid 2>/dev/null
        log_success "系统监控已停止"
    fi
    
    if [ $? -eq 0 ]; then
        log_success "综合负载测试完成，报告生成在: $report_dir"
        
        # 分析测试结果
        if [[ -f "$result_file" ]]; then
            log_info "📈 测试结果分析："
            local total_samples=$(wc -l < "$result_file")
            local error_count=$(grep -c "false" "$result_file" 2>/dev/null || echo "0")
            local success_rate=$(echo "scale=2; ($total_samples - $error_count) * 100 / $total_samples" | bc 2>/dev/null || echo "N/A")
            
            echo "  📊 总请求数: $total_samples"
            echo "  ❌ 错误数: $error_count"
            echo "  ✅ 成功率: $success_rate%"
            
            if [[ -d "./reports/system-monitoring" ]]; then
                log_info "🖥️  系统监控报告已保存在: ./reports/system-monitoring"
            fi
        fi
    else
        log_error "综合负载测试失败"
        return 1
    fi
}

# 显示帮助信息
show_help() {
    echo "饿了么外卖平台JMeter测试脚本"
    echo ""
    echo "使用方法:"
    echo "  $0 [命令]"
    echo ""
    echo "命令:"
    echo "  simple      - 执行简单连接测试"
    echo "  auth        - 执行用户认证测试"
    echo "  business    - 执行商家菜品测试"
    echo "  order       - 执行完整订单流程测试"
    echo "  cart        - 执行购物车操作测试"
    echo "  security    - 执行安全漏洞测试"
    echo "  performance - 执行性能压力测试"
    echo "  load        - 执行负载测试(旧版)"
    echo "  comprehensive - 执行综合负载测试(日常/峰值/混合+监控)"
    echo "  all         - 执行所有测试"
    echo "  help        - 显示帮助信息"
    echo ""
    echo "环境变量:"
    echo "  JMETER_HOME  - JMeter安装目录 (默认: ../apache-jmeter-5.6.3)"
    echo "  BASE_URL     - 被测系统URL (默认: http://localhost:8080/api)"
    echo ""
    echo "示例:"
    echo "  $0 auth"
    echo "  JMETER_HOME=/opt/jmeter $0 load"
}

# 显示测试结果摘要
show_summary() {
    log_info "测试结果摘要:"
    echo "----------------------------------------"
    
    if [ -d "$REPORT_DIR" ]; then
        for dir in "$REPORT_DIR"/*; do
            if [ -d "$dir" ]; then
                local test_name=$(basename "$dir")
                local index_file="$dir/index.html"
                if [ -f "$index_file" ]; then
                    echo "📊 $test_name: file://$PWD/$index_file"
                fi
            fi
        done
    fi
    
    echo "----------------------------------------"
    echo "📁 结果文件目录: $PWD/$RESULTS_DIR"
    echo "📁 报告目录: $PWD/$REPORT_DIR"
}

# 主函数
main() {
    local test_type=${1:-"help"}
    
    echo "============================================"
    echo "🍔 饿了么外卖平台 JMeter 系统测试"
    echo "============================================"
    
    case $test_type in
        "simple")
            check_system
            prepare_dirs
            run_simple_test
            show_summary
            ;;
        "auth")
            check_system
            prepare_dirs
            run_auth_test
            show_summary
            ;;
        "business")
            check_system
            prepare_dirs
            run_business_test
            show_summary
            ;;
        "order")
            check_system
            prepare_dirs
            run_order_test
            show_summary
            ;;
        "cart")
            check_system
            prepare_dirs
            run_cart_test
            show_summary
            ;;
        "security")
            check_system
            prepare_dirs
            run_security_test
            show_summary
            ;;
        "performance")
            check_system
            prepare_dirs
            run_performance_test
            show_summary
            ;;
        "load")
            check_system
            prepare_dirs
            run_load_test
            show_summary
            ;;
        "comprehensive")
            check_system
            prepare_dirs
            run_comprehensive_load_test
            show_summary
            ;;
        "all")
            check_system
            prepare_dirs
            
            log_info "开始完整测试流程..."
            
            # 基础功能测试
            run_simple_test && sleep 2
            run_auth_test && sleep 3
            run_business_test && sleep 3
            
            # 业务流程测试
            run_order_test && sleep 5
            run_cart_test && sleep 5
            
            # 安全测试
            run_security_test && sleep 3
            
            # 性能测试（最后执行）
            log_warning "准备执行性能测试，这将产生高负载..."
            sleep 5
            run_performance_test
            
            show_summary
            ;;
        "help"|*)
            show_help
            ;;
    esac
}

# 脚本入口
main "$@" 