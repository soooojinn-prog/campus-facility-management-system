/// 학생식당 오늘의 학식 데이터 (mock)
/// 구조: meals[] → 각 끼니별 { type, time, icon, items[] }
/// items 항목: name(메뉴명), price(정가), discount(할인 정보, null이면 할인 없음)
/// discount: { price(할인가), label(할인 사유 텍스트) }
/// TODO: 백엔드 /api/cafeteria/meals API 구현 후 교체
export const TODAY_MEALS = {
  date: new Date().toISOString().split('T')[0],
  meals: [
    {
      type: '조식', time: '08:00 ~ 09:30', icon: '🌅',
      items: [
        {name: '토스트 & 스크램블 에그', price: 2500, discount: null},
        {name: '미역국 + 쌀밥 정식', price: 3000, discount: null},
        {name: '시리얼 & 우유', price: 1500, discount: {price: 1000, label: '조식 특가'}},
      ],
    },
    {
      type: '중식', time: '11:30 ~ 13:30', icon: '☀️',
      items: [
        {name: '돈까스 정식', price: 4500, discount: null},
        {name: '김치찌개 + 쌀밥', price: 4000, discount: {price: 3500, label: '이번 주 할인'}},
        {name: '비빔밥', price: 4000, discount: null},
        {name: '우동', price: 3500, discount: null},
      ],
    },
    {
      type: '석식', time: '17:00 ~ 19:00', icon: '🌙',
      items: [
        {name: '제육볶음 정식', price: 4500, discount: {price: 3800, label: '석식 할인'}},
        {name: '된장찌개 + 쌀밥', price: 4000, discount: null},
        {name: '라면 + 김밥 세트', price: 3500, discount: null},
      ],
    },
  ],
};

/// 푸드코트 가게 목록 (mock) — 현재 8개 매장
/// 각 가게: id, name, desc, category(카테고리→아이콘 매핑용), representative, hours, menu[]
/// 메뉴 아이템: name, price, discount({ price, label } 또는 null), popular(인기 메뉴 여부)
/// 카테고리별 아이콘은 CafeteriaPage.jsx의 STORE_ICONS에서 관리
/// TODO: 백엔드 /api/cafeteria/foodcourt API 구현 후 교체
export const FOODCOURT_STORES = [
  {
    id: 'fc-hansot', name: '한솥도시락', desc: '든든한 한 끼, 다양한 도시락 메뉴',
    category: '한식', representative: '치킨마요 도시락', hours: '10:00 ~ 20:00',
    menu: [
      {name: '치킨마요 도시락', price: 4500, discount: null, popular: true},
      {name: '제육 도시락', price: 5000, discount: {price: 4500, label: '이번 주 특가'}},
      {name: '불고기 도시락', price: 5500, discount: null},
      {name: '참치김치 도시락', price: 4800, discount: null},
      {name: '김치볶음밥', price: 4000, discount: {price: 3500, label: '학생 할인'}},
    ],
  },
  {
    id: 'fc-bunsik', name: '엄마손 분식', desc: '추억의 맛, 학교 앞 분식집',
    category: '분식', representative: '떡볶이 + 튀김 세트', hours: '09:00 ~ 20:00',
    menu: [
      {name: '떡볶이', price: 3000, discount: null, popular: true},
      {name: '순대', price: 3000, discount: null},
      {name: '떡볶이 + 튀김 세트', price: 4500, discount: {price: 4000, label: '세트 할인'}, popular: true},
      {name: '김밥', price: 2500, discount: null},
      {name: '라볶이', price: 4000, discount: null},
      {name: '오므라이스', price: 5000, discount: {price: 4200, label: '점심 할인'}},
    ],
  },
  {
    id: 'fc-noodle', name: '면사랑', desc: '칼국수부터 짬뽕까지, 면 요리 전문',
    category: '면류', representative: '해물칼국수', hours: '10:30 ~ 19:30',
    menu: [
      {name: '해물칼국수', price: 5500, discount: null, popular: true},
      {name: '짬뽕', price: 6000, discount: {price: 5000, label: '오픈 기념'}},
      {name: '비빔국수', price: 4500, discount: null},
      {name: '수제비', price: 5000, discount: null},
      {name: '만두국', price: 5000, discount: null},
    ],
  },
  {
    id: 'fc-chicken', name: '치킨플러스', desc: '바삭한 치킨과 사이드 메뉴',
    category: '치킨', representative: '후라이드치킨', hours: '11:00 ~ 20:30',
    menu: [
      {name: '후라이드치킨', price: 8000, discount: {price: 7000, label: '학생 특가'}, popular: true},
      {name: '양념치킨', price: 8500, discount: null},
      {name: '치킨텐더', price: 5000, discount: null},
      {name: '치즈볼', price: 3000, discount: null},
      {name: '감자튀김', price: 2500, discount: {price: 2000, label: '사이드 할인'}},
    ],
  },
  {
    id: 'fc-donkatsu', name: '바삭돈까스', desc: '수제 돈까스 전문점',
    category: '돈까스', representative: '등심돈까스', hours: '10:30 ~ 20:00',
    menu: [
      {name: '등심돈까스', price: 5500, discount: null, popular: true},
      {name: '치즈돈까스', price: 6500, discount: {price: 5800, label: '인기 메뉴 할인'}, popular: true},
      {name: '생선까스', price: 5000, discount: null},
      {name: '왕돈까스', price: 7000, discount: null},
      {name: '카레돈까스', price: 6000, discount: {price: 5500, label: '학생 할인'}},
      {name: '미니돈까스 + 우동 세트', price: 6500, discount: null},
    ],
  },
  {
    id: 'fc-pizza', name: '화덕피자', desc: '갓 구운 화덕피자와 파스타',
    category: '양식', representative: '마르게리타 피자', hours: '10:00 ~ 20:30',
    menu: [
      {name: '마르게리타 피자', price: 6500, discount: null, popular: true},
      {name: '페퍼로니 피자', price: 7000, discount: {price: 6000, label: '베스트 할인'}},
      {name: '크림파스타', price: 5500, discount: null},
      {name: '토마토파스타', price: 5500, discount: null, popular: true},
      {name: '갈릭브레드', price: 2500, discount: {price: 2000, label: '세트 추가 시'}},
    ],
  },
  {
    id: 'fc-bob', name: '정든밥상', desc: '집밥 느낌 그대로, 한식 백반 전문',
    category: '백반', representative: '된장찌개 백반', hours: '11:00 ~ 19:00',
    menu: [
      {name: '된장찌개 백반', price: 4500, discount: null, popular: true},
      {name: '김치찌개 백반', price: 4500, discount: null},
      {name: '제육백반', price: 5000, discount: {price: 4500, label: '오늘의 추천'}},
      {name: '순두부찌개 백반', price: 4500, discount: null},
      {name: '불고기 백반', price: 5500, discount: null, popular: true},
    ],
  },
  {
    id: 'fc-coffee', name: '캠퍼스 카페', desc: '커피와 음료, 간단한 베이커리',
    category: '카페', representative: '아메리카노', hours: '08:00 ~ 21:00',
    menu: [
      {name: '아메리카노', price: 2000, discount: null, popular: true},
      {name: '카페라떼', price: 2800, discount: null},
      {name: '바닐라라떼', price: 3200, discount: {price: 2700, label: '이달의 음료'}},
      {name: '크로플', price: 3000, discount: null},
      {name: '머핀', price: 2500, discount: null},
    ],
  },
];
