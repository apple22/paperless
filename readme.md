# Paperless Card Application Mini Project

Java 21 + Spring Boot 3 기반으로 만든 **태블릿 대면 카드신청 페이퍼리스 미니 프로젝트**입니다.

이 프로젝트는 현대카드 태블릿 대면 카드신청 현대화 업무를 가정하여, 카드 신청 생성부터 약관 동의, 전자서명, 최종 제출, 조회까지의 기본 흐름을 구현합니다.

---

## 1. 프로젝트 목표

이 프로젝트의 목적은 단순 CRUD가 아니라, 금융권 카드신청 업무에서 자주 등장하는 다음 개념을 실습하는 것입니다.

- Java 21 환경에서 Spring Boot 프로젝트 구성
- Controller → Service → Repository 계층 구조 이해
- DTO, record, enum, Entity, Repository 개념 학습
- 카드신청 상태 전이 구현
- 공통 응답 포맷 구현
- 전역 예외 처리 구현
- PowerShell을 통한 API 테스트
- JUnit 테스트 코드 작성
- MemoryRepository에서 H2 DB + JPA Repository로 전환
- H2 Console을 통해 실제 저장 데이터 확인

---

## 2. 기술 스택

```text
Java 21
Spring Boot 3.x
Gradle - Groovy
Spring Web
Spring Validation
Spring Data JPA
H2 Database
Lombok
JUnit 5
AssertJ
MockMvc
Jackson ObjectMapper
```

---

## 3. 프로젝트 구조

```text
com.example.paperless
 ├─ PaperlessApplication
 ├─ health
 │  └─ HealthCheckController
 ├─ common
 │  ├─ ApiResponse
 │  └─ GlobalExceptionHandler
 └─ card
    ├─ controller
    │  └─ CardApplicationController
    ├─ domain
    │  ├─ ApplicationStatus
    │  └─ CardApplication
    ├─ dto
    │  ├─ CardApplicationCreateRequest
    │  ├─ CardApplicationCreateResponse
    │  ├─ CardApplicationDetailResponse
    │  ├─ CardApplicationTermsAgreementResponse
    │  ├─ CardApplicationSignatureResponse
    │  └─ CardApplicationSubmitResponse
    ├─ repository
    │  ├─ CardApplicationMemoryRepository
    │  └─ CardApplicationJpaRepository
    └─ service
       └─ CardApplicationService
```

테스트 구조:

```text
src/test/java/com/example/paperless
 └─ card
    ├─ domain
    │  └─ CardApplicationTest
    ├─ service
    │  └─ CardApplicationServiceTest
    └─ controller
       └─ CardApplicationControllerTest
```

---

## 4. 전체 업무 흐름

카드 신청은 아래 상태 흐름을 따릅니다.

```text
TEMPORARY_SAVED
    ↓ 약관 동의
TERMS_AGREED
    ↓ 전자서명
SIGNED
    ↓ 최종 제출
SUBMITTED
```

각 상태의 의미:

```text
TEMPORARY_SAVED : 임시저장
TERMS_AGREED    : 약관동의완료
SIGNED          : 전자서명완료
SUBMITTED       : 최종제출
```

상태 전이 규칙:

```text
임시저장 상태에서만 약관 동의 가능
약관동의완료 상태에서만 전자서명 가능
전자서명완료 상태에서만 최종 제출 가능
```

잘못된 흐름은 예외 처리합니다.

```text
약관 동의 없이 전자서명 ❌
전자서명 없이 최종 제출 ❌
이미 약관 동의했는데 다시 약관 동의 ❌
```

---

## 5. API 목록

### 5.1 Health Check

```http
GET /health
```

서버 상태와 Java 버전을 확인합니다.

응답 예시:

```json
{
  "status": "UP",
  "application": "paperless-card-application",
  "javaVersion": "21.0.x",
  "serverTime": "2026-04-27T13:00:00"
}
```

---

### 5.2 카드 신청 생성

