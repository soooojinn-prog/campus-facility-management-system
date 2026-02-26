// 관리자 페이지 - `/admin`
// 좌측 사이드바 탭: 시설 예약 관리 / 동아리 신청 관리 / 기숙사 신청 관리 /
// 비로그인 시 메인 (`/`)로 리다이렉트
// 레이아웃은 floor-list/floor-detail 패턴 재활용 (admin-sidebar/admin-content)
//
// API 의존:
//   GET /api/admin/clubs?status=: 동아리 개설 신청 목록 조회
//   PATCH /api/admin/clubs/{id}/status: 개설 승인/거절 (status 변경)
//   GET /api/admin/reservations?status=: 시설 예약 전체 목록
//   PATCH /api/admin/reservations/{id}/status: 시설 예약 승인/거절

import {useCallback, useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {useAuth} from '../context/AuthContext.jsx';
import {
  fetchAdminClubs, updateAdminClubStatus,
  fetchAdminReservations, updateAdminReservationStatus,
  fetchAdminDorms, updateAdminDormStatus,
} from '../data/api.js';

const TABS = [
  {key: 'clubs', label: '동아리 신청 관리'},
  {key: 'reservations', label: '시설 예약 관리'},
  {key: 'dorms', label: '기숙사 신청 관리'},
];

const STATUS_CLASSES = {
  APPROVED: 'mypage-badge-approved',
  CANCELLED: 'mypage-badge-cancelled',
  PENDING: 'mypage-badge-pending',
  REJECTED: 'mypage-badge-rejected',
};

const STATUS_MAP = {
  APPROVED: '승인',
  PENDING: '대기 중',
  REJECTED: '거절',
  CANCELLED: '취소',
};

// ############ 공통 UI ############

function AdminActionModal({title, children, onClose, onSubmit, submitting}) {
  return (
      <div className="modal-bg show" onClick={e => e.target === e.currentTarget && onClose()}>
        <div className="modal-box">
          <div className="modal-hd">
            <button className="modal-close" onClick={onClose}>✕</button>
            <h3>{title}</h3>
          </div>
          <form onSubmit={onSubmit} className="modal-bd">
            {children}
            <div className="modal-ft d-flex justify-content-end gap-2 mt-4">
              <button type="button" className="btn btn-secondary" onClick={onClose}>취소</button>
              <button type="submit" className="btn btn-primary"
                      disabled={submitting}>{submitting ? '처리 중...' : '확인'}</button>
            </div>
          </form>
        </div>
      </div>
  );
}

export function AdminPage() {
  const {currentUser} = useAuth();
  const navigate = useNavigate();
  const [tab, setTab] = useState('clubs');

  useEffect(() => {
    if (!currentUser || currentUser.role !== 'ROLE_ADMIN') navigate('/');
  }, [currentUser, navigate]);

  if (!currentUser || currentUser.role !== 'ROLE_ADMIN') return null;

  return (
      <div className="mypage-layout">
        <div className="mypage-sidebar">
          {TABS.map(t => (
              <div key={t.key} className={`mypage-sidebar-item${tab === t.key ? ' active' : ''}`}
                   onClick={() => setTab(t.key)}>
                {t.label}
              </div>
          ))}
        </div>
        <div className="mypage-content">
          {tab === 'clubs' && <ClubTab/>}
          {tab === 'reservations' && <ReservationTab/>}
          {tab === 'dorms' && <DormTab/>}
        </div>
      </div>
  );
}

// ############ 동아리 탭 ############

function ClubStatusModal({club, onClose, onRefresh}) {
  const [status, setStatus] = useState('APPROVED');
  const [reason, setReason] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);

    try {
      await updateAdminClubStatus(club.id, {status, rejectReason: status === 'REJECTED' ? reason : null});

      alert('처리가 완료되었습니다');
      onRefresh();
      onClose();
    } catch (err) {
      alert(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
      <AdminActionModal title={`${club.name} 신청 처리`} onClose={onClose} onSubmit={handleSubmit} submitting={submitting}>
        <div className="mb-3">
          <label className="form-label">소개문</label>
          <textarea className="form-control" rows={5} value={club.description} readOnly
                    style={{background: '#F7FAFC'}}/>
        </div>
        <div className="mb-3">
          <label className="form-label d-block">결정</label>
          <div className="btn-group w-100">
            <input type="radio" className="btn-check" name="st" id="st_app" value="APPROVED"
                   checked={status === 'APPROVED'} onChange={e => setStatus(e.target.value)}/>
            <label className="btn btn-outline-success" htmlFor="st_app">승인</label>
            <input type="radio" className="btn-check" name="st" id="st_rej" value="REJECTED"
                   checked={status === 'REJECTED'} onChange={e => setStatus(e.target.value)}/>
            <label className="btn btn-outline-danger" htmlFor="st_rej">거절</label>
          </div>
        </div>
        {status === 'REJECTED' && (
            <textarea className="form-control" placeholder="거절 사유를 입력하세요" value={reason}
                      onChange={e => setReason(e.target.value)} required/>
        )}
      </AdminActionModal>
  );
}

// 동아리 탭
function ClubTab() {
  const [apps, setApps] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setApps(await fetchAdminClubs());
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    // noinspection JSIgnoredPromiseFromCall
    load();
  }, [load]);

  if (loading) return <div className="mypage-loading">데이터를 불러오는 중...</div>;

  return (
      <div>
        <div className="mypage-section-header">
          <h3>동아리 신청 관리</h3>
          <p className="mypage-section-sub">신규 동아리 개설 신청을 심사합니다</p>
        </div>
        <table className="table mypage-table">
          <thead>
          <tr>
            <th>동아리명</th>
            <th>회장</th>
            <th>식별자(Slug)</th>
            <th>신청일</th>
            <th>관리</th>
          </tr>
          </thead>
          <tbody>
          {apps.map(a => (
              <tr key={a.id}>
                <td>{a.name}</td>
                <td>{a.president || a.presidentName}</td>
                <td><code>{a.slug}</code></td>
                <td>{a.createdAt?.split('T')[0]}</td>
                <td>
                  <button className="btn btn-sm btn-outline-primary" onClick={() => setSelected(a)}>처리</button>
                </td>
              </tr>
          ))}
          </tbody>
          {selected && <ClubStatusModal club={selected} onClose={() => setSelected(null)} onRefresh={load}/>}
        </table>
      </div>
  );
}

