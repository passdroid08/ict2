# 설치 및 실행 가이드 (Installation and Running Guide)

## 필수 요구사항 (Prerequisites)

1. **JDK 17 이상** - Spring Boot 실행을 위해 필요
2. **Maven 3.6+** - 백엔드 빌드를 위해 필요
3. **Node.js 14+** - React 앱 실행을 위해 필요
4. **MySQL 8.0+** - 데이터베이스

## 데이터베이스 설정 (Database Setup)

### 1. MySQL 설치 후 접속
```bash
mysql -u root -p
```

### 2. 데이터베이스 및 테이블 생성
```sql
-- Create database
CREATE DATABASE IF NOT EXISTS ictdb;

-- Use database
USE ictdb;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample data
INSERT INTO users (name, email) VALUES 
    ('John Doe', 'john@example.com'),
    ('Jane Smith', 'jane@example.com'),
    ('Bob Johnson', 'bob@example.com');
```

또는 다음 명령으로 스크립트 실행:
```bash
mysql -u root -p < backend/src/main/resources/schema.sql
```

### 3. 데이터베이스 설정 확인
`backend/src/main/resources/application.properties` 파일을 열어서 데이터베이스 연결 정보를 확인하고 필요시 수정하세요:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ictdb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root
```

## 백엔드 실행 (Backend Setup)

### 1. 백엔드 디렉토리로 이동
```bash
cd backend
```

### 2. Maven 의존성 설치 및 빌드
```bash
mvn clean install
```

### 3. Spring Boot 애플리케이션 실행
```bash
mvn spring-boot:run
```

또는 JAR 파일로 실행:
```bash
java -jar target/project-0.0.1-SNAPSHOT.jar
```

백엔드 서버는 **http://localhost:8080** 에서 실행됩니다.

### 백엔드 API 테스트
브라우저나 curl로 테스트:
```bash
curl http://localhost:8080/api/users
```

## 프론트엔드 실행 (Frontend Setup)

### 1. 프론트엔드 디렉토리로 이동
```bash
cd frontend
```

### 2. npm 패키지 설치
```bash
npm install
```

### 3. React 개발 서버 실행
```bash
npm start
```

프론트엔드는 **http://localhost:3000** 에서 실행되며, 자동으로 브라우저가 열립니다.

## 전체 애플리케이션 실행 순서

1. **MySQL 데이터베이스 시작**
2. **터미널 1**: 백엔드 실행
   ```bash
   cd backend
   mvn spring-boot:run
   ```
3. **터미널 2**: 프론트엔드 실행
   ```bash
   cd frontend
   npm start
   ```

## 주요 기능 (Features)

- ✅ 사용자 목록 조회
- ✅ 새 사용자 추가
- ✅ 사용자 정보 수정
- ✅ 사용자 삭제
- ✅ 반응형 UI

## 트러블슈팅 (Troubleshooting)

### MySQL 연결 오류
```
com.mysql.cj.jdbc.exceptions.CommunicationsException
```
- MySQL 서버가 실행 중인지 확인
- application.properties의 데이터베이스 연결 정보 확인
- MySQL 사용자 권한 확인

### 프론트엔드에서 백엔드 연결 실패
```
Network Error
```
- 백엔드 서버가 http://localhost:8080에서 실행 중인지 확인
- CORS 설정이 올바른지 확인 (WebConfig.java)

### 포트 충돌
- 백엔드: 8080 포트가 이미 사용 중이면 `application.properties`에서 `server.port` 변경
- 프론트엔드: 3000 포트가 사용 중이면 다른 포트 사용 제안이 표시됨

## 프로젝트 구조 설명

```
ict2/
├── backend/                          # Spring Boot 백엔드
│   ├── src/main/java/com/ict/project/
│   │   ├── ProjectApplication.java   # Spring Boot 메인 클래스
│   │   ├── config/
│   │   │   └── WebConfig.java        # CORS 설정
│   │   ├── controller/
│   │   │   └── UserController.java   # REST API 컨트롤러
│   │   ├── model/
│   │   │   └── User.java             # 사용자 엔티티
│   │   └── repository/
│   │       └── UserRepository.java   # JDBC 데이터 접근 계층
│   ├── src/main/resources/
│   │   ├── application.properties    # 설정 파일
│   │   └── schema.sql                # 데이터베이스 스키마
│   └── pom.xml                       # Maven 의존성 설정
│
└── frontend/                         # React 프론트엔드
    ├── public/
    │   └── index.html                # HTML 템플릿
    ├── src/
    │   ├── components/
    │   │   ├── UserForm.js           # 사용자 폼 컴포넌트
    │   │   ├── UserForm.css
    │   │   ├── UserList.js           # 사용자 목록 컴포넌트
    │   │   └── UserList.css
    │   ├── App.js                    # 메인 앱 컴포넌트
    │   ├── App.css
    │   ├── index.js                  # React 진입점
    │   └── index.css
    └── package.json                  # npm 의존성 설정
```

## 기술 스택 상세

### Backend
- **Spring Boot 3.2.1** - 프레임워크
- **Spring JDBC** - 데이터베이스 연동
- **MySQL Connector/J** - MySQL 드라이버
- **Maven** - 빌드 도구

### Frontend
- **React 18.2.0** - UI 라이브러리
- **Axios 1.6.0** - HTTP 클라이언트
- **React Scripts 5.0.1** - 빌드 도구

### Database
- **MySQL 8.x** - 관계형 데이터베이스