```http
POST /api/card-applications
```

요청:

```json
{
  "customerName": "홍길동",
  "phoneNumber": "010-1234-5678",
  "birthDate": "19900101",
  "cardProductCode": "HYUNDAI_CARD_M"
}
```

응답:

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "정상 처리되었습니다.",
  "data": {
    "applicationId": "UUID",
    "customerName": "홍길동",
    "phoneNumber": "010-1234-5678",
    "birthDate": "19900101",
    "cardProductCode": "HYUNDAI_CARD_M",
    "status": "TEMPORARY_SAVED",
    "statusDescription": "임시저장",
    "createdAt": "2026-04-27T13:00:00"
  }
}
```

---

### 5.3 카드 신청 조회

```http
GET /api/card-applications/{applicationId}
```

응답 예시:

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "정상 처리되었습니다.",
  "data": {
    "applicationId": "UUID",
    "customerName": "홍길동",
    "phoneNumber": "010-1234-5678",
    "birthDate": "19900101",
    "cardProductCode": "HYUNDAI_CARD_M",
    "status": "SUBMITTED",
    "statusDescription": "최종제출",
    "createdAt": "2026-04-27T13:00:00",
    "termsAgreedAt": "2026-04-27T13:01:00",
    "signedAt": "2026-04-27T13:02:00",
    "submittedAt": "2026-04-27T13:03:00"
  }
}
```

---

### 5.4 약관 동의

```http
POST /api/card-applications/{applicationId}/terms-agreement
```

상태 변경:

```text
TEMPORARY_SAVED → TERMS_AGREED
```

응답:

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "정상 처리되었습니다.",
  "data": {
    "applicationId": "UUID",
    "status": "TERMS_AGREED",
    "statusDescription": "약관동의완료",
    "termsAgreedAt": "2026-04-27T13:01:00"
  }
}
```

---

### 5.5 전자서명

```http
POST /api/card-applications/{applicationId}/signature
```

상태 변경:

```text
TERMS_AGREED → SIGNED
```

응답:

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "정상 처리되었습니다.",
  "data": {
    "applicationId": "UUID",
    "status": "SIGNED",
    "statusDescription": "전자서명완료",
    "signedAt": "2026-04-27T13:02:00"
  }
}
```

---

### 5.6 최종 제출

```http
POST /api/card-applications/{applicationId}/submit
```

상태 변경:

```text
SIGNED → SUBMITTED
```

응답:

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "정상 처리되었습니다.",
  "data": {
    "applicationId": "UUID",
    "status": "SUBMITTED",
    "statusDescription": "최종제출",
    "submittedAt": "2026-04-27T13:03:00"
  }
}
```

---

## 6. build.gradle 예시

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.4.5'
    id 'io.spring.dependency-management' version '1.1.7'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'
description = 'paperless'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'

    runtimeOnly 'com.h2database:h2'

    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'com.fasterxml.jackson.core:jackson-databind'

    testCompileOnly 'org.projectlombok:lombok'
    testAnnotationProcessor 'org.projectlombok:lombok'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

---

## 7. application.properties 예시

```properties
spring.application.name=paperless

server.servlet.encoding.charset=UTF-8
server.servlet.encoding.enabled=true
server.servlet.encoding.force=true

spring.datasource.url=jdbc:h2:mem:paperless;MODE=MySQL;DATABASE_TO_UPPER=false
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

---

## 8. H2 Database 확인 방법

### 8.1 서버 실행

IntelliJ에서 `PaperlessApplication` 실행.

성공 로그:

```text
Tomcat started on port 8080
Started PaperlessApplication
```

JPA 테이블 생성 로그 예시:

```sql
create table card_application (
    application_id varchar(36) not null,
    birth_date varchar(8) not null,
    card_product_code varchar(50) not null,
    created_at timestamp(6) not null,
    customer_name varchar(100) not null,
    phone_number varchar(20) not null,
    signed_at timestamp(6),
    status varchar(30) not null,
    submitted_at timestamp(6),
    terms_agreed_at timestamp(6),
    primary key (application_id)
)
```

