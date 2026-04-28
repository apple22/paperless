# Paperless Card Application API

Java 21 + Spring Boot 3 기반의 **태블릿 대면 카드신청 페이퍼리스 미니 프로젝트**입니다.

이 프로젝트는 금융권 카드신청 업무를 가정하여, 고객이 태블릿에서 카드 신청을 진행하는 흐름을 REST API로 구현한 학습용 프로젝트입니다.

현재는 DB 대신 **메모리 저장소**를 사용하며, 다음 흐름을 구현했습니다.

```text
카드 신청 생성
→ 약관 동의
→ 전자서명
→ 최종 제출
→ 신청 조회
```

---

## 1. 프로젝트 목표

이 프로젝트의 목적은 단순 CRUD가 아니라, 실무에서 자주 쓰이는 Spring Boot 구조와 금융권 신청 업무 흐름을 익히는 것입니다.

학습 목표는 다음과 같습니다.

```text
Java 21 기본 문법 익히기
Spring Boot 3 프로젝트 구조 이해
Controller → Service → Repository 계층 구조 이해
DTO, record, enum, class, method, constructor 개념 이해
상태 전이 로직 구현
공통 응답 포맷 구현
전역 예외 처리 구현
메모리 저장소 구현
PowerShell로 API 테스트
JUnit 기반 단위 테스트 작성
```

---

## 2. 기술 스택

```text
Java 21
Spring Boot 3.x
Gradle - Groovy
Spring Web
Spring Validation
Lombok
JUnit 5
AssertJ
Memory Repository
```

---

## 3. 현재 프로젝트 구조

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
    │  └─ CardApplicationMemoryRepository
    └─ service
       └─ CardApplicationService
```

테스트 구조:

```text
src/test/java/com/example/paperless
 └─ card
    ├─ domain
    │  └─ CardApplicationTest
    └─ service
       └─ CardApplicationServiceTest
```

---

## 4. 구현된 API

### 4.1 Health Check API

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
  "serverTime": "2026-04-27T13:49:37.3486671"
}
```

---

### 4.2 카드 신청 생성 API

```http
POST /api/card-applications
```

카드 신청 정보를 생성하고 메모리에 저장합니다.

요청 예시:

```json
{
  "customerName": "홍길동",
  "phoneNumber": "010-1234-5678",
  "birthDate": "19900101",
  "cardProductCode": "HYUNDAI_CARD_M"
}
```

응답 예시:

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "정상 처리되었습니다.",
  "data": {
    "applicationId": "858e071f-2265-47b8-9c19-d769c4949bea",
    "customerName": "홍길동",
    "phoneNumber": "010-1234-5678",
    "birthDate": "19900101",
    "cardProductCode": "HYUNDAI_CARD_M",
    "status": "TEMPORARY_SAVED",
    "statusDescription": "임시저장",
    "createdAt": "2026-04-27T13:49:37.3486671"
  }
}
```

---

### 4.3 카드 신청 조회 API

```http
GET /api/card-applications/{applicationId}
```

신청 ID로 카드 신청 정보를 조회합니다.

응답 예시:

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "정상 처리되었습니다.",
  "data": {
    "applicationId": "858e071f-2265-47b8-9c19-d769c4949bea",
    "customerName": "홍길동",
    "phoneNumber": "010-1234-5678",
    "birthDate": "19900101",
    "cardProductCode": "HYUNDAI_CARD_M",
    "status": "SUBMITTED",
    "statusDescription": "최종제출",
    "createdAt": "2026-04-27T13:49:37.3486671",
    "termsAgreedAt": "2026-04-27T13:50:10.123",
    "signedAt": "2026-04-27T13:51:20.123",
    "submittedAt": "2026-04-27T13:52:30.123"
  }
}
```

---

### 4.4 약관 동의 API

```http
POST /api/card-applications/{applicationId}/terms-agreement
```

카드 신청 상태를 `TEMPORARY_SAVED`에서 `TERMS_AGREED`로 변경합니다.

