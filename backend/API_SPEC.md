# API 명세서

작성 기준: 현재 구현된 컨트롤러 4개 (`AuthController`, `RoutineController`, `AppointmentController`, `RouteController`)

## 공통 사항

### 인증 방식
- 세션 기반 인증 (`HttpSession`)
- 로그인 성공 시 서버가 세션을 생성하고 `JSESSIONID` 쿠키를 내려준다.
- 이후 요청은 브라우저/클라이언트가 `JSESSIONID` 쿠키를 자동으로 포함해야 한다.
- 인증이 필요한 API에서 세션이 없거나 로그인 정보가 없으면 `401 Unauthorized` (`로그인이 필요합니다.`)가 반환된다.

### Base URL
```
/api
```

### 인증 필요 여부 요약

| API | 인증 필요 |
|---|---|
| 회원가입 | ✗ |
| 로그인 | ✗ |
| 로그아웃 | ✗ (세션 있으면 무효화) |
| 루틴 관련 전체 | ✓ |
| 약속 관련 전체 | ✓ |
| 이동시간 계산 | ✓ |

---

## 1. Auth API (`/api/auth`)

인증(회원가입/로그인/로그아웃)을 담당한다.

### 1-1. 회원가입

```
POST /api/auth/join
```

**설명**: 이메일/비밀번호로 신규 계정을 생성한다.

**요청 Body**

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| email | string | ✓ | 이메일 형식이어야 함 |
| password | string | ✓ | 최소 8자 이상 |

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**응답**: `201 Created`

| 필드 | 타입 | 설명 |
|---|---|---|
| id | number | 생성된 사용자 ID |
| email | string | 가입한 이메일 |

```json
{
  "id": 1,
  "email": "user@example.com"
}
```

### 1-2. 로그인

```
POST /api/auth/login
```

**설명**: 이메일/비밀번호로 로그인하고 세션을 발급한다. 응답 헤더의 `Set-Cookie: JSESSIONID=...`를 이후 요청에 유지해야 한다.

**요청 Body**

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| email | string | ✓ | 이메일 형식이어야 함 |
| password | string | ✓ | 비어있지 않아야 함 |

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**응답**: `200 OK`

| 필드 | 타입 | 설명 |
|---|---|---|
| id | number | 사용자 ID |
| email | string | 로그인한 이메일 |

```json
{
  "id": 1,
  "email": "user@example.com"
}
```

---

### 1-3. 로그아웃

```
POST /api/auth/logout
```

**설명**: 현재 세션을 무효화한다. 세션이 없어도 에러 없이 처리된다.

**요청 Body**: 없음

**응답**: `204 No Content` (본문 없음)

---

## 2. Routine API (`/api/routines`)

로그인한 사용자의 "준비 루틴"(단계 + 체크리스트)을 관리한다. **모든 API는 로그인 필요.**

### 2-1. 루틴 생성

```
POST /api/routines
```

**설명**: 이름, 준비 단계(steps), 체크리스트(checklist)로 구성된 루틴을 생성한다.

**요청 Body**

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| name | string | ✓ | 루틴 이름 |
| steps | array\<StepRequest\> | | 준비 단계 목록 |
| steps[].name | string | ✓ | 단계 이름 |
| steps[].durationMinutes | int | | 소요 시간(분) |
| steps[].stepOrder | int | | 정렬 순서 |
| checklist | array\<string\> | | 체크리스트 항목 이름 목록 |

```json
{
  "name": "출근 준비",
  "steps": [
    { "name": "샤워", "durationMinutes": 15, "stepOrder": 1 },
    { "name": "옷 입기", "durationMinutes": 10, "stepOrder": 2 }
  ],
  "checklist": ["지갑", "휴대폰", "사원증"]
}
```

**응답**: `201 Created`

| 필드 | 타입 | 설명 |
|---|---|---|
| id | number | 루틴 ID |
| name | string | 루틴 이름 |
| totalDurationMinutes | int | steps 소요시간 합계 |
| steps | array\<StepResponse\> | 단계 목록 |
| steps[].id | number | 단계 항목 ID |
| steps[].name | string | 단계 이름 |
| steps[].durationMinutes | int | 소요 시간(분) |
| steps[].itemOrder | int | 정렬 순서 |
| checklist | array\<ChecklistResponse\> | 체크리스트 목록 |
| checklist[].id | number | 체크리스트 항목 ID |
| checklist[].name | string | 항목 이름 |
| checklist[].itemOrder | int | 정렬 순서 |
| createdAt | string(datetime) | 생성 시각 |

