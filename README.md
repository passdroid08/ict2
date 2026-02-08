# ICT2 Project (Oracle 버전)

ICT 5th 2team project - React + Spring Boot + JDBC + Oracle Database

## 문서 (Documentation)

- README_ORACLE.md: 이 파일 (Oracle 기준 빠른 시작)
- SETUP_GUIDE_ORACLE.md: 상세한 설치 및 실행 가이드 (Oracle)
- ARCHITECTURE_ORACLE.md: 시스템 아키텍처 및 데이터 흐름 (Oracle)
- PROJECT_SUMMARY_ORACLE.md: 프로젝트 요약 (Oracle 기준)

## 빠른 시작 (Quick Start)

### 필수 요구사항
- JDK 17+
- Maven 3.6+
- Node.js 14+
- Oracle Database (로컬 개발은 XE 권장) + SQL*Plus 또는 SQLcl

### 1) 데이터베이스 설정 (Oracle)
아래 스크립트를 SQL*Plus/SQLcl로 실행합니다.

예시(SQL*Plus, XE PDB 사용):
```bash
# SYSTEM 계정으로 접속 (비밀번호는 설치 시 설정값)
sqlplus system/비밀번호@//localhost:1521/XEPDB1

# 스키마(사용자) 및 테이블 생성 스크립트 실행
@backend/src/main/resources/schema_oracle.sql
```

### 2) 백엔드 실행
```bash
cd backend
mvn spring-boot:run
```
서버: http://localhost:8080

### 3) 프론트엔드 실행 (새 터미널)
```bash
cd frontend
npm install
npm start
```
앱: http://localhost:3000

## 환경 설정

백엔드의 데이터베이스 연결 정보를 수정하려면 `backend/src/main/resources/application.properties`를 편집하세요.

예시(Oracle XE PDB: XEPDB1):
```properties
spring.datasource.url=jdbc:oracle:thin:@//localhost:1521/XEPDB1
spring.datasource.username=ICTDB
spring.datasource.password=ictdb
```

예시(Oracle XE SID: XE, 구버전 방식):
```properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:XE
spring.datasource.username=ICTDB
spring.datasource.password=ictdb
```