응답 예시:

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "정상 처리되었습니다.",
  "data": {
    "applicationId": "858e071f-2265-47b8-9c19-d769c4949bea",
    "status": "TERMS_AGREED",
    "statusDescription": "약관동의완료",
    "termsAgreedAt": "2026-04-27T13:50:10.123"
  }
}
```

---

### 4.5 전자서명 API

```http
POST /api/card-applications/{applicationId}/signature
```

카드 신청 상태를 `TERMS_AGREED`에서 `SIGNED`로 변경합니다.

응답 예시:

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "정상 처리되었습니다.",
  "data": {
    "applicationId": "858e071f-2265-47b8-9c19-d769c4949bea",
    "status": "SIGNED",
    "statusDescription": "전자서명완료",
    "signedAt": "2026-04-27T13:51:20.123"
  }
}
```

---

### 4.6 최종 제출 API

```http
POST /api/card-applications/{applicationId}/submit
```

카드 신청 상태를 `SIGNED`에서 `SUBMITTED`로 변경합니다.

응답 예시:

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "정상 처리되었습니다.",
  "data": {
    "applicationId": "858e071f-2265-47b8-9c19-d769c4949bea",
    "status": "SUBMITTED",
    "statusDescription": "최종제출",
    "submittedAt": "2026-04-27T13:52:30.123"
  }
}
```

---

## 5. 상태 흐름

현재 카드 신청 상태 흐름은 다음과 같습니다.

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

| 상태 코드 | 설명 |
|---|---|
| `TEMPORARY_SAVED` | 임시저장 |
| `TERMS_AGREED` | 약관동의완료 |
| `SIGNED` | 전자서명완료 |
| `SUBMITTED` | 최종제출 |
| `UNDER_REVIEW` | 심사중 |
| `APPROVED` | 승인 |
| `REJECTED` | 거절 |
| `CANCELED` | 취소 |

---

## 6. 상태 전이 규칙

업무 흐름상 상태는 아무 순서로나 변경할 수 없습니다.

가능한 상태 전이:

```text
TEMPORARY_SAVED → TERMS_AGREED
TERMS_AGREED    → SIGNED
SIGNED          → SUBMITTED
```

불가능한 상태 전이:

```text
TEMPORARY_SAVED → SIGNED
TEMPORARY_SAVED → SUBMITTED
TERMS_AGREED    → SUBMITTED
TERMS_AGREED    → TERMS_AGREED
SIGNED          → SIGNED
SUBMITTED       → TERMS_AGREED
```

이 규칙은 `CardApplication` 도메인 객체 내부에서 검증합니다.

예시:

```java
public void submit(LocalDateTime submittedAt) {
    if (this.status != ApplicationStatus.SIGNED) {
        throw new IllegalStateException(
                "최종 제출은 전자서명완료 상태에서만 가능합니다. currentStatus=" + this.status.getCode()
        );
    }

    this.status = ApplicationStatus.SUBMITTED;
    this.submittedAt = submittedAt;
}
```

---

## 7. 공통 응답 포맷

모든 API 응답은 `ApiResponse<T>` 형태로 통일했습니다.

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

## 8. 예외 처리

전역 예외 처리는 `GlobalExceptionHandler`에서 담당합니다.

처리하는 예외:

| 예외 | 응답 코드 | 설명 |
|---|---|---|
| `MethodArgumentNotValidException` | `VALIDATION_ERROR` | 요청값 검증 실패 |
| `IllegalArgumentException` | `BAD_REQUEST` | 잘못된 요청 또는 존재하지 않는 신청 ID |
| `IllegalStateException` | `INVALID_STATE` | 잘못된 상태 전이 |
| `Exception` | `SYSTEM_ERROR` | 알 수 없는 서버 오류 |

예시 응답:

```json
{
  "success": false,
  "code": "INVALID_STATE",
  "message": "전자서명은 약관동의완료 상태에서만 가능합니다. currentStatus=TEMPORARY_SAVED",
  "data": null
}
```

---

## 9. 주요 Java / Spring 개념 정리

### class

객체를 만들기 위한 설계도입니다.

```java
public class CardApplication {
}
```

---

### object

클래스를 이용해 실제로 만들어진 객체입니다.

```java
CardApplication cardApplication = new CardApplication(...);
```

---

### field

객체가 가지고 있는 데이터입니다.

```java
private final String applicationId;
private ApplicationStatus status;
```

---

### method

객체가 수행하는 행동입니다.

```java
public void agreeTerms(LocalDateTime agreedAt) {
}
```

---

### constructor

객체를 만들 때 초기값을 넣어주는 특별한 메서드입니다.

```java
public CardApplication(...) {
}
```

---

### this

현재 객체 자신을 의미합니다.

```java
this.applicationId = applicationId;
```

---

### final

한 번 값이 정해지면 바꿀 수 없다는 뜻입니다.

```java
private final String applicationId;
```

---

### enum

정해진 값만 사용할 수 있게 하는 타입입니다.

```java
ApplicationStatus.TEMPORARY_SAVED
```

---

### record

DTO를 간단하게 만들 수 있는 Java 문법입니다.

```java
public record CardApplicationCreateRequest(
        String customerName,
        String phoneNumber
) {
}
```

record 값은 메서드처럼 접근합니다.

```java
request.customerName()
```

---

### DTO

요청/응답 데이터를 담는 객체입니다.

```text
Request DTO  = 클라이언트가 서버로 보내는 데이터
Response DTO = 서버가 클라이언트로 반환하는 데이터
```

---

### Controller

HTTP 요청을 받는 입구입니다.

```java
@RestController
@RequestMapping("/api/card-applications")
public class CardApplicationController {
}
```

---

### Service

비즈니스 로직을 처리하는 계층입니다.

```java
@Service
public class CardApplicationService {
}
```

---

### Repository

데이터 저장과 조회를 담당하는 계층입니다.

```java
@Repository
public class CardApplicationMemoryRepository {
}
```

---

### Bean

Spring이 생성하고 관리하는 객체입니다.

`@RestController`, `@Service`, `@Repository`가 붙은 클래스는 Bean으로 등록됩니다.

---

### DI

Dependency Injection의 약자입니다.

필요한 객체를 직접 만들지 않고 Spring이 넣어주는 방식입니다.

```java
private final CardApplicationService cardApplicationService;

