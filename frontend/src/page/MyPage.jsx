// 마이페이지 — /mypage
// 좌측 사이드바 탭: 개인정보 / 기숙사 신청
// 비로그인 시 메인('/')으로 리다이렉트
// 레이아웃은 floor-list/floor-detail 패턴 재활용 (mypage-sidebar/mypage-content)
//
// API 의존:
//   GET  /api/users/me          — 프로필 조회 (ProfileTab)
//   PATCH /api/users/me         — 프로필 수정 (ProfileTab)
//   GET  /api/dorms/my          — 기숙사 신청 내역 (DormTab)
//   DELETE /api/dorms/{id}      — 신청 취소 (DormTab)
//   GET  /api/reservations/me   — 강의실 예약 내역 (ReservationTab)
//   DELETE /api/reservations/{id} — 예약 취소 (ReservationTab)
//   GET  /api/buildings/{id}/library/reading-rooms/reservations/me — 열람실 예약 (SeatReservationTab)
//   GET  /api/buildings/{id}/library/study-rooms/reservations/me  — 스터디룸 예약 (StudyReservationTab)
import {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {useAuth} from '../context/AuthContext.jsx';
import {
  cancelCounselingReservation,
  cancelDormApplication,
  cancelReservation,
  cancelSeatReservation,
  cancelStudyRoomReservation,
  fetchMyCounselingReservations,
  fetchMyDormApplications,
  fetchMyProfile,
  fetchMyReservations,
  fetchMySeatReservations,
  fetchMyStudyRoomReservations,
  updateMyProfile,
} from '../data/api.js';

const TABS = [{key: 'profile', label: '개인정보'}, {key: 'dorm', label: '기숙사 신청'}, {
  key: 'reservation',
  label: '강의실 예약',
}, {key: 'counseling', label: '상담 예약'}, {key: 'seatReservation', label: '열람실 예약'}, {
  key: 'studyReservation',
  label: '스터디룸 예약',
}];

const ROLE_LABELS = {ROLE_STUDENT: '학생', ROLE_PROFESSOR: '교수', ROLE_ADMIN: '관리자'};
const GENDER_LABELS = {MALE: '남성', FEMALE: '여성'};
const PERIOD_LABELS = {SEMESTER: '한 학기', YEAR: '1년'};
const STATUS_LABELS = {PENDING: '대기 중', APPROVED: '승인', REJECTED: '거절', CANCELLED: '취소됨'};
const STATUS_CLASSES = {
  PENDING: 'mypage-badge-pending',
  APPROVED: 'mypage-badge-approved',
  REJECTED: 'mypage-badge-rejected',
  CANCELLED: 'mypage-badge-cancelled',
};

export function MyPage() {
  const {currentUser} = useAuth();
  const navigate = useNavigate();
  const [tab, setTab] = useState('profile');

  useEffect(() => {
    if (!currentUser) navigate('/');
  }, [currentUser, navigate]);

  if (!currentUser) return null;

  return (<div className="mypage-layout">
    <div className="mypage-sidebar">
      {TABS.map(t => (<div key={t.key} className={`mypage-sidebar-item${tab === t.key ? ' active' : ''}`}
                           onClick={() => setTab(t.key)}>
        {t.label}
      </div>))}
    </div>
    <div className="mypage-content">
      {tab === 'profile' && <ProfileTab/>}
      {tab === 'dorm' && <DormTab/>}
      {tab === 'reservation' && <ReservationTab/>}
      {tab === 'counseling' && <CounselingTab/>}
      {tab === 'seatReservation' && <SeatReservationTab/>}
      {tab === 'studyReservation' && <StudyReservationTab/>}
    </div>
  </div>);
}

// 개인정보 탭 — 프로필 카드(읽기전용) + 수정 폼
// 수정 시 현재 비밀번호(oldPassword) 필수 입력
// 수정 가능 항목: 새 비밀번호, 이메일, 성별
function ProfileTab() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [email, setEmail] = useState('');
  const [editMsg, setEditMsg] = useState('');
  const [editErr, setEditErr] = useState('');

  useEffect(() => {
    fetchMyProfile()
        .then(data => {
          setProfile(data);
          setEmail(data.email || '');
        })
        .catch(e => setError(e.message))
        .finally(() => setLoading(false));
  }, []);

  async function handleSubmit(e) {
    e.preventDefault();
    setEditMsg('');
    setEditErr('');
    try {
      await updateMyProfile({oldPassword, newPassword: newPassword || null, email});
      setEditMsg('정보가 수정되었습니다.');
      setOldPassword('');
      setNewPassword('');
      const updated = await fetchMyProfile();
      setProfile(updated);
      setEmail(updated.email || '');
    } catch (err) {
      setEditErr(err.message);
    }
  }

  if (loading) return <div className="mypage-loading">로딩 중...</div>;
  if (error) return <div className="alert alert-danger">{error}</div>;
  if (!profile) return null;

  return (<div>
    <div className="mypage-section-header">
      <h3>개인정보</h3>
      <p className="mypage-section-sub">내 계정 정보를 확인하고 수정할 수 있습니다.</p>
    </div>
    <div className="mypage-profile-card">
      <div className="mypage-profile-row"><span className="mypage-label">이름</span><span>{profile.name}</span></div>
      <div className="mypage-profile-row"><span
          className="mypage-label">학번/교번</span><span>{profile.userNumber}</span></div>
      <div className="mypage-profile-row"><span className="mypage-label">이메일</span><span>{profile.email}</span>
      </div>
      <div className="mypage-profile-row"><span
          className="mypage-label">역할</span><span>{ROLE_LABELS[profile.role] || profile.role}</span></div>
      <div className="mypage-profile-row"><span
          className="mypage-label">성별</span><span>{GENDER_LABELS[profile.gender] || '-'}</span></div>
      <div className="mypage-profile-row"><span
          className="mypage-label">가입일</span><span>{profile.createdAt?.substring(0, 10)}</span></div>
    </div>

    <div className="mypage-edit-section">
      <h4>정보 수정</h4>
      <form onSubmit={handleSubmit}>
        <div className="mb-3">
          <label className="form-label">현재 비밀번호 <span className="text-danger">*</span></label>
          <input type="password" className="form-control" value={oldPassword}
                 onChange={e => setOldPassword(e.target.value)} required/>
        </div>
        <div className="mb-3">
          <label className="form-label">새 비밀번호</label>
          <input type="password" className="form-control" value={newPassword}
                 onChange={e => setNewPassword(e.target.value)} placeholder="변경하지 않으려면 비워두세요"/>
        </div>
        <div className="mb-3">
          <label className="form-label">이메일</label>
          <input type="email" className="form-control" value={email} onChange={e => setEmail(e.target.value)}/>
        </div>
        {editMsg && <div className="alert alert-success py-2">{editMsg}</div>}
        {editErr && <div className="alert alert-danger py-2">{editErr}</div>}
        <button type="submit" className="btn btn-primary">수정하기</button>
      </form>
    </div>
  </div>);
}

