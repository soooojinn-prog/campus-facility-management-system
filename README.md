# 캠퍼스 시설 관리 시스템 (CFMS)

> **대학교 통합 시설 관리 플랫폼** — 강의실 예약부터 도서관, 기숙사, 동아리, 상담 예약까지 캠퍼스의 모든 시설을 하나의 서비스에서 관리합니다.

## 목차

- [개요](#개요)
- [기능 목록](#기능-목록)
- [기술 스택](#기술-스택)
- [시작하기](#시작하기)
  - [사전 요구 사항](#사전-요구-사항)
  - [환경 변수](#환경-변수)
  - [실행 방법](#실행-방법)
- [개발 프로필 (dev)](#개발-프로필-dev)
  - [시드 계정](#시드-계정)
- [API 엔드포인트 요약](#api-엔드포인트-요약)
- [역할 및 권한](#역할-및-권한)
- [프로젝트 구조](#프로젝트-구조)

## 개요

CFMS는 캠퍼스의 모든 시설을 하나의 서비스에서 관리하는 것을 목적으로 하는 통합 캠퍼스 포털입니다.

빌드 시 React 프론트엔드가 Spring Boot 리소스에 통합되어 **단일 JAR**로 배포됩니다. 별도의 프론트엔드 서버 없이 Spring Boot 단일 프로세스로 모든 요청을 처리합니다.

## 기능 목록

| 기능 | 설명 |
|---|---|
| 🗺 **캠퍼스 지도** | SVG 기반 인터랙티브 지도. 건물 클릭으로 해당 시설 페이지로 이동 |
| 🏫 **시설 예약** | 강의실·체육관 타임라인 UI 예약. 관리자 승인 후 확정 |
| 📚 **도서관** | 도서 검색, 열람실 좌석 예약, 스터디룸 예약, 혼잡도 조회, 공지사항 |
| 🏠 **기숙사** | 성별 구분 호실 조회, 입주 신청(룸메이트 학번 지정 가능), 관리자 승인 |
| 🎯 **동아리** | 동아리 개설·가입 신청, 부원 관리(회장 전용), 관리자 승인 |
| 🩺 **상담 예약** | 교무처·학생처·취업지원센터 상담사 조회 및 시간 슬롯 예약 |
| 🍱 **식당** | 날짜별 학생식당 식단 조회, 푸드코트 매장·메뉴 안내 |
| 👤 **마이페이지** | 모든 예약·신청 내역 통합 조회 및 취소, 내 정보 수정 |
| 🔐 **관리자** | 시설 예약·기숙사·동아리·상담 예약 신청 일괄 승인/거절 |

## 기술 스택

### 백엔드

| 기술 | 버전 |
|---|---|
| Java | 21 |
| Spring Boot | 3.5 |
| Spring Security | JWT (HttpOnly 쿠키) |
| Spring Data JPA | - |
| MySQL | 8.4.7 |
| Lombok | - |
| JJWT | 0.13.0 |

### 프론트엔드

| 기술 | 비고 |
|---|---|
| React | Vite 기반 SPA |
| Bun | 패키지 매니저 및 번들러 |
| Bootstrap | UI 프레임워크 |
| React Router | SPA 클라이언트 라우팅 |

## 시작하기

### 사전 요구 사항

- **Java 21** 이상
- **MySQL** 서버 (기본: `localhost:3306`)
- **Bun** ([설치 가이드](https://bun.sh)) — 프론트엔드 빌드에 사용

### 환경 변수

아래 환경 변수를 설정합니다. 값을 지정하지 않으면 괄호 안의 기본값이 사용됩니다.

| 환경 변수 | 기본값 | 설명 |
|---|---|---|
| `JDBC_SECRET` | `jdbc:mysql://localhost:3306/campus?createDatabaseIfNotExist=true&serverTimezone=Asia/Seoul` | MySQL JDBC URL |
| `MYSQL_USERNAME` | `root` | MySQL 사용자명 |
| `MYSQL_PASSWORD` | `mysql` | MySQL 비밀번호 |
| `JWT_SECRET` | *(내장 기본값)* | JWT 서명 시크릿 키 (프로덕션에서는 반드시 변경) |

> `createDatabaseIfNotExist=true` 옵션이 기본 URL에 포함되어 있어 DB가 없으면 자동 생성됩니다.
> 테이블은 JPA `ddl-auto: update` 설정으로 자동 생성·갱신됩니다.

### 실행 방법

#### 개발 환경 (dev 프로필)

```bash
# 저장소 클론
git clone <repository-url>
cd cfms

# 프론트엔드 의존성 설치 및 빌드 포함 전체 빌드
./gradlew build

# dev 프로필로 실행 (시드 데이터 자동 로드)
./gradlew bootRun --args='--spring.profiles.active=dev'
```

서버 기동 후 브라우저에서 [http://localhost:8080](http://localhost:8080) 에 접속합니다.

#### 프론트엔드 단독 개발 서버

백엔드와 프론트엔드를 분리하여 개발할 때 사용합니다.

```bash
cd frontend
bun install
bun run dev
```

#### 프로덕션 빌드 및 실행

```bash
./gradlew build
java -jar build/libs/cfms-1.0.0.jar
```

환경 변수를 직접 전달하는 경우:

```bash
java \
  -DJDBC_SECRET="jdbc:mysql://db-host:3306/campus?serverTimezone=Asia/Seoul" \
  -DMYSQL_USERNAME="cfms_user" \
  -DMYSQL_PASSWORD="your_password" \
  -DJWT_SECRET="your_very_long_secret_key" \
  -jar build/libs/cfms-1.0.0.jar
```

## 개발 프로필 (dev)

`--spring.profiles.active=dev` 옵션으로 실행하면 애플리케이션 기동 시 샘플 데이터가 자동으로 로드되고, **종료 시 자동으로 정리**됩니다. 데이터는 아래 순서로 로드됩니다.

```
1. 사용자 (users.jsonc)
2. 건물 (buildings.jsonc)
3. 호실 (rooms.jsonc)
4. 기숙사 호실 (dorm_rooms.jsonc)
5. 도서 (books.jsonc)
6. 상담사 (counselors.jsonc)
```

종료 시에는 사용자가 실행 중 생성한 데이터(예약, 신청 등)를 먼저 삭제한 후 시드 데이터를 역순으로 언로드합니다.

### 시드 계정

| 역할 | 이름 | 학번/교번 | 비밀번호 |
|---|---|---|---|
| 학생 | 홍길동 | `26001002` | `123` |
| 학생 | 이태백 | `26001004` | `123` |
| 학생 | 홍란 | `26001006` | `123` |
| 교수 | 장길산 | `26100200` | `123` |
| 교수 | 하율 | `26100400` | `123` |
| 관리자 | 박미래 | `26999998` | `123` |
| 관리자 | 사군자 | `26999999` | `123` |

> 관리자 계정은 `ROLE_ADMIN` 권한을 가지며, 모든 신청 내역의 승인·거절 처리가 가능합니다.

## API 엔드포인트 요약

### 인증 (`/api/auth`)

| 메서드 | 경로 | 권한 | 설명 |
|---|---|---|---|
| `POST` | `/api/auth/login` | 공개 | 로그인 (JWT 쿠키 발급) |
| `POST` | `/api/auth/logout` | 인증 | 로그아웃 (쿠키 만료) |
| `POST` | `/api/auth/register` | 공개 | 회원가입 (시연용) |
| `POST` | `/api/auth/password-reset` | 공개 | 비밀번호 재설정 1단계 (요청) |
| `POST` | `/api/auth/password-reset/verify` | 공개 | 비밀번호 재설정 2단계 (인증) |
| `PATCH` | `/api/auth/password-reset/confirm` | 공개 | 비밀번호 재설정 3단계 (확정) |

### 사용자 (`/api/users`)

| 메서드 | 경로 | 권한 | 설명 |
|---|---|---|---|
| `GET` | `/api/users/me` | 인증 | 내 프로필 조회 |
| `PATCH` | `/api/users/me` | 인증 | 내 정보 수정 |

### 시설 예약 (`/api/reservations`)

| 메서드 | 경로 | 권한 | 설명 |
|---|---|---|---|
| `GET` | `/api/reservations?roomId=&date=` | 공개 | 호실별 예약 현황 조회 |
| `POST` | `/api/reservations` | 인증 | 시설 예약 신청 |
| `GET` | `/api/reservations/me` | 인증 | 내 예약 내역 조회 |
| `DELETE` | `/api/reservations/{id}` | 인증 | 예약 취소 (PENDING만 가능) |

### 도서관 (`/api/library`)

| 메서드 | 경로 | 권한 | 설명 |
|---|---|---|---|
| `GET` | `/api/library/books` | 공개 | 도서 검색 |
| `POST` | `/api/library/books/{id}/reservation` | 인증 | 도서 예약 |
| `GET` | `/api/library/reading-rooms` | 공개 | 열람실 목록 |
| `GET` | `/api/library/reading-rooms/{id}/seats` | 공개 | 좌석 배치도 |
| `POST` | `/api/library/reading-rooms/{id}/seats/{seatNo}/reservations` | 인증 | 좌석 예약 |
| `GET` | `/api/library/reading-rooms/reservations/me` | 인증 | 내 좌석 예약 내역 |
| `DELETE` | `/api/library/reading-rooms/reservations/{id}` | 인증 | 좌석 예약 취소 |
| `GET` | `/api/library/study-rooms` | 공개 | 스터디룸 목록 |
| `GET` | `/api/library/study-rooms/{id}/slots` | 공개 | 스터디룸 예약 현황 |
| `POST` | `/api/library/study-rooms/{id}/reservations` | 인증 | 스터디룸 예약 |
| `GET` | `/api/library/study-rooms/reservations/me` | 인증 | 내 스터디룸 예약 내역 |
| `DELETE` | `/api/library/study-rooms/reservations/{id}` | 인증 | 스터디룸 예약 취소 |
| `GET` | `/api/library/congestion` | 공개 | 혼잡도 조회 |
| `GET` | `/api/library/notices` | 공개 | 공지사항 목록 |
| `GET` | `/api/library/notices/{id}` | 공개 | 공지사항 상세 |

### 기숙사 (`/api/dorms`)

| 메서드 | 경로 | 권한 | 설명 |
|---|---|---|---|
| `GET` | `/api/dorms/rooms?gender=` | 공개 | 호실 목록 (성별 필터) |
| `POST` | `/api/dorms/applications` | 인증 | 입주 신청 |
| `GET` | `/api/dorms/applications/me` | 인증 | 내 신청 내역 |
| `DELETE` | `/api/dorms/applications/{id}` | 인증 | 신청 취소 (PENDING만 가능) |

### 동아리 (`/api/clubs`)

| 메서드 | 경로 | 권한 | 설명 |
|---|---|---|---|
| `GET` | `/api/clubs` | 공개 | 동아리 목록 검색 |
| `POST` | `/api/clubs` | 인증 | 동아리 개설 신청 |
| `GET` | `/api/clubs/{slug}` | 공개 | 동아리 상세 조회 |
| `PATCH` | `/api/clubs/{slug}` | 인증 (회장) | 동아리 정보 수정 |
| `GET` | `/api/clubs/{slug}/members` | 공개 | 부원 목록 조회 |
| `POST` | `/api/clubs/{slug}/members` | 인증 | 가입 신청 |
| `DELETE` | `/api/clubs/{slug}/members/{userId}` | 인증 (회장) | 부원 추방 |
| `PATCH` | `/api/clubs/{slug}/members/{userId}/role` | 인증 (회장) | 부원 역할 변경 |

### 상담 예약 (`/api/counseling`)

| 메서드 | 경로 | 권한 | 설명 |
|---|---|---|---|
| `GET` | `/api/counseling/counselors` | 공개 | 상담사 목록 (부서 필터 가능) |
| `GET` | `/api/counseling/slots` | 공개 | 상담사·날짜별 예약 현황 |
| `POST` | `/api/counseling/reservations` | 인증 | 상담 예약 신청 |
| `GET` | `/api/counseling/reservations/me` | 인증 | 내 상담 예약 내역 |
| `DELETE` | `/api/counseling/reservations/{id}` | 인증 | 상담 예약 취소 |

### 식당 (`/api/cafeterias`)

| 메서드 | 경로 | 권한 | 설명 |
|---|---|---|---|
| `GET` | `/api/cafeterias/meals` | 공개 | 날짜별 학식 조회 |
| `GET` | `/api/cafeterias/foodcourt` | 공개 | 푸드코트 매장·메뉴 목록 |

### 관리자 (`/api/admin`) — `ROLE_ADMIN` 전용

| 메서드 | 경로 | 설명 |
|---|---|---|
| `GET` | `/api/admin/clubs?status=` | 동아리 개설 신청 목록 |
| `PATCH` | `/api/admin/clubs/{id}/status` | 동아리 개설 승인/거절 |
| `GET` | `/api/admin/reservations?status=` | 시설 예약 신청 목록 |
| `PATCH` | `/api/admin/reservations/{id}/status` | 시설 예약 승인/거절 |
| `GET` | `/api/admin/dorms?status=` | 기숙사 신청 목록 |
| `PATCH` | `/api/admin/dorms/{id}/status` | 기숙사 신청 승인/거절 |
| `GET` | `/api/admin/counseling?status=` | 상담 예약 신청 목록 |
| `PATCH` | `/api/admin/counseling/{id}/status` | 상담 예약 승인/거절 |

## 역할 및 권한

| 역할 | 설명 |
|---|---|
| `ROLE_STUDENT` | 시설 예약, 동아리 가입·개설 신청, 상담·기숙사·도서관 예약 |
| `ROLE_PROFESSOR` | 학생과 동일 + 반복 예약 가능 |
| `ROLE_ADMIN` | 모든 신청 내역 승인/거절, 관리자 전용 API 접근 |

## 프로젝트 구조

```
cfms/
├── frontend/                   # React 프론트엔드 (Vite + Bun)
│   ├── src/
│   │   ├── components/         # 공통 컴포넌트 (AuthModal 등)
│   │   ├── context/            # React Context (AuthContext)
│   │   ├── data/               # API 호출 함수, mock 데이터
│   │   ├── page/               # 페이지 컴포넌트
│   │   └── styles/             # 전역 CSS
│   └── index.html
│
└── src/main/java/io/github/wizwix/cfms/
    ├── controller/             # REST API 컨트롤러
    ├── dto/
    │   ├── request/            # 요청 DTO
    │   └── response/           # 응답 DTO
    ├── global/
    │   ├── config/
    │   │   └── dev/            # dev 프로필 시드 데이터 로더
    │   ├── exception/          # 전역 예외 처리
    │   ├── filter/             # JWT 인증 필터, Rate Limit 필터
    │   └── security/           # Spring Security 설정
    ├── model/                  # JPA 엔티티
    ├── repo/                   # Spring Data JPA 레포지토리
    └── service/                # 비즈니스 로직
        └── iface/              # 서비스 인터페이스
```
