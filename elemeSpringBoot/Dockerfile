# 使用多阶段构建
FROM eclipse-temurin:21-jdk-alpine AS builder

# 设置工作目录
WORKDIR /app

# 复制pom.xml和源代码
COPY pom.xml .
COPY src ./src

# 安装Maven并构建应用
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests && \
    mv target/*.jar app.jar

# 运行阶段
FROM eclipse-temurin:21-jre-alpine

# 安装必要的包
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 创建非root用户
RUN addgroup -g 1001 -S spring && \
    adduser -S spring -u 1001 -G spring

# 设置工作目录
WORKDIR /app

# 从构建阶段复制JAR文件
COPY --from=builder /app/app.jar .

# 更改文件所有者
RUN chown spring:spring /app/app.jar

# 切换到非root用户
USER spring:spring

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"] 