### 8.2 H2 Console 접속

브라우저에서 접속:

```text
http://localhost:8080/h2-console
```

입력값:

```text
Driver Class: org.h2.Driver
JDBC URL: jdbc:h2:mem:paperless;MODE=MySQL;DATABASE_TO_UPPER=false
User Name: sa
Password:
```

Password는 비워둡니다.

### 8.3 데이터 조회

H2 Console에서 실행:

```sql
select * from card_application;
```

서버 재시작 시 데이터가 사라집니다. 현재 설정이 메모리 DB이기 때문입니다.

```properties
spring.datasource.url=jdbc:h2:mem:paperless
```

---

## 9. PowerShell API 테스트

### 9.1 UTF-8 설정

```powershell
chcp 65001
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
```

### 9.2 전체 정상 흐름 테스트

```powershell
$body = @{
    customerName = "홍길동"
    phoneNumber = "010-1234-5678"
    birthDate = "19900101"
    cardProductCode = "HYUNDAI_CARD_M"
} | ConvertTo-Json

$utf8Body = [System.Text.Encoding]::UTF8.GetBytes($body)

$createResponse = Invoke-RestMethod `
    -Uri "http://localhost:8080/api/card-applications" `
    -Method Post `
    -ContentType "application/json; charset=utf-8" `
    -Body $utf8Body

$applicationId = $createResponse.data.applicationId

$termsResponse = Invoke-RestMethod `
    -Uri "http://localhost:8080/api/card-applications/$applicationId/terms-agreement" `
    -Method Post

$signatureResponse = Invoke-RestMethod `
    -Uri "http://localhost:8080/api/card-applications/$applicationId/signature" `
    -Method Post

$submitResponse = Invoke-RestMethod `
    -Uri "http://localhost:8080/api/card-applications/$applicationId/submit" `
    -Method Post

$detailResponse = Invoke-RestMethod `
    -Uri "http://localhost:8080/api/card-applications/$applicationId" `
    -Method Get

$detailResponse | ConvertTo-Json -Depth 5
```

최종 상태가 아래처럼 나오면 성공입니다.

```json
{
  "status": "SUBMITTED",
  "statusDescription": "최종제출"
}
```

---

## 10. 공통 응답 ApiResponse

모든 API 응답은 아래 형식으로 통일합니다.

```java
public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                true,
                "SUCCESS",
                "정상 처리되었습니다.",
                data
        );
    }

    public static <T> ApiResponse<T> fail(String code, String message) {
        return new ApiResponse<>(
                false,
                code,
                message,
                null
        );
    }
}
```

성공 응답:

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "정상 처리되었습니다.",
  "data": {}
}
```

실패 응답:

```json
{
  "success": false,
  "code": "INVALID_STATE",
  "message": "최종 제출은 전자서명완료 상태에서만 가능합니다.",
  "data": null
}
```

---

## 11. 전역 예외 처리

`GlobalExceptionHandler`는 Controller에서 발생한 예외를 공통 응답으로 변환합니다.

처리하는 주요 예외:

```text
MethodArgumentNotValidException → VALIDATION_ERROR
IllegalArgumentException        → BAD_REQUEST
IllegalStateException           → INVALID_STATE
Exception                       → SYSTEM_ERROR
```

---

## 12. 핵심 Java/Spring 용어

```text
class
객체를 만들기 위한 설계도

object
class로 실제 만들어진 것

field
객체가 가진 데이터

method
객체가 하는 행동

constructor
객체 생성 시 초기값을 넣는 코드

DTO
요청/응답 데이터를 담는 객체

record
DTO를 간단하게 만들 수 있는 Java 문법

enum
정해진 값만 사용하게 하는 타입

Controller
HTTP 요청을 받는 입구

Service
업무 로직을 처리하는 계층