// 기숙사 신청 내역 탭 — 테이블 형태
// PENDING 상태만 취소 버튼 노출
// 취소 시 서버 DELETE 호출 후, 로컬 상태만 CANCELLED로 변경 (재조회 없음)
function DormTab() {
  const [apps, setApps] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchMyDormApplications()
        .then(setApps)
        .catch(e => setError(e.message))
        .finally(() => setLoading(false));
  }, []);

  async function handleCancel(id) {
    if (!confirm('정말 취소하시겠습니까?')) return;
    try {
      await cancelDormApplication(id);
      setApps(prev => prev.map(a => a.id === id ? {...a, status: 'CANCELLED'} : a));
    } catch (err) {
      alert(err.message);
    }
  }

  if (loading) return <div className="mypage-loading">로딩 중...</div>;
  if (error) return <div className="alert alert-danger">{error}</div>;

  return (<div>
    <div className="mypage-section-header">
      <h3>기숙사 신청 내역</h3>
      <p className="mypage-section-sub">내 기숙사 입주 신청 현황을 확인할 수 있습니다.</p>
    </div>
    {apps.length === 0 ? (<div className="mypage-empty">신청 내역이 없습니다.</div>) : (<div className="table-responsive">
      <table className="table mypage-table">
        <thead>
        <tr>
          <th>호실</th>
          <th>신청인</th>
          <th>학기</th>
          <th>입주 기간</th>
          <th>상태</th>
          <th>신청일</th>
          <th></th>
        </tr>
        </thead>
        <tbody>
        {apps.map(a => (<tr key={a.id}>
          <td>{a.roomNumber}호</td>
          <td>{a.partnerName ? `${a.applicantName}, ${a.partnerName}` : a.applicantName || '-'}</td>
          <td>{a.semester}</td>
          <td>{PERIOD_LABELS[a.period] || a.period}</td>
          <td>
            {a.status === 'REJECTED' && a.rejectReason ?
                <button className={`mypage-badge ${STATUS_CLASSES[a.status] || ''}`}
                        style={{cursor: 'pointer', border: 'none'}}
                        title="클릭하여 거절 사유 확인"
                        onClick={() => alert(`거절 사유:\n${a.rejectReason}`)}>
                  {STATUS_LABELS[a.status]}
                </button> : <span
                    className={`mypage-badge ${STATUS_CLASSES[a.status] || ''}`}>{STATUS_LABELS[a.status] || a.status}</span>}
          </td>
          <td>{a.createdAt?.substring(0, 10)}</td>
          <td>
            {a.status === 'PENDING' && (<button className="btn btn-outline-danger btn-sm"
                                                onClick={() => handleCancel(a.id)}>취소</button>)}
          </td>
        </tr>))}
        </tbody>
      </table>
    </div>)}
  </div>);
}