```json
{
  "id": 1,
  "name": "출근 준비",
  "totalDurationMinutes": 25,
  "steps": [
    { "id": 10, "name": "샤워", "durationMinutes": 15, "itemOrder": 1 },
    { "id": 11, "name": "옷 입기", "durationMinutes": 10, "itemOrder": 2 }
  ],
  "checklist": [
    { "id": 20, "name": "지갑", "itemOrder": 1 },
    { "id": 21, "name": "휴대폰", "itemOrder": 2 },
    { "id": 22, "name": "사원증", "itemOrder": 3 }
  ],
  "createdAt": "2026-07-10T09:00:00"
}
```

---

### 2-2. 루틴 목록 조회

```
GET /api/routines
```

**설명**: 로그인한 사용자가 보유한 모든 루틴을 조회한다.

**요청**: 없음 (인증 세션만 필요)

**응답**: `200 OK` — `RoutineResponse` 객체의 배열 (구조는 2-1 응답과 동일)

---

### 2-3. 루틴 단건 조회

```
GET /api/routines/{routineId}
```

**설명**: 특정 루틴 하나를 상세 조회한다.

**Path 변수**

| 변수 | 타입 | 설명 |
|---|---|---|
| routineId | number | 조회할 루틴 ID |

**응답**: `200 OK` — `RoutineResponse` (구조는 2-1 응답과 동일)

---

### 2-4. 루틴 수정

```
PUT /api/routines/{routineId}
```

**설명**: 루틴의 이름/단계/체크리스트를 통째로 교체한다 (요청 형식은 생성과 동일).

**Path 변수**

| 변수 | 타입 | 설명 |
|---|---|---|
| routineId | number | 수정할 루틴 ID |

**요청 Body**: 2-1과 동일한 `RoutineCreateRequest`

**응답**: `200 OK` — `RoutineResponse` (구조는 2-1 응답과 동일)

---

### 2-5. 루틴 삭제

```
DELETE /api/routines/{routineId}
```

**설명**: 루틴을 삭제한다.

**Path 변수**

| 변수 | 타입 | 설명 |
|---|---|---|
| routineId | number | 삭제할 루틴 ID |

**응답**: `204 No Content` (본문 없음)

---

## 3. Appointment API (`/api/appointments`)

약속(외출 일정)과 그에 딸린 준비 단계/체크리스트를 관리한다. **모든 API는 로그인 필요.**

### 3-1. 약속 생성

```
POST /api/appointments
```

**설명**: 약속을 생성한다. `routineId`를 지정하면 해당 루틴의 단계/체크리스트를 가져와 약속에 복사하고, 도착 시각(`arrivalTime`)을 기준으로 이동 시간(`travelMinutes`)과 여유 시간(`bufferMinutes`)을 역산해 준비 시작 시각(`preparationStartTime`)과 출발 시각(`departureTime`)을 계산한다.

**요청 Body**

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| routineId | number | | 참조할 루틴 ID (없으면 단계/체크리스트 없이 생성) |
| name | string | ✓ | 약속 이름 |
| arrivalTime | string(datetime) | ✓ | 도착해야 하는 시각 |
| travelMinutes | int | ✓ | 이동 소요 시간(분), 0 이상 |
| bufferMinutes | int | ✓ | 여유 시간(분), 0 이상 |

```json
{
  "routineId": 1,
  "name": "친구와 저녁 약속",
  "arrivalTime": "2026-07-10T19:00:00",
  "travelMinutes": 30,
  "bufferMinutes": 10
}
```

**응답**: `201 Created`

| 필드 | 타입 | 설명 |
|---|---|---|
| id | number | 약속 ID |
| name | string | 약속 이름 |
| preparationStartTime | string(datetime) | 준비 시작 시각 |
| departureTime | string(datetime) | 출발 시각 |
| arrivalTime | string(datetime) | 도착 시각 |
| steps | array\<AppointmentStepResponse\> | 준비 단계 목록 |
| steps[].id | number | 단계 항목 ID |
| steps[].name | string | 단계 이름 |
| steps[].durationMinutes | int | 소요 시간(분) |
| steps[].itemOrder | int | 정렬 순서 |
| steps[].startTime | string(datetime) | 해당 단계 시작 시각 |
| steps[].endTime | string(datetime) | 해당 단계 종료 시각 |
| steps[].completed | boolean | 완료 여부 |
| checklist | array\<AppointmentChecklistResponse\> | 체크리스트 목록 |
| checklist[].id | number | 체크리스트 항목 ID |
| checklist[].name | string | 항목 이름 |
| checklist[].itemOrder | int | 정렬 순서 |
| checklist[].completed | boolean | 완료 여부 |

```json
{
  "id": 5,
  "name": "친구와 저녁 약속",
  "preparationStartTime": "2026-07-10T18:05:00",
  "departureTime": "2026-07-10T18:30:00",
  "arrivalTime": "2026-07-10T19:00:00",
  "steps": [
    {
      "id": 30,
      "name": "샤워",
      "durationMinutes": 15,
      "itemOrder": 1,
      "startTime": "2026-07-10T18:05:00",
      "endTime": "2026-07-10T18:20:00",
      "completed": false
    }
  ],
  "checklist": [
    { "id": 40, "name": "지갑", "itemOrder": 1, "completed": false }
  ]
}
```

