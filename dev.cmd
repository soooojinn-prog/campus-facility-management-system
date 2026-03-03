@echo off
title CFMS Dev

where java >nul 2>nul
if %ErrorLevel% neq 0 (
  echo [ERROR] Java가 감지되지 않았습니다. JDK 21 혹은 JDK 25를 설치해 주세요.
  pause
  exit /b 1
)

echo [INFO] 프로젝트 빌드 중...
call gradlew build
if %ErrorLevel% neq 0 (
  echo [ERROR] Gradle 빌드 실패.
  pause
  exit /b 1
)

echo [INFO] 프로젝트 및 웹 브라우저 시작 중...
start "1" cmd /k "java -jar build\libs\campus-facility-management-system-1.0.0.jar --spring.profiles.active=dev"
start "2" "http://localhost:8080"

echo [INFO] 끝.
exit /b 0
