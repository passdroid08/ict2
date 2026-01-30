# ICT2 Project

ICT 5th 2team project - React + Spring Boot + JDBC + MySQL

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
- MySQL 8.x
- Maven

### Frontend
- React 18.2.0
- Axios
- React Scripts

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