// 강의실 예약 내역 탭 — 테이블 형태
// PENDING 상태만 취소 버튼 노출
// 취소 시 서버 DELETE 호출 후, 로컬 상태만 CANCELLED로 변경
function ReservationTab() {
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchMyReservations()
        .then(setList)
        .catch(e => setError(e.message))
        .finally(() => setLoading(false));
  }, []);

  async function handleCancel(id) {
    if (!confirm('정말 취소하시겠습니까?')) return;
    try {
      await cancelReservation(id);
      setList(prev => prev.map(r => r.id === id ? {...r, status: 'CANCELLED'} : r));
    } catch (err) {
      alert(err.message);
    }
  }

  function formatTime(dateStr) {
    if (!dateStr) return '-';
    const d = new Date(dateStr);
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    const hh = String(d.getHours()).padStart(2, '0');
    const mi = String(d.getMinutes()).padStart(2, '0');
    return `${mm}/${dd} ${hh}:${mi}`;
  }

  if (loading) return <div className="mypage-loading">로딩 중...</div>;
  if (error) return <div className="alert alert-danger">{error}</div>;

  return (<div>
    <div className="mypage-section-header">
      <h3>강의실 예약 내역</h3>
      <p className="mypage-section-sub">내 강의실 예약 현황을 확인할 수 있습니다.</p>
    </div>
    {list.length === 0 ? (<div className="mypage-empty">신청 내역이 없습니다.</div>) : (<div className="table-responsive">
      <table className="table mypage-table">
        <thead>
        <tr>
          <th>강의실</th>
          <th>시작</th>
          <th>종료</th>
          <th>목적</th>
          <th>동아리</th>
          <th>상태</th>
          <th></th>
        </tr>
        </thead>
        <tbody>
        {list.map(r => (<tr key={r.id}>
          <td>{r.roomCode}</td>
          <td>{formatTime(r.startTime)}</td>
          <td>{formatTime(r.endTime)}</td>
          <td>{r.purpose || '-'}</td>
          <td>{r.clubName || '-'}</td>
          <td>
            {r.status === 'REJECTED' && r.rejectReason ?
                <button className={`mypage-badge ${STATUS_CLASSES[r.status] || ''}`}
                        style={{cursor: 'pointer', border: 'none'}}
                        title="클릭하여 거절 사유 확인"
                        onClick={() => alert(`거절 사유:\n${r.rejectReason}`)}>
                  {STATUS_LABELS[r.status]}
                </button> : <span
                    className={`mypage-badge ${STATUS_CLASSES[r.status] || ''}`}>{STATUS_LABELS[r.status] || r.status}</span>}
          </td>
          <td>
            {r.status === 'PENDING' && (<button className="btn btn-outline-danger btn-sm"
                                                onClick={() => handleCancel(r.id)}>취소</button>)}
          </td>
        </tr>))}
        </tbody>
      </table>
    </div>)}
  </div>);
}

const DEPT_LABELS = {ACADEMIC: '교무처', STUDENT: '학생처', CAREER: '취업지원센터'};

// 상담 예약 내역 탭 — 테이블 형태
// PENDING 상태만 취소 버튼 노출
function CounselingTab() {
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchMyCounselingReservations()
        .then(setList)
        .catch(e => setError(e.message))
        .finally(() => setLoading(false));
  }, []);

  async function handleCancel(id) {
    if (!confirm('정말 취소하시겠습니까?')) return;
    try {
      await cancelCounselingReservation(id);
      setList(prev => prev.map(r => r.id === id ? {...r, status: 'CANCELLED'} : r));
    } catch (err) {
      alert(err.message);
    }
  }

  if (loading) return <div className="mypage-loading">로딩 중...</div>;
  if (error) return <div className="alert alert-danger">{error}</div>;

  return (<div>
    <div className="mypage-section-header">
      <h3>상담 예약 내역</h3>
      <p className="mypage-section-sub">내 상담 예약 현황을 확인할 수 있습니다.</p>
    </div>
    {list.length === 0 ? (<div className="mypage-empty">상담 예약 내역이 없습니다.</div>) : (<div className="table-responsive">
      <table className="table mypage-table">
        <thead>
        <tr>
          <th>상담사</th>
          <th>부서</th>
          <th>날짜</th>
          <th>시간</th>
          <th>주제</th>
          <th>상태</th>
          <th></th>
        </tr>
        </thead>
        <tbody>
        {list.map(r => (<tr key={r.id}>
          <td>{r.counselorName}</td>
          <td>{DEPT_LABELS[r.department] || r.department}</td>
          <td>{r.date}</td>
          <td>{r.startTime?.substring(0, 5)} ~ {r.endTime?.substring(0, 5)}</td>
          <td>{r.topic || '-'}</td>
          <td>
            {r.status === 'REJECTED' && r.rejectReason ?
                <button className={`mypage-badge ${STATUS_CLASSES[r.status] || ''}`}
                        style={{cursor: 'pointer', border: 'none'}}
                        title="클릭하여 거절 사유 확인"
                        onClick={() => alert(`거절 사유:\n${r.rejectReason}`)}>
                  {STATUS_LABELS[r.status]}
                </button> : <span
                    className={`mypage-badge ${STATUS_CLASSES[r.status] || ''}`}>{STATUS_LABELS[r.status] || r.status}</span>}
          </td>
          <td>
            {r.status === 'PENDING' && (<button className="btn btn-outline-danger btn-sm"
                                                onClick={() => handleCancel(r.id)}>취소</button>)}
          </td>
        </tr>))}
        </tbody>
      </table>
    </div>)}
  </div>);
}

