# ICT2 프로젝트 아키텍처 (Oracle 버전)

## 시스템 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                        사용자 (User)                          │
│                     http://localhost:3000                    │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        │ HTTP Requests
                        ▼
┌─────────────────────────────────────────────────────────────┐
│                   React Frontend (Port 3000)                 │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Components:                                          │  │
│  │  - App.js (Main Component)                           │  │
│  │  - UserForm.js (Create/Update Form)                  │  │
│  │  - UserList.js (Display Users)                       │  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Axios HTTP Client                                    │  │
│  │  - API calls to backend                              │  │
│  └──────────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        │ REST API (JSON)
                        │ /api/users
                        ▼
┌─────────────────────────────────────────────────────────────┐
│              Spring Boot Backend (Port 8080)                 │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Controller Layer                                     │  │
│  │  - UserController.java                               │  │
│  │    GET    /api/users      (전체 조회)                 │  │
│  │    GET    /api/users/{id} (단일 조회)                 │  │
│  │    POST   /api/users      (생성)                      │  │
│  │    PUT    /api/users/{id} (수정)                      │  │
│  │    DELETE /api/users/{id} (삭제)                      │  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Repository Layer                                     │  │
│  │  - UserRepository.java                               │  │
│  │  - JdbcTemplate (Spring JDBC)                        │  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Model Layer                                          │  │
│  │  - User.java                                          │  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Configuration                                        │  │
│  │  - WebConfig.java (CORS)                             │  │
│  │  - application.properties                            │  │
│  └──────────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        │ JDBC Connection
                        │ jdbc:oracle:thin:@//localhost:1521/XEPDB1
                        ▼
┌─────────────────────────────────────────────────────────────┐
│               Oracle Database (Default Port 1521)            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Schema(User): ICTDB                                  │  │
│  │  ┌────────────────────────────────────────────────┐  │  │
│  │  │  Table: users                                   │  │  │
│  │  │  - id (NUMBER, PK, IDENTITY)                   │  │  │
│  │  │  - name (VARCHAR2(255))                        │  │  │
│  │  │  - email (VARCHAR2(255), UNIQUE)               │  │  │
│  │  │  - created_at (TIMESTAMP DEFAULT SYSTIMESTAMP) │  │  │
│  │  └────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## 데이터 흐름 (Data Flow)

### 1) 사용자 목록 조회 (Get All Users)
```
User → React App → GET /api/users → UserController → UserRepository
→ JdbcTemplate → Oracle → 결과 반환 → JSON 응답 → React 화면 표시
```

### 2) 사용자 생성 (Create User)
```
User → UserForm (입력) → POST /api/users → UserController
→ UserRepository → JdbcTemplate → INSERT INTO users → Oracle
→ 성공 응답 → React 목록 갱신
```

### 3) 사용자 수정 (Update User)
```
User → Edit 버튼 → UserForm (기존 데이터 로드) → PUT /api/users/{id}
→ UserController → UserRepository → JdbcTemplate → UPDATE users
→ Oracle → 성공 응답 → React 목록 갱신
```

### 4) 사용자 삭제 (Delete User)
```
User → Delete 버튼 → 확인 → DELETE /api/users/{id} → UserController
→ UserRepository → JdbcTemplate → DELETE FROM users → Oracle
→ 성공 응답 → React 목록 갱신
```