public CardApplicationController(CardApplicationService cardApplicationService) {
    this.cardApplicationService = cardApplicationService;
}
```

---

### Validation

요청값이 올바른지 검사하는 기능입니다.

```java
@NotBlank(message = "고객명은 필수입니다.")
String customerName
```

---

### Exception

프로그램 실행 중 발생한 문제입니다.

```java
throw new IllegalStateException("최종 제출은 전자서명완료 상태에서만 가능합니다.");
```

---

## 10. PowerShell API 테스트

### 10.1 UTF-8 설정

PowerShell에서 한글이 깨지는 경우 아래 설정을 먼저 실행합니다.

```powershell
chcp 65001
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
```

---

### 10.2 카드 신청 생성

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

$createResponse | ConvertTo-Json -Depth 5
```

---

### 10.3 약관 동의

```powershell
$termsResponse = Invoke-RestMethod `
    -Uri "http://localhost:8080/api/card-applications/$applicationId/terms-agreement" `
    -Method Post

$termsResponse | ConvertTo-Json -Depth 5
```

---

### 10.4 전자서명

```powershell
$signatureResponse = Invoke-RestMethod `
    -Uri "http://localhost:8080/api/card-applications/$applicationId/signature" `
    -Method Post

$signatureResponse | ConvertTo-Json -Depth 5
```

---

### 10.5 최종 제출

```powershell
$submitResponse = Invoke-RestMethod `
    -Uri "http://localhost:8080/api/card-applications/$applicationId/submit" `
    -Method Post

$submitResponse | ConvertTo-Json -Depth 5
```

---

### 10.6 최종 조회

```powershell
$detailResponse = Invoke-RestMethod `
    -Uri "http://localhost:8080/api/card-applications/$applicationId" `
    -Method Get

$detailResponse | ConvertTo-Json -Depth 5
```

최종 상태가 다음과 같으면 정상입니다.

```text
SUBMITTED
최종제출
```

---

## 11. 테스트 코드

현재 작성한 테스트는 두 종류입니다.

```text
CardApplicationTest
→ 도메인 객체 자체의 상태 전이 규칙 테스트

CardApplicationServiceTest
→ Service가 Repository와 도메인을 연결해서 업무 흐름을 처리하는지 테스트
```

---

### 11.1 도메인 테스트

테스트 대상:

```text
CardApplication
```

검증 내용:

```text
임시저장 상태에서 약관 동의 가능
약관 동의 없이 전자서명 불가
약관동의완료 상태에서 전자서명 가능
전자서명 없이 최종 제출 불가
전자서명완료 상태에서 최종 제출 가능
이미 약관동의완료 상태에서 다시 약관 동의 불가
```

---

### 11.2 Service 테스트

테스트 대상:

```text
CardApplicationService
```

검증 내용:

