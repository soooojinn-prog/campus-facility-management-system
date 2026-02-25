// 상담 예약 페이지 — /counseling
// 3단 레이아웃: 부서+상담사 | 캘린더 | 타임라인
// 행정동(admin) 건물 클릭 시 이동
//
// API 의존:
//   GET  /api/counseling/counselors?dept={dept}    — 상담사 목록 (공개)
//   GET  /api/counseling/slots?counselorId=&date=  — 예약 현황 (공개)
//   POST /api/counseling/reservations              — 예약 신청 (인증)
import {useEffect, useState} from 'react';
import {useAuth} from '../context/AuthContext.jsx';
import {createCounselingReservation, fetchCounselors, fetchCounselingSlots} from '../data/api.js';

const DEPARTMENTS = [
  {key: 'ACADEMIC', label: '교무처', sub: '학사상담'},
  {key: 'STUDENT', label: '학생처', sub: '심리상담'},
  {key: 'CAREER', label: '취업지원센터', sub: '진로상담'},
];

const MONTH_NAMES = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];

export function CounselingPage() {
  const {currentUser} = useAuth();
  const [dept, setDept] = useState('ACADEMIC');
  const [counselors, setCounselors] = useState([]);
  const [selectedCounselor, setSelectedCounselor] = useState(null);
  const [selectedDay, setSelectedDay] = useState(new Date().getDate());
  const [slots, setSlots] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [modalSlot, setModalSlot] = useState(null);

  // 부서 변경 → 상담사 목록 조회
  useEffect(() => {
    fetchCounselors(dept)
      .then(list => {
        setCounselors(list);
        setSelectedCounselor(list.length > 0 ? list[0] : null);
      })
      .catch(() => setCounselors([]));
  }, [dept]);

  // 상담사/날짜 변경 → 예약 현황 조회
  useEffect(() => {
    if (!selectedCounselor) { setSlots([]); return; }
    const dateStr = buildDateStr(selectedDay);
    fetchCounselingSlots(selectedCounselor.id, dateStr)
      .then(setSlots)
      .catch(() => setSlots([]));
  }, [selectedCounselor, selectedDay]);

  function buildDateStr(day) {
    const now = new Date();
    const y = now.getFullYear();
    const m = String(now.getMonth() + 1).padStart(2, '0');
    const d = String(day).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  function handleEmptySlotClick(hour) {
    if (!currentUser) { alert('로그인이 필요합니다.'); return; }
    setModalSlot({hour});
    setShowModal(true);
  }

  function handleReservationCreated() {
    setShowModal(false);
    // 예약 현황 재조회
    if (selectedCounselor) {
      const dateStr = buildDateStr(selectedDay);
      fetchCounselingSlots(selectedCounselor.id, dateStr).then(setSlots).catch(() => {});
    }
  }

  return (
    <div>
      {/* 상단 정보 */}
      <div className="store-info-area">
        <div className="container">
          <div className="breadcrumb-nav">
            <a href="/">캠퍼스 맵</a><span className="sep">/</span><span className="current">행정동 · 상담 예약</span>
          </div>
          <h2 className="store-name" style={{marginTop: '1rem'}}>행정동 상담 예약</h2>
          <p style={{color: '#718096', fontSize: '.9rem', marginTop: '.5rem'}}>부서를 선택하고 상담사와 일정을 정하여 예약하세요.</p>
        </div>
      </div>

      {/* 부서 탭 */}
      <div className="building-tabs">
        {DEPARTMENTS.map(d => (
          <button key={d.key} className={dept === d.key ? 'active' : ''} onClick={() => setDept(d.key)}>
            {d.label} <span style={{fontSize: '.75rem', opacity: .7}}>· {d.sub}</span>
          </button>
        ))}
      </div>

      {/* 3단 레이아웃 */}
      <div className="counsel-layout">
        {/* 좌측: 상담사 목록 */}
        <div className="counsel-sidebar">
          <div className="counsel-sidebar-header">상담사 목록</div>
          {counselors.map(c => (
            <div key={c.id}
                 className={`counsel-counselor-item${selectedCounselor?.id === c.id ? ' active' : ''}`}
                 onClick={() => setSelectedCounselor(c)}>
              <div className="counsel-counselor-name">{c.name}</div>
              <div className="counsel-counselor-pos">{c.position}</div>
              <div className="counsel-counselor-spec">{c.specialization}</div>
            </div>
          ))}
          {counselors.length === 0 && <div className="counsel-empty">등록된 상담사가 없습니다.</div>}
        </div>

        {/* 중앙: 미니 캘린더 */}
        <div className="counsel-calendar-side">
          <CounselCalendar selectedDay={selectedDay} onDayClick={setSelectedDay}/>
        </div>

        {/* 우측: 타임라인 */}
        <div className="counsel-timeline-side">
          <div className="rv-timeline-header">
            <h4>{selectedCounselor ? selectedCounselor.name + ' 상담사' : '상담사를 선택하세요'}</h4>
            <div className="rv-date-sub">{buildDateStr(selectedDay)}</div>
          </div>
          <CounselTimeline slots={slots} onEmptyClick={handleEmptySlotClick}/>
        </div>
      </div>

      {/* 예약 모달 */}
      {showModal && modalSlot && selectedCounselor && (
        <ReserveModal
          counselor={selectedCounselor}
          date={buildDateStr(selectedDay)}
          hour={modalSlot.hour}
          onClose={() => setShowModal(false)}
          onCreated={handleReservationCreated}
        />
      )}
    </div>
  );
}

// 상담용 미니 캘린더 — 색상 시드 없이 순수 날짜 선택용
function CounselCalendar({selectedDay, onDayClick}) {
  const now = new Date();
  const y = now.getFullYear();
  const m = now.getMonth();
  const today = now.getDate();
  const firstDay = new Date(y, m, 1).getDay();
  const daysInMonth = new Date(y, m + 1, 0).getDate();
  const daysInPrev = new Date(y, m, 0).getDate();

  const weeks = [];
  let week = [];

  for (let i = 0; i < firstDay; i++) {
    week.push({day: daysInPrev - firstDay + 1 + i, otherMonth: true});
  }
  for (let d = 1; d <= daysInMonth; d++) {
    if (week.length === 7) { weeks.push(week); week = []; }
    week.push({
      day: d, otherMonth: false, isToday: d === today, isSelected: d === selectedDay,
      isSunday: week.length === 0, isSaturday: week.length === 6,
    });
  }
  let nd = 1;
  while (week.length < 7) { week.push({day: nd++, otherMonth: true}); }
  weeks.push(week);

  return (
    <div>
      <div className="cal-header">
        <span className="cal-month-num">{m + 1}</span>
        <span className="cal-month-name">{MONTH_NAMES[m]}</span>
        <span className="cal-year">{y}</span>
      </div>
      <table className="cal-table">
        <thead><tr><th>S</th><th>M</th><th>T</th><th>W</th><th>T</th><th>F</th><th>S</th></tr></thead>
        <tbody>
          {weeks.map((wk, wi) => (
            <tr key={wi}>
              {wk.map((cell, ci) => {
                if (cell.otherMonth) return <td key={ci} className="other-month"><span className="day-num">{cell.day}</span></td>;
                const cls = [cell.isSunday && 'sunday', cell.isSaturday && 'saturday', cell.isToday && 'today', cell.isSelected && 'selected'].filter(Boolean).join(' ');
                return (
                  <td key={ci} className={cls} style={{cursor: 'pointer'}} onClick={() => onDayClick(cell.day)}>
                    <span className="day-num">{cell.day}</span>
                  </td>
                );
              })}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

// 타임라인 — 09:00~17:00, 30분 단위
function CounselTimeline({slots, onEmptyClick}) {
  const timeSlots = [];
  for (let h = 9; h < 17; h++) {
    timeSlots.push({hour: h, min: 0, label: `${String(h).padStart(2, '0')}:00`});
    timeSlots.push({hour: h, min: 30, label: `${String(h).padStart(2, '0')}:30`});
  }

  function getSlotForTime(hour, min) {
    const t = `${String(hour).padStart(2, '0')}:${String(min).padStart(2, '0')}`;
    return slots.find(s => {
      const start = s.startTime?.substring(0, 5);
      const end = s.endTime?.substring(0, 5);
      return start <= t && t < end;
    });
  }

  return (
    <div className="timeline">
      {timeSlots.map(ts => {
        const booked = getSlotForTime(ts.hour, ts.min);
        const startLabel = booked?.startTime?.substring(0, 5);
        const isStart = booked && startLabel === ts.label;

        if (booked && !isStart) return null; // 예약 중간 슬롯은 스킵

        if (booked && isStart) {
          const statusCls = booked.status === 'APPROVED' ? 'approved' : 'pending';
          return (
            <div className="tl-slot" key={ts.label}>
              <div className="tl-time">{ts.label}</div>
              <div className="tl-content">
                <div className={`tl-booked ${statusCls}`}>
                  <div className="tl-title">{booked.topic || '상담 예약'}</div>
                  <div className="tl-detail">{booked.startTime?.substring(0, 5)} ~ {booked.endTime?.substring(0, 5)}</div>
                  <div className="tl-status-tag">{booked.status === 'APPROVED' ? '승인됨' : '대기 중'}</div>
                </div>
              </div>
            </div>
          );
        }

        return (
          <div className="tl-slot" key={ts.label}>
            <div className="tl-time">{ts.label}</div>
            <div className="tl-content">
              <div className="tl-empty" onClick={() => onEmptyClick(ts.hour, ts.min)}>+ 예약 가능</div>
            </div>
          </div>
        );
      })}
    </div>
  );
}

// 상담 예약 모달
function ReserveModal({counselor, date, hour, onClose, onCreated}) {
  const [startHour, setStartHour] = useState(hour);
  const [startMin, setStartMin] = useState(0);
  const [endHour, setEndHour] = useState(hour);
  const [endMin, setEndMin] = useState(30);
  const [topic, setTopic] = useState('');
  const [memo, setMemo] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    if (!topic.trim()) { setError('상담 주제를 입력해주세요.'); return; }
    const startTime = `${String(startHour).padStart(2, '0')}:${String(startMin).padStart(2, '0')}:00`;
    const endTime = `${String(endHour).padStart(2, '0')}:${String(endMin).padStart(2, '0')}:00`;
    if (startTime >= endTime) { setError('종료 시간이 시작 시간보다 커야 합니다.'); return; }
    setSubmitting(true);
    try {
      await createCounselingReservation({counselorId: counselor.id, date, startTime, endTime, topic, memo});
      alert('상담 예약이 신청되었습니다.');
      onCreated();
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  const hourOptions = [];
  for (let h = 9; h <= 17; h++) hourOptions.push(h);

  return (
    <div className="modal-bg show" onClick={onClose}>
      <div className="modal-box" onClick={e => e.stopPropagation()}>
        <div className="modal-hd">
          <h3>상담 예약 신청</h3>
          <div className="modal-sub">{counselor.name} · {date}</div>
          <button className="modal-close" onClick={onClose}>X</button>
        </div>
        <form onSubmit={handleSubmit}>
          <div className="modal-bd">
            <div className="mb-3">
              <label className="form-label">상담사</label>
              <input type="text" className="form-control" value={`${counselor.name} (${counselor.position})`} readOnly/>
            </div>
            <div className="mb-3" style={{display: 'flex', gap: '12px'}}>
              <div style={{flex: 1}}>
                <label className="form-label">시작 시간</label>
                <div style={{display: 'flex', gap: '4px'}}>
                  <select className="form-select" value={startHour} onChange={e => setStartHour(Number(e.target.value))}>
                    {hourOptions.filter(h => h < 17).map(h => <option key={h} value={h}>{String(h).padStart(2, '0')}시</option>)}
                  </select>
                  <select className="form-select" value={startMin} onChange={e => setStartMin(Number(e.target.value))}>
                    <option value={0}>00분</option>
                    <option value={30}>30분</option>
                  </select>
                </div>
              </div>
              <div style={{flex: 1}}>
                <label className="form-label">종료 시간</label>
                <div style={{display: 'flex', gap: '4px'}}>
                  <select className="form-select" value={endHour} onChange={e => setEndHour(Number(e.target.value))}>
                    {hourOptions.filter(h => h >= 9).map(h => <option key={h} value={h}>{String(h).padStart(2, '0')}시</option>)}
                  </select>
                  <select className="form-select" value={endMin} onChange={e => setEndMin(Number(e.target.value))}>
                    <option value={0}>00분</option>
                    <option value={30}>30분</option>
                  </select>
                </div>
              </div>
            </div>
            <div className="mb-3">
              <label className="form-label">상담 주제 <span className="text-danger">*</span></label>
              <input type="text" className="form-control" value={topic} onChange={e => setTopic(e.target.value)} placeholder="예: 수강신청 변경 문의"/>
            </div>
            <div className="mb-3">
              <label className="form-label">메모</label>
              <textarea className="form-control" rows="3" value={memo} onChange={e => setMemo(e.target.value)} placeholder="추가로 전달할 내용이 있으면 입력하세요"/>
            </div>
            {error && <div className="alert alert-danger py-2">{error}</div>}
          </div>
          <div className="modal-ft">
            <button type="button" className="btn btn-secondary" onClick={onClose}>취소</button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>{submitting ? '신청 중...' : '예약 신청'}</button>
          </div>
        </form>
      </div>
    </div>
  );
}
