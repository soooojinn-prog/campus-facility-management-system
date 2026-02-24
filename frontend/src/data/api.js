// Vite 프록시를 통해 백엔드(8080)로 전달됨 (vite.config.js 참고)
const BASE = '/api';

// ── 건물/시설 관련 (백엔드 미구현 → 현재 buildings.js mock 데이터 사용 중) ──

// TODO: 백엔드 BuildingApiController 구현 후 활성화
export async function fetchBuildingMap() {
  const res = await fetch(`${BASE}/buildings/map`);
  return res.json();
}

// TODO: 백엔드 BuildingApiController 구현 후 활성화
export async function fetchBuildingDetail(key) {
  const res = await fetch(`${BASE}/buildings/${key}`);
  return res.json();
}

// TODO: 백엔드 ReservationApiController 구현 후 활성화
export async function fetchReservations(roomKey, date) {
  const res = await fetch(`${BASE}/reservations?roomKey=${roomKey}&date=${date}`);
  return res.json();
}

export async function createReservation(data) {
  const res = await fetch(`${BASE}/reservations`, {
    method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(data),
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text);
  }
  return res.json();
}

// TODO: 백엔드 SemesterController 자체가 없음 — 구현 필요
export async function fetchCurrentSemester() {
  const res = await fetch(`${BASE}/semesters/current`);
  return res.json();
}

// ── 인증 관련 ──

/// 로그인 — 백엔드 RequestLogin DTO에 맞춰 userNumber 사용
/// 응답: { accessToken, user: { id, name, userNumber, role } }
export async function loginApi(userNumber, password) {
  const res = await fetch(`${BASE}/auth/login`, {
    method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({userNumber, password}),
  });
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

/// 회원가입 — 백엔드 RequestRegister DTO: { userNumber, name, password, email }
/// 응답: 201 Created (body 없음), 가입 후 별도 로그인 필요
export async function signupApi(userNumber, password, name, email) {
  const res = await fetch(`${BASE}/auth/register`, {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({userNumber, name, password, email}),
  });
  if (!res.ok) throw new Error(await res.text());
}

/// 로그아웃 — 백엔드에서 JWT 쿠키를 만료시킴
export async function logoutApi() {
  await fetch(`${BASE}/auth/logout`, {method: 'POST'});
}