---

### 3-2. 약속 단건 조회

```
GET /api/appointments/{appointmentId}
```

**설명**: 특정 약속 하나를 상세 조회한다.

**Path 변수**

| 변수 | 타입 | 설명 |
|---|---|---|
| appointmentId | number | 조회할 약속 ID |

**응답**: `200 OK` — `AppointmentResponse` (구조는 3-1 응답과 동일)

---

### 3-3. 약속 준비 항목 완료 처리

```
PATCH /api/appointments/{appointmentId}/items/{itemId}/complete
```

**설명**: 약속에 속한 준비 단계 또는 체크리스트 항목 하나를 완료 상태로 표시한다.

**Path 변수**

| 변수 | 타입 | 설명 |
|---|---|---|
| appointmentId | number | 약속 ID |
| itemId | number | 완료 처리할 항목(단계 또는 체크리스트) ID |

**요청 Body**: 없음

**응답**: `200 OK` (본문 없음)

---

## 4. Route API (`/api/routes`)

Google Maps Platform Routes API의 Compute Routes를 이용해 현재 위치와 도착지 사이의 자동차 기준 예상 이동시간과 거리를 계산한다. **모든 API는 로그인 필요.**

실제 Google API 키는 저장소에 커밋하지 않고 환경변수 `GOOGLE_MAPS_API_KEY`에 설정한다. 예시는 `.env.example`과 `ROUTES_API_DESIGN.md`를 참고한다.

### 4-1. 이동시간 계산

```
POST /api/routes/eta
```

**설명**: 안드로이드 앱이 현재 위치와 도착지 좌표를 보내면, 백엔드가 Google Routes API를 호출해 실시간 교통을 반영한 예상 이동시간을 반환한다.

**요청 Body**

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| origin | object | ✓ | 출발지 좌표 |
| origin.latitude | number | ✓ | 위도, -90 이상 90 이하 |
| origin.longitude | number | ✓ | 경도, -180 이상 180 이하 |
| destination | object | ✓ | 도착지 좌표 |
| destination.latitude | number | ✓ | 위도, -90 이상 90 이하 |
| destination.longitude | number | ✓ | 경도, -180 이상 180 이하 |

```json
{
  "origin": {
    "latitude": 37.5665,
    "longitude": 126.9780
  },
  "destination": {
    "latitude": 37.4979,
    "longitude": 127.0276
  }
}
```

**응답**: `200 OK`

| 필드 | 타입 | 설명 |
|---|---|---|
| durationSeconds | number | 이동시간(초) |
| distanceMeters | number | 이동거리(m) |
| durationText | string | 앱 표시용 이동시간 |
| distanceText | string | 앱 표시용 이동거리 |

```json
{
  "durationSeconds": 1840,
  "distanceMeters": 12300,
  "durationText": "31분",
  "distanceText": "12.3km"
}
```

**주요 에러**

| 상태 | 설명 |
|---|---|
| 400 Bad Request | 좌표 누락 또는 범위 오류 |
| 401 Unauthorized | 로그인 세션 없음 |
| 500 Internal Server Error | `GOOGLE_MAPS_API_KEY` 미설정 |
| 502 Bad Gateway | Google Routes API에서 경로 계산 실패 또는 잘못된 응답 반환 |

---

## 에러 응답

`GlobalExceptionHandler`(`common/GlobalExceptionHandler.java`)가 모든 예외를 공통 형식으로 변환한다.

### 401 Unauthorized — 인증 필요

인증이 필요한 API에 세션 없이 접근하면 반환된다. (`ResponseStatusException` 처리)

```
HTTP 401 Unauthorized
```
```json
{
  "message": "로그인이 필요합니다."
}
```

### 404 Not Found — 대상 없음

존재하지 않는 리소스(예: 없는 `routineId`, `appointmentId`)를 조회/수정/삭제할 때 `NoSuchElementException`이 발생하면 반환된다.

```
HTTP 404 Not Found
```
```json
{
  "message": "..."
}
```

### 400 Bad Request — 잘못된 요청 (일반)

`IllegalArgumentException`이 발생하면 반환된다. (예: 다른 사용자의 리소스 접근 등 도메인 검증 실패)

```
HTTP 400 Bad Request
```
```json
{
  "message": "..."
}
```

### 400 Bad Request — 입력값 검증 실패

`@Valid` 검증에 실패(`MethodArgumentNotValidException`)하면 반환된다. 위 두 케이스와 달리 `message` 키가 아니라 **필드명을 key로 하는 맵** 형태다.

```
HTTP 400 Bad Request
```
```json
{
  "email": "must be a well-formed email address",
  "password": "must not be blank"
}
```
