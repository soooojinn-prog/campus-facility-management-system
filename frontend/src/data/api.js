// Vite 프록시를 통해 백엔드(8080)로 전달됨 (vite.config.js 참고)
const BASE = '/api';

// ── 건물/시설 관련 ──

/// 건물 목록 조회 (공개)
/// 응답: [{ id, name, slug, info, points, type, rentable }]
export async function fetchBuildings() {
  const res = await fetch(`${BASE}/buildings`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

/// 건물 호실 목록 조회 (공개)
/// 응답: [{ id, buildingName, name, floor, type, capacity }]
export async function fetchBuildingRooms(slug) {
  const res = await fetch(`${BASE}/buildings/${slug}/rooms`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

/// 호실별 예약 현황 조회 (공개)
/// 응답: [{ id, roomCode, buildingName, userName, clubName, startTime, endTime, status, purpose, rejectReason, createdAt }]
export async function fetchRoomReservations(roomId, date) {
  const res = await fetch(`${BASE}/reservations?roomId=${roomId}&date=${date}`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

/// 시설 예약 신청 — 로그인 필요
/// data: { roomId, startTime, endTime, purpose, clubId? }
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

// ── 식당 관련 ──

/// 오늘의 학식 — date 없으면 서버에서 오늘 날짜로 조회
/// 응답: { date, meals: [{ type, time, icon, items: [{ name, price, discount }] }] }
export async function fetchTodayMeals(date) {
  const q = date ? `?date=${date}` : '';
  const res = await fetch(`${BASE}/cafeterias/meals${q}`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

/// 푸드코트 가게 전체
/// 응답: [{ id, name, desc, category, representative, hours, menu: [{ name, price, discount, popular }] }]
export async function fetchFoodCourtStores() {
  const res = await fetch(`${BASE}/cafeterias/foodcourt`);
  if (!res.ok) throw new Error(await res.text());
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

/// 회원가입 — 백엔드 RequestRegister DTO: { userNumber, name, password, email, gender }
/// 응답: 201 Created (body 없음), 가입 후 별도 로그인 필요
export async function signupApi(userNumber, password, name, email, gender, role) {
  const res = await fetch(`${BASE}/auth/register`, {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({userNumber, name, password, email, gender, role}),
  });
  if (!res.ok) {
    try {
      const json = JSON.parse(await res.text());
      throw new Error(json.message || '회원가입에 실패했습니다.');
    } catch (e) {
      if (e instanceof SyntaxError) throw new Error('회원가입에 실패했습니다.');
      throw e;
    }
  }
}

/// 로그아웃 — 백엔드에서 JWT 쿠키를 만료시킴
export async function logoutApi() {
  await fetch(`${BASE}/auth/logout`, {method: 'POST'});
}

// ── 기숙사 관련 ──

/// 기숙사 호실 목록 (층별) — gender: "MALE" or "FEMALE"
/// 응답: [{ floor, rooms: [{ id, roomNumber, floor, occupancy, residentName }] }]
export async function fetchDormRooms(gender) {
  const res = await fetch(`${BASE}/dorms/rooms?gender=${gender}`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

/// 기숙사 입주 신청 — 로그인 필요 (JWT 쿠키 자동 전송)
/// data: { roomId, semester, period, partnerNumber }
/// 응답: { applicationId, roomNumber, message }
export async function applyDorm(data) {
  const res = await fetch(`${BASE}/dorms/applications`, {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(data),
  });
  if (!res.ok) {
    try {
      const json = JSON.parse(await res.text());
      throw new Error(json.message || '기숙사 신청에 실패했습니다.');
    } catch (e) {
      if (e instanceof SyntaxError) throw new Error('기숙사 신청에 실패했습니다.');
      throw e;
    }
  }
  return res.json();
}

// ── 마이페이지 관련 ──

/// 내 프로필 조회 — 로그인 필요
/// 응답: { id, name, userNumber, email, role, gender, createdAt }
export async function fetchMyProfile() {
  const res = await fetch(`${BASE}/users/me`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

/// 내 정보 수정 — 로그인 필요
/// data: { oldPassword, newPassword, email }
export async function updateMyProfile(data) {
  const res = await fetch(`${BASE}/users/me`, {
    method: 'PATCH',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error(await res.text());
}

/// 내 기숙사 신청 내역 조회 — 로그인 필요
/// 응답: [{ id, roomNumber, semester, period, status, partnerName, createdAt }]
export async function fetchMyDormApplications() {
  const res = await fetch(`${BASE}/dorms/applications/me`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

/// 기숙사 신청 취소 — PENDING만 가능, 로그인 필요
export async function cancelDormApplication(id) {
  const res = await fetch(`${BASE}/dorms/applications/${id}`, {method: 'DELETE'});
  if (!res.ok) throw new Error(await res.text());
}

// ── 관리자 관련 ──

/// 동아리 개설 신청 목록 조회 — ROLE_ADMIN 필요
export async function fetchAdminClubs(status = 'PENDING') {
  const res = await fetch(`${BASE}/admin/clubs?status=${status}`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

/// 동아리 개설 승인/거절 — ROLE_ADMIN 필요
export async function updateAdminClubStatus(id, data) {
  const res = await fetch(`${BASE}/admin/clubs/${id}/status`, {
    method: 'PATCH',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error(await res.text());
}

/// 시설 예약 목록 조회 — ROLE_ADMIN 필요
export async function fetchAdminReservations(status = 'PENDING') {
  const res = await fetch(`${BASE}/admin/reservations?status=${status}`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

/// 시설 예약 승인/거절 — ROLE_ADMIN 필요
export async function updateAdminReservationStatus(id, data) {
  const res = await fetch(`${BASE}/admin/reservations/${id}/status`, {
    method: 'PATCH',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error(await res.text());
}

/// 기숙사 신청 목록 조회 — ROLE_ADMIN 필요
export async function fetchAdminDorms(status = 'PENDING') {
  const res = await fetch(`${BASE}/admin/dorms?status=${status}`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

/// 기숙사 신청 승인/거절 — ROLE_ADMIN 필요
export async function updateAdminDormStatus(id, data) {
  const res = await fetch(`${BASE}/admin/dorms/${id}/status`, {
    method: 'PATCH',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error(await res.text());
}

// ── 시설 예약 (마이페이지) ──

/// 내 강의실 예약 내역 조회 — 로그인 필요
export async function fetchMyReservations() {
  const res = await fetch(`${BASE}/reservations/me`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

/// 강의실 예약 취소 — PENDING만 가능, 로그인 필요
export async function cancelReservation(id) {
  const res = await fetch(`${BASE}/reservations/${id}`, {method: 'DELETE'});
  if (!res.ok) throw new Error(await res.text());
}

// ── 상담 예약 관련 ──

/// 상담사 목록 — dept: "ACADEMIC" | "STUDENT" | "CAREER" (null이면 전체)
export async function fetchCounselors(dept) {
  const q = dept ? `?dept=${dept}` : '';
  const res = await fetch(`${BASE}/counseling/counselors${q}`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

/// 해당 상담사+날짜의 예약 현황 — 공개 API
export async function fetchCounselingSlots(counselorId, date) {
  const res = await fetch(`${BASE}/counseling/slots?counselorId=${counselorId}&date=${date}`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

/// 상담 예약 신청 — 로그인 필요
export async function createCounselingReservation(data) {
  const res = await fetch(`${BASE}/counseling/reservations`, {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

/// 내 상담 예약 내역 — 로그인 필요
export async function fetchMyCounselingReservations() {
  const res = await fetch(`${BASE}/counseling/reservations/me`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

/// 상담 예약 취소 — PENDING만 가능, 로그인 필요
export async function cancelCounselingReservation(id) {
  const res = await fetch(`${BASE}/counseling/reservations/${id}`, {method: 'DELETE'});
  if (!res.ok) throw new Error(await res.text());
}

// ── 도서관 관련 ──

// 백엔드 에러 응답에서 message 필드 추출 헬퍼
async function parseErrorMessage(res, fallback) {
  try {
    const text = await res.text();
    const json = JSON.parse(text);
    return json.message || fallback;
  } catch {
    return fallback;
  }
}

/// 열람실 목록 조회
export async function fetchReadingRooms() {
  const res = await fetch(`${BASE}/library/reading-rooms`);
  if (!res.ok) throw new Error('열람실 정보를 불러오지 못했습니다.');
  return res.json();
}

/// 열람실 좌석 배치도 조회
export async function fetchReadingRoomSeats(roomId) {
  const res = await fetch(`${BASE}/library/reading-rooms/${roomId}/seats`);
  if (!res.ok) throw new Error('좌석 정보를 불러오지 못했습니다.');
  return res.json();
}

/// 열람실 좌석 예약 — 로그인 필요
export async function reserveSeat(roomId, seatNo) {
  const res = await fetch(
      `${BASE}/library/reading-rooms/${roomId}/seats/${seatNo}/reservations`,
      {method: 'POST', headers: {'Content-Type': 'application/json'}},
  );
  if (!res.ok) throw new Error(await parseErrorMessage(res, '좌석 예약에 실패했습니다.'));
}

/// 도서 검색 — q: 검색어, publisher: 출판사, category: 카테고리
export async function searchBooks({q = '', publisher = '', category = ''} = {}) {
  const params = new URLSearchParams();
  if (q) params.set('q', q);
  if (publisher) params.set('publisher', publisher);
  if (category) params.set('category', category);
  const res = await fetch(`${BASE}/library/books?${params}`);
  if (!res.ok) throw new Error('도서 검색에 실패했습니다.');
  return res.json();
}

/// 도서 예약 — 로그인 필요
export async function reserveBook(bookId) {
  const res = await fetch(`${BASE}/library/books/${bookId}/reservation`, {
    method: 'POST', headers: {'Content-Type': 'application/json'},
  });
  if (!res.ok) throw new Error('도서 예약에 실패했습니다.');
}

/// 스터디룸 목록 조회
export async function fetchStudyRooms() {
  const res = await fetch(`${BASE}/library/study-rooms`);
  if (!res.ok) throw new Error('스터디룸 정보를 불러오지 못했습니다.');
  return res.json();
}

/// 스터디룸 예약 현황 (시간 슬롯) 조회
export async function fetchStudyRoomSlots(roomId, date) {
  const res = await fetch(
      `${BASE}/library/study-rooms/${roomId}/slots?date=${date}`,
  );
  if (!res.ok) throw new Error('예약 현황을 불러오지 못했습니다.');
  return res.json();
}

/// 스터디룸 예약 — 로그인 필요
export async function reserveStudyRoom(roomId, {date, startHour}) {
  const res = await fetch(
      `${BASE}/library/study-rooms/${roomId}/reservations`,
      {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({date, startHour}),
      },
  );
  if (!res.ok) throw new Error(await parseErrorMessage(res, '스터디룸 예약에 실패했습니다.'));
}

/// 도서관 혼잡도 조회
export async function fetchCongestion() {
  const res = await fetch(`${BASE}/library/congestion`);
  if (!res.ok) throw new Error('혼잡도 정보를 불러오지 못했습니다.');
  return res.json();
}

/// 도서관 공지사항 목록 조회
export async function fetchNotices() {
  const res = await fetch(`${BASE}/library/notices`);
  if (!res.ok) throw new Error('공지사항을 불러오지 못했습니다.');
  return res.json();
}

/// 도서관 공지사항 상세 조회
export async function fetchNotice(noticeId) {
  const res = await fetch(`${BASE}/library/notices/${noticeId}`);
  if (!res.ok) throw new Error('공지 상세를 불러오지 못했습니다.');
  return res.json();
}

/// 내 열람실 좌석 예약 내역 — 로그인 필요
export async function fetchMySeatReservations() {
  const res = await fetch(`${BASE}/library/reading-rooms/reservations/me`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

/// 내 스터디룸 예약 내역 — 로그인 필요
export async function fetchMyStudyRoomReservations() {
  const res = await fetch(`${BASE}/library/study-rooms/reservations/me`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

/// 상담 예약 목록 조회 — ROLE_ADMIN 필요
export async function fetchAdminCounseling(status = 'PENDING') {
  const res = await fetch(`${BASE}/admin/counseling?status=${status}`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

/// 상담 예약 승인/거절 — ROLE_ADMIN 필요
export async function updateAdminCounselingStatus(id, data) {
  const res = await fetch(`${BASE}/admin/counseling/${id}/status`, {
    method: 'PATCH',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error(await res.text());
}

/// 열람실 좌석 예약 취소 — 로그인 필요
export async function cancelSeatReservation(reservationId) {
  const res = await fetch(
      `${BASE}/library/reading-rooms/reservations/${reservationId}`,
      {method: 'DELETE'},
  );
  if (!res.ok) throw new Error(await parseErrorMessage(res, '좌석 예약 취소에 실패했습니다.'));
}

/// 스터디룸 예약 취소 — 로그인 필요
export async function cancelStudyRoomReservation(reservationId) {
  const res = await fetch(
      `${BASE}/library/study-rooms/reservations/${reservationId}`,
      {method: 'DELETE'},
  );
  if (!res.ok) throw new Error(await parseErrorMessage(res, '스터디룸 예약 취소에 실패했습니다.'));
}

// ── 동아리 관련 ──

/// 내가 가입한 동아리 목록 — 마이페이지 동아리 탭
export async function fetchMyClubs() {
  const res = await fetch(`${BASE}/clubs/my`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

/// 동아리 목록 검색 — q: 검색어 (공개)
export async function fetchClubs(q = '') {
  const res = await fetch(`${BASE}/clubs${q ? `?q=${encodeURIComponent(q)}` : ''}`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

/// 동아리 상세 조회 — slug 기반 (공개)
export async function fetchClubDetail(slug) {
  const res = await fetch(`${BASE}/clubs/${slug}`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

/// 동아리 부원 목록 조회 (공개)
export async function fetchClubMembers(slug) {
  const res = await fetch(`${BASE}/clubs/${slug}/members`);
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

/// 동아리 개설 신청 — 로그인 필요
/// data: { name, slug, description, autoApprove }
export async function createClub(data) {
  const res = await fetch(`${BASE}/clubs`, {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

/// 동아리 가입 신청 — 로그인 필요
export async function joinClub(slug) {
  const res = await fetch(`${BASE}/clubs/${slug}/members`, {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
  });
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

/// 동아리 정보 수정 — 동아리장만 가능, 로그인 필요
/// data: { name, description, autoApprove }
export async function updateClub(slug, data) {
  const res = await fetch(`${BASE}/clubs/${slug}`, {
    method: 'PATCH',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}