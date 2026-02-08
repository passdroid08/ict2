-- 실행 권한: SYSTEM(또는 사용자 생성 권한이 있는 계정)으로 실행 권장

-- 1) 사용자(스키마) 생성
-- 이미 존재하면 에러가 날 수 있으니, 필요 시 DROP USER ICTDB CASCADE 후 재실행하세요.
CREATE USER ICTDB IDENTIFIED BY ictdb;

-- 2) 기본 권한 부여 (개발용 최소)
GRANT CREATE SESSION TO ICTDB;
GRANT CREATE TABLE TO ICTDB;
GRANT CREATE SEQUENCE TO ICTDB;
GRANT CREATE VIEW TO ICTDB;
GRANT CREATE PROCEDURE TO ICTDB;

-- 3) ICTDB로 접속 후 객체 생성
-- SQL*Plus에서는 CONNECT로 전환 가능
CONNECT ICTDB/ictdb;