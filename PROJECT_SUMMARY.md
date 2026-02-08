# 프로젝트 요약 (Oracle 버전)

## 프로젝트 개요
ICT 5기 2팀 프로젝트 - React, Spring Boot, JDBC, Oracle Database를 사용한 풀스택 웹 애플리케이션

## 구현 완료 기능

### Backend (Spring Boot 3.2.1)
- REST API 엔드포인트: 사용자 CRUD 작업을 위한 5개 API
  - GET /api/users
  - GET /api/users/{id}
  - POST /api/users
  - PUT /api/users/{id}
  - DELETE /api/users/{id}

- 데이터 접근 계층: Spring JDBC와 JdbcTemplate 사용
  - PreparedStatement 기반으로 SQL Injection 방지

- 입력 검증: Bean Validation 사용
  - @NotBlank, @Email, @Size

- 에러 처리: 구체적인 예외 처리
  - EmptyResultDataAccessException → 404
  - DuplicateKeyException → 409
  - DataAccessException → 500

- 보안
  - 환경 변수를 통한 DB 자격 증명 관리
  - CORS 설정으로 프론트엔드 접근 제어

### Frontend (React 18.2.0)
- 컴포넌트 구조: App.js, UserForm.js, UserList.js
- 상태 관리: useState, useEffect
- API 통신: Axios 1.13.4
- UI/UX: 반응형 디자인, 로딩/에러 표시

### Database (Oracle)
- 스키마 설계
  - users 테이블 (id, name, email, created_at)
  - IDENTITY 기반 PK
  - UNIQUE 제약조건 (email)
- 샘플 데이터 3건 포함 (schema_oracle.sql)

## 문서화
- README_ORACLE.md
- SETUP_GUIDE_ORACLE.md
- ARCHITECTURE_ORACLE.md
- PROJECT_SUMMARY_ORACLE.md

## 기술 스택

### Backend
| 기술 | 버전 | 용도 |
|-----|------|-----|
| Spring Boot | 3.2.1 | 애플리케이션 프레임워크 |
| Spring JDBC | 3.2.1 | 데이터베이스 연동 |
| Spring Validation | 3.2.1 | 입력 검증 |
| Oracle JDBC Driver | (프로젝트 설정에 따름) | Oracle 드라이버 |
| Maven | 3.6+ | 빌드 도구 |
| JDK | 17 | 개발 플랫폼 |

### Frontend
| 기술 | 버전 | 용도 |
|-----|------|-----|
| React | 18.2.0 | UI 라이브러리 |
| Axios | 1.13.4 | HTTP 클라이언트 |
| React Scripts | 5.0.1 | 빌드 도구 |
| Node.js | 14+ | 런타임 |

### Database
| 기술 | 버전 | 용도 |
|-----|------|-----|
| Oracle Database | (XE 또는 상용) | 관계형 데이터베이스 |
