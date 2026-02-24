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
  info: '총장실 · 교무처 · 학생처',
  rentable: false,
  points: '389,46 417,38 426,46 432,105 400,117 388,105',
}, {
  key: 'library',
  name: '도서관',
  info: '열람실 · 자료실 · 스터디룸',
  rentable: false,
  points: '418,131 481,111 501,136 499,153 436,175 417,153',
}, {
  key: 'dormitory',
  name: '기숙사',
  info: '학생 생활관 · 1~8층',
  rentable: false,
  points: '25,147 26,161 52,277 145,253 143,221 124,170 80,185 69,168 53,163 50,143',
}, {
  key: 'gym',
  name: '체육관',
  info: '축구장 · 농구장 · 배드민턴장',
  rentable: true,
  points: '587,487 643,543 669,549 689,545 704,529 704,519 713,527 719,523 709,509 713,502 709,491 723,487 756,512 797,480 772,455 746,469 745,457 746,437 732,423 715,423 696,433 690,448 690,466 679,459 662,465 649,454 628,449 607,451 587,465',
}];

// 건물 상세 데이터 — 백엔드 BuildingApiController 미구현으로 프론트 mock 사용 중
// 구조: { [buildingKey]: { name, info, floors: { [층번호]: { desc, rooms[] } } } }
// rooms 항목: id(호실코드), name, type, category, capacity, status(available/partial/full)
export const BUILDING_DATA = {
  lecture1: {
    name: '강의동 1', info: '강의실 12개 · 1~4층', floors: {
      1: {
        desc: '대강의실 / 강의실', rooms: [{
          id: 'L1-101', name: '101호', type: '대강의실', category: '대강의실', capacity: 120, status: 'available',
        }, {id: 'L1-102', name: '102호', type: '강의실', category: '강의실', capacity: 60, status: 'partial'}, {
          id: 'L1-103', name: '103호', type: '강의실', category: '강의실', capacity: 60, status: 'full',
        }],
      }, 2: {
        desc: '강의실 / 세미나실', rooms: [{
          id: 'L1-201', name: '201호', type: '강의실', category: '강의실', capacity: 80, status: 'available',
        }, {id: 'L1-202', name: '202호', type: '강의실', category: '강의실', capacity: 60, status: 'available'}, {
          id: 'L1-203', name: '203호', type: '세미나실', category: '세미나실', capacity: 20, status: 'partial',
        }],
      }, 3: {
        desc: '강의실', rooms: [{
          id: 'L1-301', name: '301호', type: '강의실', category: '강의실', capacity: 60, status: 'available',
        }, {id: 'L1-302', name: '302호', type: '강의실', category: '강의실', capacity: 60, status: 'full'}],
      }, 4: {
        desc: '세미나실',
        rooms: [{id: 'L1-401', name: '401호', type: '세미나실', category: '세미나실', capacity: 30, status: 'available'}],
      },
    },
  }, lecture2: {
    name: '강의동 2', info: '강의실 15개 · 1~5층', floors: {
      1: {
        desc: '대강의실', rooms: [{
          id: 'L2-101', name: '101호', type: '대강의실', category: '대강의실', capacity: 150, status: 'partial',
        }, {id: 'L2-102', name: '102호', type: '강의실', category: '강의실', capacity: 80, status: 'available'}],
      }, 2: {
        desc: '강의실', rooms: [{id: 'L2-201', name: '201호', type: '강의실', category: '강의실', capacity: 60, status: 'full'}, {
          id: 'L2-202', name: '202호', type: '강의실', category: '강의실', capacity: 60, status: 'available',
        }, {id: 'L2-203', name: '203호', type: '강의실', category: '강의실', capacity: 40, status: 'available'}],
      }, 3: {
        desc: '실습실', rooms: [{
          id: 'L2-301', name: '301호', type: '컴퓨터실', category: '실습실', capacity: 50, status: 'partial',
        }, {id: 'L2-302', name: '302호', type: '실습실', category: '실습실', capacity: 40, status: 'available'}],
      }, 4: {
        desc: '강의실',
        rooms: [{id: 'L2-401', name: '401호', type: '강의실', category: '강의실', capacity: 60, status: 'available'}],
      }, 5: {
        desc: '세미나실',
        rooms: [{id: 'L2-501', name: '501호', type: '세미나실', category: '세미나실', capacity: 25, status: 'available'}],
      },
    },
  }, lecture3: {
    name: '강의동 3', info: '강의실 10개 · 세미나실 5개', floors: {
      1: {
        desc: '강의실', rooms: [{
          id: 'L3-101', name: '101호', type: '강의실', category: '강의실', capacity: 80, status: 'available',
        }, {id: 'L3-102', name: '102호', type: '세미나실', category: '세미나실', capacity: 25, status: 'available'}],
      }, 2: {
        desc: '세미나실', rooms: [{
          id: 'L3-201', name: '201호', type: '강의실', category: '강의실', capacity: 60, status: 'partial',
        }, {id: 'L3-202', name: '202호', type: '세미나실', category: '세미나실', capacity: 20, status: 'full'}],
      },
    },
  }, lecture4: {
    name: '강의동 4', info: '강의실 8개 · 실습실 6개', floors: {
      1: {
        desc: '실습실', rooms: [{
          id: 'L4-101', name: '101호', type: '실습실', category: '실습실', capacity: 40, status: 'available',
        }, {id: 'L4-102', name: '102호', type: '실습실', category: '실습실', capacity: 40, status: 'partial'}],
      }, 2: {
        desc: '강의실', rooms: [{id: 'L4-201', name: '201호', type: '강의실', category: '강의실', capacity: 80, status: 'full'}, {
          id: 'L4-202', name: '202호', type: '실습실', category: '실습실', capacity: 35, status: 'available',
        }],
      },
    },
  }, gym: {
    name: '체육관', info: '축구장 · 농구장 · 배드민턴장', floors: {
      1: {
        desc: '실외 시설', rooms: [{
          id: 'G-soccer', name: '축구장', type: '체육시설', category: '구기', capacity: 200, status: 'partial',
        }, {
          id: 'G-baskA', name: '농구장 A', type: '체육시설', category: '구기', capacity: 80, status: 'available',
        }, {id: 'G-badm', name: '배드민턴장', type: '체육시설', category: '라켓', capacity: 40, status: 'full'}],
      }, 2: {
        desc: '실내 시설',
        rooms: [{id: 'G-gym', name: '헬스장', type: '체육시설', category: '피트니스', capacity: 50, status: 'full'}, {
          id: 'G-tt', name: '탁구장', type: '체육시설', category: '라켓', capacity: 30, status: 'available',
        }],
      },
    },
  },
};

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

