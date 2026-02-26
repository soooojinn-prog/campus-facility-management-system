// 건물 SVG polygon 좌표 (캠퍼스 지도 위 클릭 영역)
// - rentable: true인 건물만 클릭 가능 (hover 시 파란색 하이라이트)
// - customRoute: 기본 /building/:key 대신 별도 경로로 이동 (예: 식당 → /cafeteria)
export const BUILDING_POLYGONS = [{
  key: 'lecture1',
  name: '강의동 1',
  info: '강의실 12개 · 1~4층',
  rentable: true,
  points: '347,295 406,274 418,296 421,318 361,340 345,315',
}, {
  key: 'lecture2',
  name: '강의동 2',
  info: '강의실 15개 · 1~5층',
  rentable: true,
  points: '421,231 476,211 494,235 495,253 433,271 421,251',
}, {
  key: 'lecture3',
  name: '강의동 3',
  info: '강의실 10개 · 세미나실 5개 · 1~4층',
  rentable: true,
  points: '518,217 553,206 559,217 580,206 589,213 591,239 565,255 534,266 528,261 515,263',
}, {
  key: 'lecture4',
  name: '강의동 4',
  info: '강의실 8개 · 실습실 6개 · 1~5층',
  rentable: true,
  points: '610,173 631,162 643,166 652,177 653,201 604,235 591,230 589,204 610,192',
}, {
  key: 'cafeteria',
  name: '식당',
  info: '학생식당 · 푸드코트',
  rentable: true,
  customRoute: '/cafeteria',
  points: '239,382 292,363 315,412 313,425 262,446 239,396',
}, {
  key: 'admin',
  name: '행정동',
  info: '총장실 · 교무처 · 학생처 · 상담 예약',
  rentable: true,
  customRoute: '/counseling',
  points: '389,46 417,38 426,46 432,105 400,117 388,105',
}, {
  key: 'library',
  name: '도서관',
  info: '열람실 · 자료실 · 스터디룸',
  rentable: true,
  customRoute: '/library',
  points: '418,131 481,111 501,136 499,153 436,175 417,153',
}, {
  key: 'dormitory',
  name: '기숙사',
  info: '학생 생활관 · 1~5층',
  rentable: true,
  customRoute: '/dormitory',
  points: '25,147 26,161 52,277 145,253 143,221 124,170 80,185 69,168 53,163 50,143',
}, {
  key: 'gym',
  name: '체육관',
  info: '축구장 · 농구장 · 배드민턴장',
  rentable: true,
  points: '587,487 643,543 669,549 689,545 704,529 704,519 713,527 719,523 709,509 713,502 709,491 723,487 756,512 797,480 772,455 746,469 745,457 746,437 732,423 715,423 696,433 690,448 690,466 679,459 662,465 649,454 628,449 607,451 587,465',
}];

// 학기 데이터 — 백엔드 SemesterController 미구현으로 프론트 mock 사용 중
export const SEMESTERS = [{name: '2025년 2학기', start: '2025-09-01', end: '2025-12-19'}, {
  name: '2025년 겨울 계절수업', start: '2025-12-22', end: '2026-02-20',
}, {name: '2026년 1학기', start: '2026-03-02', end: '2026-06-19'}];

export function getCurrentSemester() {
  const t = new Date().toISOString().split('T')[0];
  for (const s of SEMESTERS) {
    if (t >= s.start && t <= s.end) return s;
  }
  for (const s of SEMESTERS) {
    if (t < s.start) return {...s, name: s.name + ' (예정)'};
  }
  return SEMESTERS[SEMESTERS.length - 1];
}

export const MONTH_NAMES = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];

// RoomType 한글 매핑
export const ROOM_TYPE_LABELS = {
  AUDITORIUM: '대강의실', CLASSROOM: '강의실', COMPUTER: '컴퓨터실',
  COURT: '구기장', GYM: '헬스장', LAB: '실습실', RACQUET: '라켓스포츠', SEMINAR: '세미나실',
};
