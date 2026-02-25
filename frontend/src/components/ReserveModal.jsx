import {useState} from 'react';
import {useAuth} from '../context/AuthContext.jsx';
import {createReservation} from '../data/api.js';

const ROLE_LABELS = {
  ROLE_STUDENT: '학생', ROLE_PROFESSOR: '교수', ROLE_ADMIN: '관리자',
};

export function ReserveModal({buildingName, room, selectedDay, startHour, onClose, onReserved}) {
  const {currentUser} = useAuth();
  const now = new Date();
  const y = now.getFullYear();
  const m = now.getMonth();
  const dateStr = `${y}-${String(m + 1).padStart(2, '0')}-${String(selectedDay).padStart(2, '0')}`;

  const [start, setStart] = useState(startHour);
  const [end, setEnd] = useState(startHour + 1);
  const [purpose, setPurpose] = useState('');
  const [error, setError] = useState(false);

  async function handleSubmit() {
    if (end <= start) {
      alert('종료 시간은 시작 시간 이후여야 합니다.');
      return;
    }
    if (!purpose.trim()) {
      alert('대여 목적을 입력해주세요.');
      return;
    }

    try {
      // 백엔드 RequestReservation DTO에 맞춘 요청 형식
      // startTime/endTime: ISO 8601 LocalDateTime (e.g. "2026-02-25T09:00:00")
      // roomId: DB PK (Long) — mock에서는 문자열 키였으나 DB 전환 후 숫자
      // clubId: 동아리 예약 기능은 추후 구현 (현재 null)
      await createReservation({
        roomId: room.id,
        startTime: `${dateStr}T${String(start).padStart(2, '0')}:00:00`,
        endTime: `${dateStr}T${String(end).padStart(2, '0')}:00:00`,
        purpose: purpose,
        clubId: null,
      });
      onClose();
      if (onReserved) onReserved();
      alert(`예약 신청이 완료되었습니다!\n\n` + `신청자: ${currentUser.name}(${currentUser.role})\n` + `호실: ${room.name}\n` + `시간: ${String(start).padStart(2, '0')}:00 ~ ${String(end).padStart(2, '0')}:00\n` + `목적: ${purpose}\n\n` + `관리자 승인 후 확정됩니다.`);
    } catch (e) {
      setError(true);
    }
  }

  return (<div className="modal-bg show" onClick={e => {
    if (e.target === e.currentTarget) onClose();
  }}>
    <div className="modal-box">
      <div className="modal-hd">
        <button className="modal-close" onClick={onClose}>✕</button>
        <h3>시설 예약 신청</h3>
        <div className="modal-sub">{buildingName} · {room.name} · {y}년 {m + 1}월 {selectedDay}일</div>
      </div>
      <div className="modal-bd">
        <div className="mb-3">
          <label className="form-label">예약 주체</label>
          <input type="text" className="form-control"
                 value={currentUser ? `${currentUser.name}(${ROLE_LABELS[currentUser.role] || currentUser.role})` : ''} readOnly
                 style={{background: '#F7FAFC'}}/>
        </div>
        <div className="row mb-3">
          <div className="col-6">
            <label className="form-label">이용 시작</label>
            <select className="form-select" value={start} onChange={e => {
              setStart(Number(e.target.value));
              setError(false);
            }}>
              {Array.from({length: 13}, (_, i) => i + 9).map(h => (
                  <option key={h} value={h}>{String(h).padStart(2, '0')}:00</option>))}
            </select>
          </div>
          <div className="col-6">
            <label className="form-label">이용 종료</label>
            <select className="form-select" value={end} onChange={e => {
              setEnd(Number(e.target.value));
              setError(false);
            }}>
              {Array.from({length: 13}, (_, i) => i + 10).map(h => (
                  <option key={h} value={h}>{String(h).padStart(2, '0')}:00</option>))}
            </select>
          </div>
        </div>
        <div className="mb-3">
          <label className="form-label">대여 목적</label>
          <textarea className="form-control" rows="3" placeholder="시설 대여 목적을 간단히 입력하세요" value={purpose}
                    onChange={e => setPurpose(e.target.value)}/>
        </div>
        <div className="alert alert-info py-2" style={{fontSize: '0.82rem'}}>ℹ️ 예약 신청 후 관리자 승인이 필요합니다.</div>
        {error && <div className="alert alert-danger py-2" style={{fontSize: '0.82rem'}}>⚠️ 선택한 시간이 기존 예약과 겹칩니다.</div>}
      </div>
      <div className="modal-ft">
        <button className="btn btn-outline-secondary btn-sm" onClick={onClose}>취소</button>
        <button className="btn btn-primary btn-sm" onClick={handleSubmit}>예약 신청</button>
      </div>
    </div>
  </div>);
}