// 가상 예약 데이터 생성 — roomId+날짜 기반 시드로 일관된 가짜 예약 생성
// 백엔드 ReservationApiController 구현 후 fetchReservations()로 교체 예정
export function genReservations(roomId, dateStr) {
  const seed = roomId.charCodeAt(0) + roomId.charCodeAt(roomId.length - 1) + parseInt(dateStr.replace(/-/g, '')) % 100;
  const res = [];
  const names = ['컴퓨터공학개론', '알고리즘', '데이터베이스', '운영체제', '네트워크', '축구동아리 모임', '농구동아리 연습', '프로그래밍 스터디', '교류 대회', '학생회 회의'];
  const profs = ['김철수 교수', '박영희 교수', '이준호 교수', '정미연 교수'];
  for (let h = 9; h < 22; h++) {
    const r = (seed * 31 + h * 7) % 100;
    if (r < 35) {
      const isPending = r < 10;
      const isProf = r >= 10;
      const dur = r < 20 ? 2 : 1;
      const name = names[(seed + h) % names.length];
      const prof = profs[(seed + h) % profs.length];
      res.push({
        start: h,
        end: Math.min(h + dur, 22),
        title: name,
        detail: isProf ? prof : '동아리 예약',
        status: isPending ? 'pending' : 'approved',
      });
      h += dur - 1;
    }
  }
  return res;
}
