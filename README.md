# ICT2 Project

ICT 5th 2team project - React + Spring Boot + JDBC + MySQL

## 📚 문서 (Documentation)

- **[README.md](README.md)** - 이 파일: 프로젝트 개요 및 빠른 시작
- **[SETUP_GUIDE.md](SETUP_GUIDE.md)** - 상세한 설치 및 실행 가이드
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - 시스템 아키텍처 및 데이터 흐름 설명
- **[PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)** - 프로젝트 완성도 및 보안 검증 결과

## ⚡ 빠른 시작 (Quick Start)

### 필수 요구사항
- JDK 17+
- Maven 3.6+
- Node.js 14+
- MySQL 8.0+

### 1. 데이터베이스 설정
```bash
mysql -u root -p < backend/src/main/resources/schema.sql
```

### 2. 백엔드 실행
```bash
cd backend
mvn spring-boot:run
```
서버: http://localhost:8080

### 3. 프론트엔드 실행 (새 터미널)
```bash
cd frontend
npm install
npm start
```
앱: http://localhost:3000

## 프로젝트 구조

```
ict2/
├── backend/           # Spring Boot 백엔드
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/ict/project/
│   │   │   │   ├── ProjectApplication.java
│   │   │   │   ├── config/
│   │   │   │   ├── controller/
│   │   │   │   ├── model/
│   │   │   │   └── repository/
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── schema.sql
│   │   └── test/
│   └── pom.xml
└── frontend/          # React 프론트엔드
    ├── public/
    ├── src/
    │   ├── components/
    │   ├── App.js
    │   └── index.js
    └── package.json
```

## 기술 스택

### Backend
- Spring Boot 3.2.1
- Spring JDBC
- Spring Validation
- MySQL 8.x
- Maven

### Frontend
- React 18.2.0
- Axios 1.13.4
- React Scripts

## ✅ 보안 검증 완료

- ✅ npm 패키지: 취약점 없음
- ✅ Maven 패키지: 취약점 없음
- ✅ CodeQL 분석: 경고 없음
- ✅ 환경 변수를 통한 자격 증명 관리
- ✅ Bean Validation 입력 검증
- ✅ SQL Injection 방지

## 설치 및 실행 방법

### 1. MySQL 데이터베이스 설정

```bash
# MySQL 접속
mysql -u root -p

# 데이터베이스 및 테이블 생성
source backend/src/main/resources/schema.sql
```

또는 MySQL Workbench를 사용하여 `schema.sql` 파일을 실행하세요.

### 2. Backend 실행

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

서버는 `http://localhost:8080`에서 실행됩니다.

### 3. Frontend 실행

```bash
cd frontend
npm install
npm start
```

애플리케이션은 `http://localhost:3000`에서 실행됩니다.

## API 엔드포인트

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/users | 모든 사용자 조회 |
| GET | /api/users/{id} | 특정 사용자 조회 |
| POST | /api/users | 새 사용자 생성 |
| PUT | /api/users/{id} | 사용자 정보 수정 |
| DELETE | /api/users/{id} | 사용자 삭제 |

## 주요 기능

- 사용자 목록 조회
- 사용자 추가
- 사용자 정보 수정
- 사용자 삭제
- 반응형 UI 디자인

## 환경 설정

### application.properties
백엔드의 데이터베이스 연결 정보를 수정하려면 `backend/src/main/resources/application.properties` 파일을 편집하세요:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ictdb
spring.datasource.username=root
spring.datasource.password=root
```

## 개발팀

ICT 5th 2team