// 열람실 예약 내역 탭 — 내가 예약한 좌석 목록 (로그인 기반)
function SeatReservationTab() {
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchMySeatReservations()
        .then(setList)
        .catch(e => setError(e.message))
        .finally(() => setLoading(false));
  }, []);

  async function handleCancel(id) {
    if (!confirm('정말 취소하시겠습니까?')) return;
    try {
      await cancelSeatReservation(id);
      setList(prev => prev.filter(r => r.id !== id));
      alert('열람실 좌석 예약이 취소되었습니다.');
    } catch (err) {
      alert(err.message);
    }
  }

  if (loading) return <div className="mypage-loading">로딩 중...</div>;
  if (error) return <div className="alert alert-danger">{error}</div>;

  return (<div>
    <div className="mypage-section-header">
      <h3>열람실 예약 내역</h3>
      <p className="mypage-section-sub">내 도서관 열람실 좌석 예약 현황입니다.</p>
    </div>
    {list.length === 0 ? (<div className="mypage-empty">열람실 예약 내역이 없습니다.</div>) : (
        <div className="table-responsive">
          <table className="table mypage-table">
            <thead>
            <tr>
              <th>층</th>
              <th>열람실</th>
              <th>좌석 번호</th>
              <th>예약 날짜</th>
              <th></th>
            </tr>
            </thead>
            <tbody>
            {list.map(r => (<tr key={r.id}>
              <td>{r.floor}</td>
              <td>{r.roomName}</td>
              <td>{r.seatNo}번</td>
              <td>{r.date}</td>
              <td>
                <button className="btn btn-outline-danger btn-sm"
                        onClick={() => handleCancel(r.id)}>취소
                </button>
              </td>
            </tr>))}
            </tbody>
          </table>
        </div>)}
  </div>);
}

function StudyReservationTab() {
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchMyStudyRoomReservations()
        .then(setList)
        .catch(e => setError(e.message))
        .finally(() => setLoading(false));
  }, []);

  async function handleCancel(id) {
    if (!confirm('정말 취소하시겠습니까?')) return;
    try {
      await cancelStudyRoomReservation(id);
      setList(prev => prev.filter(r => r.id !== id));
      alert('스터디룸 예약이 취소되었습니다.');
    } catch (err) {
      alert(err.message);
    }
  }

  if (loading) return <div className="mypage-loading">로딩 중...</div>;
  if (error) return <div className="alert alert-danger">{error}</div>;

  return (<div>
    <div className="mypage-section-header">
      <h3>스터디룸 예약 내역</h3>
      <p className="mypage-section-sub">내 도서관 스터디룸 예약 현황입니다.</p>
    </div>
    {list.length === 0 ? (<div className="mypage-empty">스터디룸 예약 내역이 없습니다.</div>) : (
        <div className="table-responsive">
          <table className="table mypage-table">
            <thead>
            <tr>
              <th>층</th>
              <th>호실</th>
              <th>예약 날짜</th>
              <th>시간</th>
              <th></th>
            </tr>
            </thead>
            <tbody>
            {list.map(r => (<tr key={r.id}>
              <td>{r.floor}</td>
              <td>{r.roomName}</td>
              <td>{r.date}</td>
              <td>{String(r.startHour).padStart(2, '0')}:00 ~ {String(r.startHour + 1).padStart(2, '0')}:00</td>
              <td>
                <button className="btn btn-outline-danger btn-sm"
                        onClick={() => handleCancel(r.id)}>취소
                </button>
              </td>
            </tr>))}
            </tbody>
          </table>
        </div>)}
  </div>);
}
