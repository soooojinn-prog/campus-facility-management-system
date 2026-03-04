/// 예약 현황 컴포넌트
/// - 좌측: 층별 호실 목록 (클릭으로 선택)
/// - 중앙: 미니 캘린더 (날짜 선택)
/// - 우측: 시간대별 타임라인 (09:00~21:00, 1시간 단위)
/// - 빈 슬롯 클릭 시 예약 모달 표시 (교수/관리자만 가능)
import {useEffect, useState} from 'react';
import {useAuth} from '../context/AuthContext.jsx';
import {fetchRoomReservations} from '../data/api.js';
import {AuthModal} from './AuthModal.jsx';
import {ReserveModal} from './ReserveModal.jsx';

export function ReservationView({buildingKey, buildingData, jumpToRoom}) {
  const {currentUser} = useAuth();
  const now = new Date();
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [selectedDate, setSelectedDate] = useState(new Date(now.getFullYear(), now.getMonth(), now.getDate()));
  const [reservations, setReservations] = useState([]);
  const [modalInfo, setModalInfo] = useState(null);
  const [showAuth, setShowAuth] = useState(false);

  // 주간 날짜 목록 (오늘 ~ 6일 후)
  const weekDates = Array.from({length: 7}, (_, i) => {
    const d = new Date(now.getFullYear(), now.getMonth(), now.getDate() + i);
    return d;
  });
  const dayNames = ['일', '월', '화', '수', '목', '금', '토'];
  const selectedDay = selectedDate.getDate();

  const floorKeys = Object.keys(buildingData.floors).map(Number).sort((a, b) => a - b);

  useEffect(() => {
    if (jumpToRoom) {
      for (const f of floorKeys) {
        const room = buildingData.floors[f].rooms.find(r => r.id === jumpToRoom);
        if (room) {
          setSelectedRoom(room);
          break;
        }
      }
    }
  }, [jumpToRoom]);

  // 호실+날짜가 바뀔 때마다 해당 예약 데이터를 API에서 불러옴
  useEffect(() => {
    if (!selectedRoom) return;
    loadReservations();
  }, [selectedRoom, selectedDate]);

  function loadReservations() {
    if (!selectedRoom) return;
    const y = selectedDate.getFullYear();
    const m = selectedDate.getMonth();
    const d = selectedDate.getDate();
    const dateStr = `${y}-${String(m + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
    fetchRoomReservations(selectedRoom.id, dateStr).then(data => {
      // API 응답(ResponseReservation)을 타임라인 UI가 사용하는 형식으로 변환
      //   API: { startTime: "2026-02-25T09:00:00", endTime: "...", purpose, userName, status, ... }
      //   타임라인: { start: 9, end: 10, title: "목적", detail: "예약자", status: "pending" }
      const mapped = data.map(r => {
        const startHour = new Date(r.startTime).getHours();
        const endHour = new Date(r.endTime).getHours();
        return {
          start: startHour,
          end: endHour,
          title: r.purpose || '예약',
          detail: r.userName + (r.clubName ? ` (${r.clubName})` : ''),
          status: r.status.toLowerCase(),  // "PENDING" → "pending" (CSS 클래스용)
        };
      });
      setReservations(mapped);
    }).catch(() => setReservations([]));
  }

  function handleEmptySlotClick(h) {
    if (!currentUser) {
      setShowAuth(true);
      return;
    }
    setModalInfo({room: selectedRoom, startHour: h});
  }

  function handleReserved() {
    loadReservations();
  }

  return (<div className="reservation-view">
    <div className="rv-room-list">
      {floorKeys.map(f => {
        const fd = buildingData.floors[f];
        return (<div key={f} className="rv-floor-group">
          <div className="rv-floor-label">{f === 0 ? '야외' : `${f}F`} · {fd.desc}</div>
          {fd.rooms.map(room => (<div
              key={room.id}
              className={`rv-room-item${selectedRoom?.id === room.id ? ' active' : ''}`}
              data-room-id={room.id}
              onClick={() => {
                setSelectedRoom(room);
                setSelectedDate(new Date(now.getFullYear(), now.getMonth(), now.getDate()));
              }}
          >
            <div className="rv-dot room-status-dot available"/>
            <span>{room.name}</span>
          </div>))}
        </div>);
      })}
    </div>
    <div className="rv-main">
      <div className="rv-content">
        <div className="rv-calendar-side">
          <div style={{marginBottom: '1rem'}}>
            <div style={{fontWeight: 600, fontSize: '0.95rem', marginBottom: '0.8rem', color: '#2D3748'}}>날짜 선택</div>
            <div style={{display: 'flex', flexDirection: 'column', gap: '4px'}}>
              {weekDates.map(d => {
                const isToday = d.getDate() === now.getDate() && d.getMonth() === now.getMonth();
                const isSelected = d.getTime() === selectedDate.getTime();
                const isSun = d.getDay() === 0;
                const isSat = d.getDay() === 6;
                return (<div key={d.getTime()}
                             onClick={() => setSelectedDate(d)}
                             style={{
                               display: 'flex',
                               alignItems: 'center',
                               gap: '10px',
                               padding: '8px 12px',
                               borderRadius: '8px',
                               cursor: 'pointer',
                               background: isSelected ? '#3182CE' : 'transparent',
                               color: isSelected ? '#FFF' : isSun ? '#E53E3E' : isSat ? '#3182CE' : '#2D3748',
                               fontWeight: isSelected || isToday ? 600 : 400,
                               fontSize: '0.88rem',
                               transition: 'background 0.15s',
                             }}>
                  <span style={{width: '24px', textAlign: 'center'}}>{dayNames[d.getDay()]}</span>
                  <span>{d.getMonth() + 1}/{d.getDate()}</span>
                  {isToday && <span style={{
                    fontSize: '0.7rem',
                    padding: '1px 6px',
                    borderRadius: '8px',
                    background: isSelected ? 'rgba(255,255,255,0.25)' : '#EBF8FF',
                    color: isSelected ? '#FFF' : '#3182CE',
                  }}>오늘</span>}
                </div>);
              })}
            </div>
          </div>
          <hr className="my-3"/>
          <div style={{fontSize: '0.82rem', color: '#718096'}}>
            <div className="mb-2"><strong style={{color: '#2D3748'}}>안내</strong></div>
            <ul style={{paddingLeft: '1rem', margin: 0, lineHeight: '1.8'}}>
              <li>1시간 단위 예약</li>
              <li>최대 3시간 연속 예약</li>
              <li>오늘부터 7일간 예약 가능</li>
            </ul>
            <hr className="my-2"/>
            <div className="d-flex align-items-center gap-2 mb-1">
              <div style={{
                width: 20, height: 12, background: 'linear-gradient(135deg,#2c5282,#3182ce)', borderRadius: 3,
              }}/>
              <span>승인된 예약</span>
            </div>
            <div className="d-flex align-items-center gap-2 mb-1">
              <div style={{
                width: 20,
                height: 12,
                background: 'repeating-linear-gradient(45deg,#ed8936,#ed8936 4px,#dd6b20 4px,#dd6b20 8px)',
                borderRadius: 3,
              }}/>
              <span>승인 대기</span>
            </div>
            <div className="d-flex align-items-center gap-2">
              <div
                  style={{width: 20, height: 12, background: '#F7FAFC', border: '1px solid #e2e8f0', borderRadius: 3}}/>
              <span>예약 가능 (클릭)</span>
            </div>
          </div>
        </div>
        <div className="rv-timeline-side">
          {!selectedRoom ? (<div className="d-flex align-items-center justify-content-center h-100 text-muted">
            <div className="text-center">
              <div style={{fontSize: '2.5rem', marginBottom: '0.5rem'}}>👈</div>
              <div style={{fontSize: '0.95rem'}}>좌측에서 호실을 선택하세요</div>
            </div>
          </div>) : (<>
            <div className="rv-timeline-header">
              <h4>{selectedRoom.name} <span
                  style={{fontWeight: 400, fontSize: '0.9rem', color: '#718096'}}>· {buildingData.name}</span>
              </h4>
              <div
                  className="rv-date-sub">{selectedDate.getFullYear()}년 {selectedDate.getMonth() + 1}월 {selectedDate.getDate()}일
                ({dayNames[selectedDate.getDay()]})
              </div>
            </div>
            <div className="timeline">
              {Array.from({length: 13}, (_, i) => i + 9).map(h => {
                const res = reservations.find(r => h >= r.start && h < r.end);
                const isStart = res && h === res.start;
                if (res && !isStart) return null;
                return (<div key={h} className="tl-slot">
                  <div className="tl-time">{String(h).padStart(2, '0')}:00</div>
                  <div className="tl-content">
                    {isStart && (<div className={`tl-booked ${res.status}`}
                                      style={res.end - res.start > 1 ? {minHeight: (res.end - res.start) * 52 - 8} : undefined}>
                      <div className="tl-title">{res.title}</div>
                      <div
                          className="tl-detail">{res.detail} · {String(res.start).padStart(2, '0')}:00~{String(res.end).padStart(2, '0')}:00
                      </div>
                      <div className="tl-status-tag">{res.status === 'approved' ? '승인됨' : '승인 대기'}</div>
                    </div>)}
                    {!res && (<div className="tl-empty" onClick={() => handleEmptySlotClick(h)}>+ 예약 가능</div>)}
                  </div>
                </div>);
              })}
            </div>
          </>)}
        </div>
      </div>
    </div>
    {modalInfo && (<ReserveModal
        buildingName={buildingData.name}
        room={modalInfo.room}
        selectedDate={selectedDate}
        startHour={modalInfo.startHour}
        onClose={() => setModalInfo(null)}
        onReserved={handleReserved}
    />)}
    {showAuth && <AuthModal onClose={() => setShowAuth(false)}/>}
  </div>);
}
