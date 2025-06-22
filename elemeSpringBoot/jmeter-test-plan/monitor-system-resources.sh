#!/bin/bash

# 系统资源监控脚本
# 用于在JMeter负载测试期间监控硬件资源使用情况

# 配置参数
LOG_DIR="./reports/system-monitoring"
MONITOR_INTERVAL=5  # 监控间隔（秒）
CPU_THRESHOLD=80    # CPU使用率阈值
MEMORY_THRESHOLD=85 # 内存使用率阈值
DISK_THRESHOLD=90   # 磁盘使用率阈值

# 颜色定义
RED='\033[0;31m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color

# 创建日志目录
mkdir -p "$LOG_DIR"

# 获取当前时间戳
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
CPU_LOG="$LOG_DIR/cpu_usage_$TIMESTAMP.log"
MEMORY_LOG="$LOG_DIR/memory_usage_$TIMESTAMP.log"
DISK_LOG="$LOG_DIR/disk_usage_$TIMESTAMP.log"
NETWORK_LOG="$LOG_DIR/network_usage_$TIMESTAMP.log"
ALERT_LOG="$LOG_DIR/alerts_$TIMESTAMP.log"

# 初始化日志文件
echo "时间戳,CPU使用率(%),负载1分钟,负载5分钟,负载15分钟" > "$CPU_LOG"
echo "时间戳,内存总量(GB),已使用内存(GB),内存使用率(%),交换区使用率(%)" > "$MEMORY_LOG"
echo "时间戳,磁盘总量(GB),已使用磁盘(GB),磁盘使用率(%),可用空间(GB)" > "$DISK_LOG"
echo "时间戳,网络接收(MB/s),网络发送(MB/s),连接数" > "$NETWORK_LOG"
echo "时间戳,告警类型,告警内容,当前值,阈值" > "$ALERT_LOG"

# 函数：记录告警
log_alert() {
    local alert_type="$1"
    local alert_content="$2"
    local current_value="$3"
    local threshold="$4"
    local timestamp=$(date +"%Y-%m-%d %H:%M:%S")
    
    echo "$timestamp,$alert_type,$alert_content,$current_value,$threshold" >> "$ALERT_LOG"
    echo -e "${RED}[告警] $timestamp - $alert_type: $alert_content (当前值: $current_value, 阈值: $threshold)${NC}"
}

# 函数：监控CPU使用率
monitor_cpu() {
    local timestamp=$(date +"%Y-%m-%d %H:%M:%S")
    
    # 获取CPU使用率和负载
    local cpu_usage=$(top -l 1 | grep "CPU usage" | awk '{print $3}' | sed 's/%//')
    local load_avg=$(uptime | awk -F'load averages:' '{print $2}' | sed 's/^ *//')
    local load_1min=$(echo $load_avg | awk '{print $1}')
    local load_5min=$(echo $load_avg | awk '{print $2}')
    local load_15min=$(echo $load_avg | awk '{print $3}')
    
    # 如果CPU使用率为空，使用iostat获取
    if [[ -z "$cpu_usage" ]]; then
        cpu_usage=$(iostat -c 2 | tail -1 | awk '{print 100-$6}')
    fi
    
    echo "$timestamp,$cpu_usage,$load_1min,$load_5min,$load_15min" >> "$CPU_LOG"
    
    # 检查CPU使用率阈值
    if (( $(echo "$cpu_usage > $CPU_THRESHOLD" | bc -l) )); then
        log_alert "CPU" "CPU使用率过高" "$cpu_usage%" "$CPU_THRESHOLD%"
    fi
    
    echo -e "${GREEN}CPU使用率: $cpu_usage% | 负载: $load_1min, $load_5min, $load_15min${NC}"
}

# 函数：监控内存使用
monitor_memory() {
    local timestamp=$(date +"%Y-%m-%d %H:%M:%S")
    
    # 获取内存信息 (macOS)
    local memory_info=$(vm_stat)
    local page_size=$(vm_stat | grep "page size" | awk '{print $8}')
    local pages_free=$(echo "$memory_info" | grep "Pages free" | awk '{print $3}' | sed 's/\.//')
    local pages_active=$(echo "$memory_info" | grep "Pages active" | awk '{print $3}' | sed 's/\.//')
    local pages_inactive=$(echo "$memory_info" | grep "Pages inactive" | awk '{print $3}' | sed 's/\.//')
    local pages_speculative=$(echo "$memory_info" | grep "Pages speculative" | awk '{print $3}' | sed 's/\.//')
    local pages_wired=$(echo "$memory_info" | grep "Pages wired down" | awk '{print $4}' | sed 's/\.//')
    
    # 计算内存使用情况 (GB)
    local total_pages=$((pages_free + pages_active + pages_inactive + pages_speculative + pages_wired))
    local used_pages=$((pages_active + pages_inactive + pages_speculative + pages_wired))
    local total_memory_gb=$(echo "scale=2; $total_pages * $page_size / 1024 / 1024 / 1024" | bc)
    local used_memory_gb=$(echo "scale=2; $used_pages * $page_size / 1024 / 1024 / 1024" | bc)
    local memory_usage_percent=$(echo "scale=2; $used_memory_gb * 100 / $total_memory_gb" | bc)
    
    # 获取交换区使用情况
    local swap_usage=$(sysctl vm.swapusage | awk '{print $7}' | sed 's/[()%]//g')
    
    echo "$timestamp,$total_memory_gb,$used_memory_gb,$memory_usage_percent,$swap_usage" >> "$MEMORY_LOG"
    
    # 检查内存使用率阈值
    if (( $(echo "$memory_usage_percent > $MEMORY_THRESHOLD" | bc -l) )); then
        log_alert "内存" "内存使用率过高" "$memory_usage_percent%" "$MEMORY_THRESHOLD%"
    fi
    
    echo -e "${GREEN}内存使用: ${used_memory_gb}GB/${total_memory_gb}GB ($memory_usage_percent%) | 交换区: $swap_usage%${NC}"
}

