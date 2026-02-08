# 설치 및 실행 가이드 (Oracle 버전)

## 필수 요구사항 (Prerequisites)

1. JDK 17 이상 (Spring Boot 실행)
2. Maven 3.6+ (백엔드 빌드)
3. Node.js 14+ (React 앱 실행)
4. Oracle Database (로컬은 XE 권장) + SQL*Plus 또는 SQLcl

## 데이터베이스 설정 (Database Setup)

이 프로젝트는 JDBC로 Oracle DB에 연결합니다. 기존 MySQL용 안내는 Oracle 기준으로 아래처럼 바꿔서 진행합니다.

### 1) Oracle 접속
아래는 예시입니다. 환경에 따라 서비스명(XEPDB1) 또는 SID(XE)가 다를 수 있습니다.

```bash
# 서비스명(PDB) 방식 예시
sqlplus system/비밀번호@//localhost:1521/XEPDB1
```

### 2) 스키마(사용자) 및 테이블 생성
`backend/src/main/resources/schema_oracle.sql`을 실행합니다.

```sql
@backend/src/main/resources/schema_oracle.sql
```

스크립트가 하는 일:
- 사용자(스키마) ICTDB 생성 및 권한 부여
- users 테이블 생성 (ID는 Oracle IDENTITY 사용)
- 샘플 데이터 3건 INSERT

### 3) 백엔드 DB 설정 확인
`backend/src/main/resources/application.properties`에서 접속 정보를 확인/수정합니다.

서비스명(PDB) 방식 예시:
```properties
spring.datasource.url=jdbc:oracle:thin:@//localhost:1521/XEPDB1
spring.datasource.username=ICTDB
spring.datasource.password=ictdb
```

SID 방식 예시(구버전 XE):
```properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:XE
spring.datasource.username=ICTDB
spring.datasource.password=ictdb
```

## 백엔드 실행 (Backend Setup)

### 1) 백엔드 디렉토리로 이동
```bash
cd backend
```

### 2) 빌드
```bash
mvn clean install
```

### 3) 실행
```bash
mvn spring-boot:run
```

백엔드 서버: http://localhost:8080

## 프론트엔드 실행 (Frontend Setup)

### 1) 프론트엔드 디렉토리로 이동
```bash
cd frontend
```

### 2) 패키지 설치
```bash
npm install
```

### 3) 실행
```bash
npm start
```

프론트엔드: http://localhost:3000

## 전체 실행 순서

1) Oracle DB 실행
2) 터미널 1: backend 실행
3) 터미널 2: frontend 실행

## 트러블슈팅

### Oracle 연결 오류
- Oracle 서비스명(SERVICE_NAME) 또는 SID가 맞는지 확인
- 1521 포트가 열려 있는지 확인
- 사용자/비밀번호, 계정 잠금 여부 확인

### 프론트엔드에서 백엔드 연결 실패 (Network Error)
- 백엔드가 http://localhost:8080 에서 실행 중인지 확인
- CORS 설정(WebConfig.java) 확인

## 환경 변수 설정 (권장)

운영 환경에서는 자격 증명을 환경 변수로 분리하는 것이 좋습니다.

```bash
# Linux/Mac
export DB_URL="jdbc:oracle:thin:@//localhost:1521/XEPDB1"
export DB_USERNAME="ICTDB"
export DB_PASSWORD="ictdb"

# Windows PowerShell
$env:DB_URL="jdbc:oracle:thin:@//localhost:1521/XEPDB1"
$env:DB_USERNAME="ICTDB"
$env:DB_PASSWORD="ictdb"
```