```text
카드 신청 생성 시 TEMPORARY_SAVED 상태로 저장
생성한 신청을 applicationId로 조회 가능
없는 applicationId 조회 시 예외 발생
약관 동의 시 TERMS_AGREED 상태로 변경
약관 동의 없이 전자서명 시 예외 발생
약관 동의 후 전자서명 시 SIGNED 상태로 변경
전자서명 없이 최종 제출 시 예외 발생
전자서명 후 최종 제출 시 SUBMITTED 상태로 변경
```

---

### 11.3 테스트 실행

IntelliJ에서 테스트 클래스 왼쪽의 실행 버튼을 클릭하거나, 프로젝트 루트에서 아래 명령어를 실행합니다.

```powershell
.\gradlew.bat test
```

성공 시:

```text
BUILD SUCCESSFUL
```

---

## 12. 현재까지 완성한 기능 체크리스트

```text
[x] Java 21 + Spring Boot 3 프로젝트 생성
[x] Spring Web, Validation, Lombok 추가
[x] /health API 구현
[x] 카드 신청 생성 API 구현
[x] 카드 신청 조회 API 구현
[x] 약관 동의 API 구현
[x] 전자서명 API 구현
[x] 최종 제출 API 구현
[x] 상태 enum 구현
[x] 도메인 객체 구현
[x] 메모리 저장소 구현
[x] 공통 응답 ApiResponse 구현
[x] 전역 예외 처리 구현
[x] PowerShell API 테스트
[x] 도메인 테스트 작성
[x] Service 테스트 작성
```

---

## 13. 다음 단계 추천

이 프로젝트의 다음 학습 단계는 다음 중 하나입니다.

### 13.1 Controller 테스트

`MockMvc`를 사용해서 실제 HTTP 요청처럼 Controller를 테스트합니다.

테스트할 API:

```text
POST /api/card-applications
GET /api/card-applications/{applicationId}
POST /api/card-applications/{applicationId}/terms-agreement
POST /api/card-applications/{applicationId}/signature
POST /api/card-applications/{applicationId}/submit
```

---

### 13.2 H2 DB + JPA 전환

현재는 메모리에 저장하고 있으므로 서버를 재시작하면 데이터가 사라집니다.

다음 단계에서는 H2 DB와 JPA를 붙여서 실제 DB 저장 구조를 학습할 수 있습니다.

예상 추가 구조:

```text
CardApplicationEntity
CardApplicationJpaRepository
CardApplicationRepository interface
```

---

### 13.3 개인정보 마스킹 로그

금융권 프로젝트에서는 개인정보 로그 처리가 중요합니다.

추가할 수 있는 기능:

```text
고객명 마스킹
전화번호 마스킹
신청 ID 일부 마스킹
request 전체 로그 금지
```

---

### 13.4 신청 취소 API

추가 API:

```http
POST /api/card-applications/{applicationId}/cancel
```

상태 흐름 예시:

```text
TEMPORARY_SAVED → CANCELED
TERMS_AGREED    → CANCELED
SIGNED          → CANCELED
SUBMITTED 이후 취소 불가
```

---

## 14. 실행 방법

### 14.1 서버 실행

IntelliJ에서 `PaperlessApplication` 실행 또는 터미널에서 실행합니다.

Windows:

```powershell
.\gradlew.bat bootRun
```

macOS/Linux:

```bash
./gradlew bootRun
```

---

### 14.2 테스트 실행

Windows:

```powershell
.\gradlew.bat test
```

macOS/Linux:

```bash
./gradlew test
```

---

## 15. 메모리 저장소 주의사항

현재 저장소는 `ConcurrentHashMap` 기반의 메모리 저장소입니다.

```java
private final Map<String, CardApplication> store = new ConcurrentHashMap<>();
```

특징:

```text
서버 실행 중에만 데이터 유지
서버 재시작 시 모든 데이터 삭제
학습용으로 적합
실무에서는 DB 저장소로 교체 필요
```

---

## 16. 프로젝트 한 줄 요약

이 프로젝트는 Java 21과 Spring Boot 3를 사용해 카드신청 페이퍼리스 업무 흐름을 구현한 미니 프로젝트입니다.

현재는 카드 신청 생성, 약관 동의, 전자서명, 최종 제출, 신청 조회, 상태 전이 검증, 공통 응답, 전역 예외 처리, 메모리 저장소, 단위 테스트까지 구현했습니다.