# 函数：监控磁盘使用
monitor_disk() {
    local timestamp=$(date +"%Y-%m-%d %H:%M:%S")
    
    # 获取根目录磁盘使用情况
    local disk_info=$(df -h / | tail -1)
    local total_disk=$(echo $disk_info | awk '{print $2}' | sed 's/G//')
    local used_disk=$(echo $disk_info | awk '{print $3}' | sed 's/G//')
    local available_disk=$(echo $disk_info | awk '{print $4}' | sed 's/G//')
    local disk_usage_percent=$(echo $disk_info | awk '{print $5}' | sed 's/%//')
    
    echo "$timestamp,$total_disk,$used_disk,$disk_usage_percent,$available_disk" >> "$DISK_LOG"
    
    # 检查磁盘使用率阈值
    if (( disk_usage_percent > DISK_THRESHOLD )); then
        log_alert "磁盘" "磁盘使用率过高" "$disk_usage_percent%" "$DISK_THRESHOLD%"
    fi
    
    echo -e "${GREEN}磁盘使用: ${used_disk}GB/${total_disk}GB ($disk_usage_percent%) | 可用: ${available_disk}GB${NC}"
}

# 函数：监控网络使用
monitor_network() {
    local timestamp=$(date +"%Y-%m-%d %H:%M:%S")
    
    # 获取网络统计信息
    local network_info=$(netstat -I en0 -b | tail -1)
    local bytes_in=$(echo $network_info | awk '{print $7}')
    local bytes_out=$(echo $network_info | awk '{print $10}')
    
    # 转换为MB/s (这里是累计值，实际应该计算差值)
    local mb_in=$(echo "scale=2; $bytes_in / 1024 / 1024" | bc)
    local mb_out=$(echo "scale=2; $bytes_out / 1024 / 1024" | bc)
    
    # 获取连接数
    local connections=$(netstat -an | grep ESTABLISHED | wc -l | tr -d ' ')
    
    echo "$timestamp,$mb_in,$mb_out,$connections" >> "$NETWORK_LOG"
    
    echo -e "${GREEN}网络: 接收 ${mb_in}MB | 发送 ${mb_out}MB | 连接数: $connections${NC}"
}

# 函数：监控Spring Boot应用
monitor_spring_boot() {
    local timestamp=$(date +"%Y-%m-%d %H:%M:%S")
    
    # 检查Spring Boot应用是否运行
    local spring_boot_pid=$(ps aux | grep "elemeSpringBoot" | grep -v grep | awk '{print $2}')
    
    if [[ -n "$spring_boot_pid" ]]; then
        # 获取应用的CPU和内存使用情况
        local app_stats=$(ps -p $spring_boot_pid -o %cpu,%mem,vsz,rss)
        local app_cpu=$(echo "$app_stats" | tail -1 | awk '{print $1}')
        local app_mem=$(echo "$app_stats" | tail -1 | awk '{print $2}')
        local app_vsz=$(echo "$app_stats" | tail -1 | awk '{print $3}')
        local app_rss=$(echo "$app_stats" | tail -1 | awk '{print $4}')
        
        echo -e "${GREEN}Spring Boot应用 (PID: $spring_boot_pid): CPU $app_cpu% | 内存 $app_mem% | VSZ ${app_vsz}KB | RSS ${app_rss}KB${NC}"
        
        # 记录到应用专用日志
        local app_log="$LOG_DIR/spring_boot_$TIMESTAMP.log"
        if [[ ! -f "$app_log" ]]; then
            echo "时间戳,PID,CPU(%),内存(%),VSZ(KB),RSS(KB)" > "$app_log"
        fi
        echo "$timestamp,$spring_boot_pid,$app_cpu,$app_mem,$app_vsz,$app_rss" >> "$app_log"
    else
        echo -e "${YELLOW}警告: Spring Boot应用未运行${NC}"
    fi
}

# 主监控循环
echo -e "${GREEN}开始系统资源监控...${NC}"
echo -e "${GREEN}监控间隔: ${MONITOR_INTERVAL}秒${NC}"
echo -e "${GREEN}日志目录: $LOG_DIR${NC}"
echo -e "${GREEN}阈值设置: CPU ${CPU_THRESHOLD}%, 内存 ${MEMORY_THRESHOLD}%, 磁盘 ${DISK_THRESHOLD}%${NC}"
echo -e "${GREEN}按 Ctrl+C 停止监控${NC}"
echo ""

# 捕获Ctrl+C信号
trap 'echo -e "\n${GREEN}监控已停止。日志文件保存在: $LOG_DIR${NC}"; exit 0' INT

# 监控循环
while true; do
    echo -e "${YELLOW}=== $(date +"%Y-%m-%d %H:%M:%S") 系统资源监控 ===${NC}"
    
    monitor_cpu
    monitor_memory
    monitor_disk
    monitor_network
    monitor_spring_boot
    
    echo ""
    sleep $MONITOR_INTERVAL
done 