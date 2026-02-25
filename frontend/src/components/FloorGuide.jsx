import {useState} from 'react';

/// 층별 안내 컴포넌트
/// - buildingData.floors: { [층번호]: { desc: "강의실 / 세미나실", rooms: [...] } }
/// - room.type: 한글 변환된 유형 (BuildingPage에서 ROOM_TYPE_LABELS 적용 완료)
/// - room.id: DB PK (Long) — 클릭 시 onRoomClick(room.id)로 ReservationView 탭 전환
/// - 모든 호실이 "예약 가능" 배지를 표시 (실시간 예약 상태 표시는 추후 구현)
export function FloorGuide({buildingData, onRoomClick}) {
  const floorKeys = Object.keys(buildingData.floors).map(Number).sort((a, b) => b - a);
  const [activeFloor, setActiveFloor] = useState(floorKeys[0]);

  const floorData = buildingData.floors[activeFloor];

  // room.type(한글)별 그룹화 → 카테고리 섹션으로 표시
  // e.g. { "강의실": [room1, room2], "세미나실": [room3] }
  const categories = {};
  floorData.rooms.forEach(r => {
    const cat = r.type;
    if (!categories[cat]) categories[cat] = [];
    categories[cat].push(r);
  });

  return (<div className="floor-guide">
    <div className="floor-list">
      {floorKeys.map(f => {
        const fd = buildingData.floors[f];
        return (<div key={f} className={`floor-item${f === activeFloor ? ' active' : ''}`}
                     onClick={() => setActiveFloor(f)}>
          <div className="floor-num">{f}F</div>
          <div>
            <div style={{fontWeight: 500, fontSize: '0.9rem'}}>{fd.desc}</div>
            <div className="floor-desc">호실 {fd.rooms.length}개</div>
          </div>
          <div className="floor-pin">{fd.rooms.length}</div>
        </div>);
      })}
    </div>
    <div className="floor-detail">
      <div className="d-flex justify-content-between align-items-end">
        <div className="floor-detail-header flex-grow-1">
          <h3>{activeFloor}F</h3>
          <div className="floor-subtitle">{buildingData.name} · {floorData.desc}</div>
        </div>
      </div>
      {Object.entries(categories).map(([cat, rooms]) => (<div key={cat} className="room-category">
        <div className="room-category-title">{cat}<span className="room-category-count">{rooms.length}</span>
        </div>
        {rooms.map(room => (<div key={room.id} className="room-row" onClick={() => onRoomClick(room.id)}>
          <div className="d-flex align-items-center">
            <div className="room-status-dot available"/>
            <div>
              <div className="room-name">{room.name}</div>
              <div className="room-cap">{room.type} · 수용 {room.capacity}명</div>
            </div>
          </div>
          <span className="badge bg-success" style={{fontSize: '0.72rem'}}>예약 가능</span>
        </div>))}
      </div>))}
    </div>
  </div>);
}
