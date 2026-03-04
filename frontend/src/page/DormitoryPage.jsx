import {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {AuthModal} from '../components/AuthModal.jsx';
import {useAuth} from '../context/AuthContext.jsx';
import {applyDorm, fetchDormRooms} from '../data/api.js';

/// 기숙사 페이지 — 캠퍼스 맵에서 기숙사 클릭 시 이동 (customRoute: '/dormitory')
/// Step 1: 성별 선택 (남자기숙사 / 여자기숙사)
/// Step 2: 층별 호실 보기 (좌측 층 목록 + 우측 호실 그리드)
/// Step 3: 신청 모달 (학기, 기간, 같이 신청 시 친구 학번)
export function DormitoryPage() {
  const navigate = useNavigate();
  const {currentUser} = useAuth();
  const [showAuth, setShowAuth] = useState(false);

  // Step 상태: null → 성별 선택, 'MALE'/'FEMALE' → 호실 보기
  const [selectedGender, setSelectedGender] = useState(null);
  const [floors, setFloors] = useState([]);
  const [selectedFloor, setSelectedFloor] = useState(1);
  const [loading, setLoading] = useState(false);

  // 신청 모달
  const [applyModal, setApplyModal] = useState(null); // { room, mode: 'solo' | 'together' }

  function handleGenderSelect(gender) {
    if (!currentUser) {
      setShowAuth(true);
      return;
    }
    if (currentUser.gender !== gender) {
      alert('본인의 성별에 맞는 기숙사를 선택해주세요.');
      return;
    }
    setSelectedGender(gender);
  }

  useEffect(() => {
    if (!selectedGender) return;
    setLoading(true);
    fetchDormRooms(selectedGender)
        .then(data => {
          const sorted = [...data].sort((a, b) => b.floor - a.floor);
          setFloors(sorted);
          if (sorted.length > 0) setSelectedFloor(sorted[0].floor);
        })
        .catch(() => alert('호실 정보를 불러오지 못했습니다.'))
        .finally(() => setLoading(false));
  }, [selectedGender]);

  const genderLabel = selectedGender === 'MALE' ? '남자기숙사' : '여자기숙사';
  const currentFloorData = floors.find(f => f.floor === selectedFloor);

  return (<div id="dormitoryView">
    {/* 브레드크럼 */}
    <div className="breadcrumb-nav">
      <div className="container">
        <a href="#" onClick={e => {
          e.preventDefault();
          navigate('/');
        }}>HOME</a>
        <span className="sep">/</span>
        <a href="#" onClick={e => {
          e.preventDefault();
          navigate('/');
        }}>캠퍼스 지도</a>
        <span className="sep">/</span>
        {selectedGender ? (<>
          <a href="#" onClick={e => {
            e.preventDefault();
            setSelectedGender(null);
          }}>기숙사</a>
          <span className="sep">/</span>
          <span className="current">{genderLabel}</span>
        </>) : (<span className="current">기숙사</span>)}
      </div>
    </div>

    {/* 상단 정보 영역 */}
    <div className="store-info-area">
      <div className="container">
        <div className="store-name">
          <span>{selectedGender ? genderLabel : '기숙사'}</span>
          {selectedGender ? <a className="back-link" onClick={() => setSelectedGender(null)}>&larr; 기숙사 선택으로 돌아가기</a> :
              <a className="back-link" onClick={() => navigate('/')}>&larr; 캠퍼스 지도로 돌아가기</a>}
        </div>
        <div className="info-grid">
          <div className="info-col">
            <div className="semester-box">
              <div className="semester-icon">🏠</div>
              <div className="semester-label">시설 안내</div>
              <div className="semester-value">학생 생활관</div>
              <div className="semester-sub">2인 1실 · 5층 건물</div>
            </div>
          </div>
          <div className="info-col">
            <ul className="hours-list">
              <li><span className="hours-icon">🛏️</span> 2인 1실 (남녀 분리)</li>
              <li><span className="hours-icon">📋</span> 학기 단위 또는 1년 단위 신청</li>
              <li><span className="hours-icon">👥</span> 친구와 같이 신청 가능</li>
              <li><span className="hours-icon">ℹ️</span> 신청 후 관리자 승인 필요</li>
            </ul>
          </div>
          <div className="info-col">
            <div className="caf-today-summary">
              <div className="caf-summary-title">기숙사 현황</div>
              <div className="caf-summary-row">
                <span className="caf-summary-icon">🏠</span>
                <span className="caf-summary-type">남자</span>
                <span className="caf-summary-time">5층 × 15실</span>
              </div>
              <div className="caf-summary-row">
                <span className="caf-summary-icon">🏠</span>
                <span className="caf-summary-type">여자</span>
                <span className="caf-summary-time">5층 × 15실</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    {/* 콘텐츠 */}
    {!selectedGender ? (<GenderSelection onSelect={handleGenderSelect}/>) : loading ? (
        <div style={{padding: 40, textAlign: 'center', color: '#718096'}}>로딩 중...</div>) : (
        <div className="floor-guide">
          {/* 좌측: 층 목록 */}
          <div className="floor-list">
            {floors.map(f => {
              const empty = f.rooms.filter(r => r.occupancy === 0).length;
              const partial = f.rooms.filter(r => r.occupancy === 1).length;
              return (<div key={f.floor}
                           className={`floor-item${f.floor === selectedFloor ? ' active' : ''}`}
                           onClick={() => setSelectedFloor(f.floor)}>
                <div className="floor-num">{f.floor}F</div>
                <div>
                  <div style={{fontWeight: 500, fontSize: '0.9rem'}}>{f.floor}층</div>
                  <div className="floor-desc">{f.rooms.length}실</div>
                </div>
                <div className="floor-pin" title="빈방"
                     style={{background: empty > 0 ? '#C6F6D5' : '#FED7D7', color: empty > 0 ? '#276749' : '#C53030'}}>
                  {empty + partial}
                </div>
              </div>);
            })}
          </div>

          {/* 우측: 호실 그리드 */}
          <div className="floor-detail">
            {currentFloorData ? (<>
              <div className="floor-detail-header">
                <h3>{genderLabel} {selectedFloor}층</h3>
                <div className="floor-subtitle">
                  {currentFloorData.rooms.length}실 ·
                  빈방 {currentFloorData.rooms.filter(r => r.occupancy === 0).length} ·
                  1명 {currentFloorData.rooms.filter(r => r.occupancy === 1).length} ·
                  만실 {currentFloorData.rooms.filter(r => r.occupancy === 2).length}
                </div>
              </div>
              <div className="dorm-status-legend">
                <div className="dorm-legend-item">
                  <div className="dorm-legend-dot" style={{background: '#C6F6D5'}}/>
                  빈방 (0/2)
                </div>
                <div className="dorm-legend-item">
                  <div className="dorm-legend-dot" style={{background: '#FEFCBF'}}/>
                  1명 (1/2)
                </div>
                <div className="dorm-legend-item">
                  <div className="dorm-legend-dot" style={{background: '#FED7D7'}}/>
                  만실 (2/2)
                </div>
              </div>
              <div className="dorm-room-grid">
                {currentFloorData.rooms.map(room => (
                    <DormRoomCard key={room.id} room={room} onApply={(mode) => setApplyModal({room, mode})}/>))}
              </div>
            </>) : (<div style={{padding: 40, color: '#718096'}}>층을 선택해주세요.</div>)}
          </div>
        </div>)}

    {/* 신청 모달 */}
    {applyModal && <DormApplyModal
        room={applyModal.room}
        mode={applyModal.mode}
        onClose={() => setApplyModal(null)}
        onSuccess={() => {
          setApplyModal(null);
          // 새로고침
          fetchDormRooms(selectedGender).then(setFloors);
        }}
    />}

    {/* 로그인 모달 */}
    {showAuth && <AuthModal onClose={() => setShowAuth(false)}/>}
  </div>);
}

/// Step 1: 성별 선택 카드
function GenderSelection({onSelect}) {
  const genders = [{key: 'MALE', icon: '🏠', label: '남자기숙사', desc: '남학생 전용\n5층 × 15실 = 75실'}, {
    key: 'FEMALE',
    icon: '🏠',
    label: '여자기숙사',
    desc: '여학생 전용\n5층 × 15실 = 75실',
  }];
  return (<div style={{padding: '60px 0', background: '#FAFBFC', minHeight: 'calc(100vh - 280px)'}}>
    <div className="container" style={{maxWidth: 600, margin: '0 auto', textAlign: 'center'}}>
      <h2 style={{fontSize: '1.5rem', fontWeight: 700, color: '#1A365D', marginBottom: 8}}>기숙사 선택</h2>
      <p style={{color: '#718096', marginBottom: 32, fontSize: '.9rem'}}>본인의 성별에 맞는 기숙사를 선택해주세요.</p>
      <div className="role-cards" style={{justifyContent: 'center', gap: 24}}>
        {genders.map(g => (<div key={g.key}
                                className={`role-card dorm-gender-${g.key.toLowerCase()}`}
                                style={{padding: '32px 24px', maxWidth: 240, cursor: 'pointer'}}
                                onClick={() => onSelect(g.key)}>
          <div className="role-icon" style={{fontSize: '3rem'}}>{g.icon}</div>
          <div className="role-label" style={{fontSize: '1.1rem', marginTop: 8}}>{g.label}</div>
          <div className="role-desc" style={{marginTop: 8}}>
            {g.desc.split('\n').map((line, i) => <span key={i}>{line}<br/></span>)}
          </div>
        </div>))}
      </div>
    </div>
  </div>);
}

/// 호실 카드 — occupancy에 따라 색상/버튼 다름
function DormRoomCard({room, onApply}) {
  const bgColor = room.occupancy === 0 ? '#F0FFF4' : room.occupancy === 1 ? '#FFFFF0' : '#FFF5F5';
  const borderColor = room.occupancy === 0 ? '#C6F6D5' : room.occupancy === 1 ? '#FEFCBF' : '#FED7D7';
  const dotColor = room.occupancy === 0 ? '#48BB78' : room.occupancy === 1 ? '#ED8936' : '#F56565';
  const label = room.occupancy === 0 ? '빈방' : room.occupancy === 1 ? '1명' : '만실';

  return (<div className="dorm-room-card" style={{background: bgColor, borderColor}}>
    <div className="dorm-room-header">
      <div className="dorm-room-number">{room.roomNumber}</div>
      <div className="dorm-room-occ">
            <span className="room-status-dot" style={{
              background: dotColor, display: 'inline-block', width: 8, height: 8, borderRadius: '50%', marginRight: 4,
            }}/>
        {room.occupancy}/2 ({label})
      </div>
    </div>
    {room.occupancy === 1 && room.residentName && (
        <div className="dorm-room-resident">입주자: {room.residentName}</div>)}
    <div className="dorm-room-actions">
      {room.occupancy < 2 && (
          <button className="btn btn-sm btn-primary" onClick={() => onApply('solo')}>신청하기</button>)}
      {room.occupancy === 0 && (
          <button className="btn btn-sm btn-outline-primary" onClick={() => onApply('together')}>같이 신청</button>)}
    </div>
  </div>);
}

/// 신청 모달
function DormApplyModal({room, mode, onClose, onSuccess}) {
  const now = new Date();
  const year = now.getFullYear();
  const semesterOptions = [`${year}-1`, `${year}-2`];

  const [semester, setSemester] = useState(semesterOptions[now.getMonth() <= 6 ? 0 : 1]);
  const [period, setPeriod] = useState('SEMESTER');
  const [partnerNumber, setPartnerNumber] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit() {
    if (mode === 'together' && !partnerNumber.trim()) {
      alert('같이 신청할 친구의 학번을 입력해주세요.');
      return;
    }
    setSubmitting(true);
    try {
      const result = await applyDorm({
        roomId: room.id, semester, period, partnerNumber: mode === 'together' ? partnerNumber.trim() : null,
      });
      alert(result.message);
      onSuccess();
    } catch (e) {
      alert(e.message || '신청에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  }

  return (<div className="modal-bg show" onClick={e => {
    if (e.target === e.currentTarget) onClose();
  }}>
    <div className="modal-box" style={{width: 440}}>
      <div className="modal-hd">
        <button className="modal-close" onClick={onClose}>&times;</button>
        <h3>기숙사 입주 신청</h3>
        <div className="modal-sub">{room.roomNumber}호 · {mode === 'together' ? '같이 신청' : '단독 신청'}</div>
      </div>
      <div className="modal-bd">
        <div className="mb-3">
          <label className="form-label">호실</label>
          <input type="text" className="form-control" value={room.roomNumber} disabled/>
        </div>
        <div className="mb-3">
          <label className="form-label">학기</label>
          <select className="form-select" value={semester} onChange={e => setSemester(e.target.value)}>
            {semesterOptions.map(s => (<option key={s} value={s}>{s.replace('-', '년 ')}학기</option>))}
          </select>
        </div>
        <div className="mb-3">
          <label className="form-label">입주 기간</label>
          <select className="form-select" value={period} onChange={e => setPeriod(e.target.value)}>
            <option value="SEMESTER">한 학기</option>
            <option value="YEAR">1년</option>
          </select>
        </div>
        {mode === 'together' && (<div className="mb-3">
          <label className="form-label">같이 입주할 친구 학번</label>
          <input type="text" className="form-control" placeholder="친구의 학번을 입력하세요"
                 value={partnerNumber} onChange={e => setPartnerNumber(e.target.value)}/>
          <div className="form-text text-muted" style={{fontSize: '.75rem'}}>
            입력한 학번의 학생과 함께 배정됩니다.
          </div>
        </div>)}
        <button className="btn btn-primary w-100" onClick={handleSubmit} disabled={submitting}>
          {submitting ? '신청 중...' : '신청하기'}
        </button>
      </div>
    </div>
  </div>);
}
