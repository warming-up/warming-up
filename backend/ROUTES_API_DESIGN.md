# Routes API 이동시간 계산 설계

## 목표

안드로이드 앱에서 사용자의 현재 위치와 도착지 좌표를 백엔드로 보내면, 백엔드가 Google Maps Platform Routes API의 Compute Routes를 호출해 자동차 기준 예상 이동시간과 거리를 반환한다.

## 구조

```text
Android App
  FusedLocationProviderClient로 현재 위치 획득
  도착지 좌표와 함께 백엔드 호출
        |
        v
Backend /api/routes/eta
  세션 인증 확인
  좌표 입력 검증
  Google Routes API 호출
  duration/distanceMeters 포맷팅
        |
        v
Android App
  "31분 · 12.3km" 표시
```

## API 키 관리

실제 API 키는 저장소에 커밋하지 않는다. 로컬에서는 `.env` 또는 실행 환경변수에 넣고, 배포 환경에서는 Railway/서버의 환경변수로 설정한다.

```env
GOOGLE_MAPS_API_KEY=실제_API_KEY
GOOGLE_MAPS_ROUTES_BASE_URL=https://routes.googleapis.com
```

`.env.example`에는 변수명과 빈 값만 둔다.

Google Cloud Console의 API 키 제한은 다음을 권장한다.

- API 제한사항: `Routes API`
- 애플리케이션 제한사항: 백엔드 배포 서버의 고정 공인 IP

## 백엔드 엔드포인트

```http
POST /api/routes/eta
Cookie: JSESSIONID=...
Content-Type: application/json
```

요청:

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

응답:

```json
{
  "durationSeconds": 1840,
  "distanceMeters": 12300,
  "durationText": "31분",
  "distanceText": "12.3km"
}
```

## Google Routes API 호출

백엔드는 REST로 Compute Routes를 호출한다. 현재 API 키 기반 인증을 사용하므로 Java 클라이언트 라이브러리의 ADC 인증보다 REST 호출이 단순하다.

```http
POST https://routes.googleapis.com/directions/v2:computeRoutes
X-Goog-Api-Key: ${GOOGLE_MAPS_API_KEY}
X-Goog-FieldMask: routes.duration,routes.distanceMeters
Content-Type: application/json
```

요청 본문:

```json
{
  "origin": {
    "location": {
      "latLng": {
        "latitude": 37.5665,
        "longitude": 126.9780
      }
    }
  },
  "destination": {
    "location": {
      "latLng": {
        "latitude": 37.4979,
        "longitude": 127.0276
      }
    }
  },
  "travelMode": "DRIVE",
  "routingPreference": "TRAFFIC_AWARE",
  "units": "METRIC"
}
```

## 구현 경계

- `RouteController`: HTTP 요청, 세션 인증 확인, DTO 검증
- `RouteService`: Google 응답을 앱 응답으로 변환
- `GoogleRoutesClient`: Google Routes API 호출 경계
- `GoogleRoutesRestClient`: REST 호출 구현

## 안드로이드 역할

안드로이드는 Routes API 키를 들고 있지 않는다.

1. 위치 권한 요청
2. `FusedLocationProviderClient`로 현재 위치 획득
3. 도착지 좌표와 함께 `/api/routes/eta` 호출
4. 백엔드 응답의 `durationText`, `distanceText` 표시
