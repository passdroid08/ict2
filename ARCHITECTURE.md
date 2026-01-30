# ICT2 프로젝트 아키텍처 (Project Architecture)

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
│  │  @RestController                                      │  │
│  │  - UserController.java                               │  │
│  │    GET    /api/users      (전체 조회)                 │  │
│  │    GET    /api/users/{id} (단일 조회)                 │  │
│  │    POST   /api/users      (생성)                      │  │
│  │    PUT    /api/users/{id} (수정)                      │  │
│  │    DELETE /api/users/{id} (삭제)                      │  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Repository Layer                                     │  │
│  │  @Repository                                          │  │
│  │  - UserRepository.java                               │  │
│  │  - JdbcTemplate (Spring JDBC)                        │  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Model Layer                                          │  │
│  │  - User.java (Entity)                                │  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Configuration                                        │  │
│  │  - WebConfig.java (CORS)                             │  │
│  │  - application.properties                            │  │
│  └──────────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        │ JDBC Connection
                        │ jdbc:mysql://localhost:3306/ictdb
                        ▼
┌─────────────────────────────────────────────────────────────┐
│                   MySQL Database (Port 3306)                 │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Database: ictdb                                      │  │
│  │  ┌────────────────────────────────────────────────┐  │  │
│  │  │  Table: users                                   │  │  │
│  │  │  - id (BIGINT, PK, AUTO_INCREMENT)             │  │  │
│  │  │  - name (VARCHAR(255))                         │  │  │
│  │  │  - email (VARCHAR(255), UNIQUE)                │  │  │
│  │  │  - created_at (TIMESTAMP)                      │  │  │
│  │  └────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## 데이터 흐름 (Data Flow)

### 1. 사용자 목록 조회 (Get All Users)
```
User → React App → GET /api/users → UserController → UserRepository 
→ JdbcTemplate → MySQL → 결과 반환 → JSON 응답 → React 화면 표시
```

### 2. 사용자 생성 (Create User)
```
User → UserForm (입력) → POST /api/users → UserController 
→ UserRepository → JdbcTemplate → INSERT INTO users → MySQL 
→ 성공 응답 → React 목록 갱신
```

### 3. 사용자 수정 (Update User)
```
User → Edit 버튼 → UserForm (기존 데이터 로드) → PUT /api/users/{id} 
→ UserController → UserRepository → JdbcTemplate → UPDATE users 
→ MySQL → 성공 응답 → React 목록 갱신
```

### 4. 사용자 삭제 (Delete User)
```
User → Delete 버튼 → 확인 → DELETE /api/users/{id} → UserController 
→ UserRepository → JdbcTemplate → DELETE FROM users → MySQL 
→ 성공 응답 → React 목록 갱신
```

## 기술 스택 설명

### Frontend (React)
- **React**: 컴포넌트 기반 UI 라이브러리
- **Axios**: Promise 기반 HTTP 클라이언트
- **React Hooks**: useState, useEffect를 사용한 상태 관리
- **CSS**: 컴포넌트별 스타일링

### Backend (Spring Boot)
- **Spring Boot**: 자동 설정과 내장 서버로 빠른 개발
- **Spring JDBC**: JdbcTemplate를 통한 데이터베이스 접근
- **Spring MVC**: RESTful API 구현
- **Maven**: 의존성 관리 및 빌드 도구

### Database (MySQL)
- **MySQL**: 관계형 데이터베이스
- **JDBC Driver**: Java와 MySQL 연결

## CORS 설정

프론트엔드(http://localhost:3000)와 백엔드(http://localhost:8080)가 다른 포트에서 실행되므로 CORS(Cross-Origin Resource Sharing) 설정이 필요합니다.

**WebConfig.java**에서 CORS 허용:
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

## REST API 명세

| Method | Endpoint | Request Body | Response | Description |
|--------|----------|--------------|----------|-------------|
| GET | /api/users | - | User[] | 모든 사용자 조회 |
| GET | /api/users/{id} | - | User | 특정 사용자 조회 |
| POST | /api/users | `{name, email}` | String | 새 사용자 생성 |
| PUT | /api/users/{id} | `{name, email}` | String | 사용자 정보 수정 |
| DELETE | /api/users/{id} | - | String | 사용자 삭제 |

### User 객체 구조
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com"
}
```

## 보안 고려사항

1. **SQL Injection 방지**: JdbcTemplate의 PreparedStatement 사용
2. **CORS 설정**: 허용된 Origin만 접근 가능
3. **입력 검증**: 프론트엔드와 백엔드에서 데이터 검증 필요 (추후 추가)
4. **인증/인가**: 현재는 미구현 (추후 Spring Security 추가 가능)

## 향후 개선 사항

- [ ] Spring Security를 통한 인증/인가 추가
- [ ] JWT 토큰 기반 인증
- [ ] 입력 데이터 검증 (Validation)
- [ ] 에러 처리 개선
- [ ] 페이지네이션
- [ ] 검색 기능
- [ ] 단위 테스트 추가
- [ ] Docker 컨테이너화
