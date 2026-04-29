#!/bin/bash

# ========================================
#   抖音后端服务启动脚本
# ========================================

set -e

cd "$(dirname "$0")"

echo "[1/3] 检查 MySQL 服务..."
if ! netstat -ano | grep -q ":3306" 2>/dev/null; then
    if ! lsof -i :3306 >/dev/null 2>&1; then
        echo "[错误] MySQL 服务未启动，请先启动 MySQL"
        exit 1
    fi
fi
echo "[OK] MySQL 服务已运行"

echo ""
echo "[2/3] 检查端口 8080..."
if netstat -ano 2>/dev/null | grep -q ":8080" || lsof -i :8080 >/dev/null 2>&1; then
    echo "[警告] 端口 8080 已被占用，可能是服务已在运行"
    read -p "是否继续启动? (Y/N): " choice
    if [[ ! "$choice" =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi
echo "[OK] 端口 8080 可用"

echo ""
echo "[3/3] 启动 Spring Boot 服务..."
echo "---------------------------------------"
echo "服务启动后访问地址: http://localhost:8080/api/v1"
echo "按 Ctrl+C 可以停止服务"
echo "---------------------------------------"
echo ""

mvn spring-boot:run
