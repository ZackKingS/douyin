@echo off
chcp 65001 >nul
echo ========================================
echo   抖音后端服务启动脚本
echo ========================================
echo.

cd /d "%~dp0"

echo [1/3] 检查 MySQL 服务...
netstat -ano | findstr ":3306" >nul
if %errorlevel% neq 0 (
    echo [错误] MySQL 服务未启动，请先启动 MySQL
    pause
    exit /b 1
)
echo [OK] MySQL 服务已运行

echo.
echo [2/3] 检查端口 8080...
netstat -ano | findstr ":8080" >nul
if %errorlevel% equ 0 (
    echo [警告] 端口 8080 已被占用，可能是服务已在运行
    echo.
    set /p choice=是否继续启动? (Y/N):
    if /i not "%choice%"=="Y" exit /b 1
)
echo [OK] 端口 8080 可用

echo.
echo [3/3] 启动 Spring Boot 服务...
echo ----------------------------------------
echo 服务启动后访问地址: http://localhost:8080/api/v1
echo 按 Ctrl+C 可以停止服务
echo ----------------------------------------
echo.

mvn spring-boot:run

pause