// ############ 시설 예약 탭 ############

function ReservationStatusModal({res, onClose, onRefresh}) {
  const [status, setStatus] = useState('APPROVED');
  const [reason, setReason] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    try {
      await updateAdminReservationStatus(res.id, {status, rejectReason: status === 'REJECTED' ? reason : null});
      onRefresh();
      onClose();
    } catch (err) {
      alert(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
      <AdminActionModal title="시설 예약 심사" onClose={onClose} onSubmit={handleSubmit} submitting={submitting}>
        <div className="p-3 mb-3 border rounded bg-light">
          <strong>장소:</strong> {res.buildingName} {res.roomCode}<br/>
          <strong>일시:</strong> {res.startTime?.replace('T', ' ')} ~ {res.endTime?.split('T')[1]}<br/>
          <strong>목적:</strong> {res.purpose}
        </div>
        <div className="mb-3">
          <label className="form-label d-block">승인 여부</label>
          <select className="form-select" value={status} onChange={e => setStatus(e.target.value)}>
            <option value="APPROVED">승인</option>
            <option value="REJECTED">거절</option>
          </select>
        </div>
        {status === 'REJECTED' && <textarea className="form-control" placeholder="거절 사유" value={reason}
                                            onChange={e => setReason(e.target.value)} required/>}
      </AdminActionModal>
  );
}

function ReservationTab() {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState(null);

  const load = useCallback(async () => {
    try {
      setData(await fetchAdminReservations());
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    // noinspection JSIgnoredPromiseFromCall
    load();
  }, [load]);

  return (
      <div>
        <div className="mypage-section-header">
          <h3>시설 예약 관리</h3>
          <p>시설 대관 신청 목록입니다.</p>
        </div>
        {loading ? <div className="mypage-loading">데이터를 불러오는 중...</div> : <table className="table mypage-table">
          <thead>
          <tr>
            <th>장소</th>
            <th>예약자</th>
            <th>일시</th>
            <th>상태</th>
            <th>관리</th>
          </tr>
          </thead>
          <tbody>
          {data.map(r => (
              <tr key={r.id}>
                <td>{r.buildingName} {r.roomCode}</td>
                <td>{r.userName} {r.clubName && <small className="text-muted">({r.clubName})</small>}</td>
                <td>{r.startTime?.substring(5, 16).replace('T', ' ')}</td>
                <td><span className={`mypage-badge ${STATUS_CLASSES[r.status]}`}>{STATUS_MAP[r.status]}</span></td>
                <td>
                  <button className="btn btn-sm btn-outline-primary" onClick={() => setSelected(r)}>처리</button>
                </td>
              </tr>
          ))}
          </tbody>
        </table>}
        {selected && <ReservationStatusModal res={selected} onClose={() => setSelected(null)} onRefresh={load}/>}
      </div>
  );
}

// ############ 기숙사 탭 ############

function DormStatusModal({app, onClose, onRefresh}) {
  const [status, setStatus] = useState('APPROVED');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    try {
      await updateAdminDormStatus(app.id, {status});
      onRefresh();
      onClose();
    } catch {
      alert('실패');
    } finally {
      setSubmitting(false);
    }
  }

  return (
      <AdminActionModal title="기숙사 신청 심사" onClose={onClose} onSubmit={handleSubmit} submitting={submitting}>
        <div className="mb-3">
          <p><strong>신청자:</strong> {app.userName || '학부생'}</p>
          <p><strong>호실:</strong> {app.roomNumber} ({app.period})</p>
        </div>
        <select className="form-select" value={status} onChange={e => setStatus(e.target.value)}>
          <option value="APPROVED">승인</option>
          <option value="REJECTED">거절</option>
        </select>
      </AdminActionModal>
  );
}

function DormTab() {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState(null);

  const load = useCallback(async () => {
    try {
      setData(await fetchAdminDorms());
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  return (
      <div>
        <div className="mypage-section-header">
          <h3>기숙사 신청 관리</h3>
          <p>기숙사 입주 신청을 관리합니다.</p>
        </div>
        {loading ? <div className="mypage-loading">데이터를 불러오는 중...</div> : <table className="table mypage-table">
          <thead>
          <tr>
            <th>호실</th>
            <th>기간</th>
            <th>룸메이트</th>
            <th>신청일</th>
            <th>관리</th>
          </tr>
          </thead>
          <tbody>
          {data.map(d => (
              <tr key={d.id}>
                <td>{d.roomNumber}</td>
                <td>{d.semester} ({d.period})</td>
                <td>{d.partnerName || '없음'}</td>
                <td>{d.createdAt?.split('T')[0]}</td>
                <td>
                  <button className="btn btn-sm btn-outline-primary" onClick={() => setSelected(d)}>처리</button>
                </td>
              </tr>
          ))}
          </tbody>
        </table>}
        {selected && <DormStatusModal app={selected} onClose={() => setSelected(null)} onRefresh={load}/>}
      </div>
  );
}