Repository
데이터 저장/조회 계층

Entity
DB 테이블과 매핑되는 Java 클래스

JPA
Java 객체와 DB 테이블을 연결해주는 기술

JpaRepository
기본 CRUD 기능을 자동 제공하는 Spring Data JPA 인터페이스

Bean
Spring이 생성하고 관리하는 객체

DI
Spring이 필요한 객체를 넣어주는 것

Validation
요청값이 올바른지 검사하는 것

Exception
프로그램 실행 중 발생한 문제

State Transition
업무 상태가 단계별로 바뀌는 것

@Transactional
DB 작업을 하나의 작업 단위로 묶는 어노테이션
```

---

## 13. 테스트 코드

현재 작성한 테스트 종류:

```text
CardApplicationTest
→ 도메인 상태 전이 테스트

CardApplicationServiceTest
→ Service + Repository 흐름 테스트

CardApplicationControllerTest
→ MockMvc 기반 API 테스트
```

### 13.1 테스트 실행

PowerShell에서 프로젝트 루트 기준:

```powershell
.\gradlew.bat clean test
```

특정 Controller 테스트만 실행:

```powershell
.\gradlew.bat test --tests "com.example.paperless.card.controller.CardApplicationControllerTest"
```

특정 메서드만 실행:

```powershell
.\gradlew.bat test --tests "com.example.paperless.card.controller.CardApplicationControllerTest.create_success"
```

---

## 14. 테스트 파일 위치 주의

테스트 파일은 반드시 `src/test/java` 아래에 있어야 합니다.

정상:

```text
src/test/java/com/example/paperless/card/controller/CardApplicationControllerTest.java
```

잘못된 위치:

```text
src/main/java/com/example/paperless/card/controller/CardApplicationControllerTest.java
```

`src/main/java` 아래에 테스트 파일을 두면 JUnit import 오류가 발생할 수 있습니다.

예:

```text
package org.junit.jupiter.api does not exist
```

---

## 15. 현재까지 학습한 실무 흐름

```text
1. Spring Boot 프로젝트 생성
2. JDK 21 설정
3. Health Check API 작성
4. 카드 신청 생성 API 작성
5. DTO / record 사용
6. Validation 적용
7. Controller → Service 분리
8. 공통 응답 ApiResponse 적용
9. GlobalExceptionHandler 적용
10. enum으로 상태값 관리
11. MemoryRepository로 저장/조회 구현
12. 약관 동의 API 구현
13. 전자서명 API 구현
14. 최종 제출 API 구현
15. PowerShell로 API 테스트
16. 도메인 테스트 작성
17. Service 테스트 작성
18. Controller 테스트 작성
19. H2 DB + JPA 전환
20. H2 Console에서 DB 데이터 확인
```

---

## 16. 다음 추천 단계

다음 단계는 아래 순서로 진행하면 좋습니다.

```text
1. JPA Repository 기반 Service 테스트 수정
2. Controller 테스트를 H2/JPA 기준으로 정리
3. 개인정보 마스킹 적용
4. 로그 추가
5. MyBatis 방식으로 동일 기능 구현 연습
6. JDK 21 업그레이드 체크리스트 학습
7. Spring Boot 2 → 3 / javax → jakarta 마이그레이션 학습
```

금융권 SI 대비라면 특히 아래를 추가로 공부하는 것이 좋습니다.

```text
개인정보 로그 마스킹
전자문서/전자서명 증적 관리
상태 이력 테이블
DB 트랜잭션
예외 코드 체계
운영 배포 전 테스트 증적 작성
```

---

## 17. 한 줄 요약

이 프로젝트는 Java 21 + Spring Boot 3 기반으로 카드 신청 페이퍼리스 업무를 구현하며, Controller-Service-Repository 구조, 상태 전이, 공통 응답, 예외 처리, 테스트 코드, H2/JPA 저장소까지 학습하는 미니 실무 프로젝트입니다.
