// 관리자 페이지 - `/admin`
// 좌측 사이드바 탭: 동아리 신청 관리 / 시설 예약 관리 / 기숙사 신청 관리 / 상담 예약 관리
// 비로그인 시 메인 (`/`)로 리다이렉트
// 레이아웃은 floor-list/floor-detail 패턴 재활용 (admin-sidebar/admin-content)
//
// API 의존:
//   GET /api/admin/clubs?status=: 동아리 개설 신청 목록 조회
//   PATCH /api/admin/clubs/{slug}/status: 개설 승인/거절 (status 변경)
//   GET /api/admin/reservations?status=: 시설 예약 전체 목록
//   PATCH /api/admin/reservations/{id}/status: 시설 예약 승인/거절
//   GET /api/admin/dorms?status=: 기숙사 신청 목록
//   PATCH /api/admin/dorms/{id}/status: 기숙사 신청 승인/거절
//   GET /api/admin/counseling?status=: 상담 예약 목록
//   PATCH /api/admin/counseling/{id}/status: 상담 예약 승인/거절

import {useCallback, useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {useAuth} from '../context/AuthContext.jsx';
import {
  fetchAdminClubs,
  fetchAdminCounseling,
  fetchAdminDorms,
  fetchAdminReservations,
  updateAdminClubStatus,
  updateAdminCounselingStatus,
  updateAdminDormStatus,
  updateAdminReservationStatus,
} from '../data/api.js';

const TABS = [
  {key: 'clubs', label: '동아리 신청 관리'},
  {key: 'reservations', label: '시설 예약 관리'},
  {key: 'dorms', label: '기숙사 신청 관리'},
  {key: 'counseling', label: '상담 예약 관리'},
];

const STATUS_CLASSES = {
  APPROVED: 'mypage-badge-approved',
  CANCELLED: 'mypage-badge-cancelled',
  PENDING: 'mypage-badge-pending',
  REJECTED: 'mypage-badge-rejected',
};

const STATUS_MAP = {
  APPROVED: '승인', PENDING: '대기 중', REJECTED: '거절', CANCELLED: '취소',
};

const DEPT_LABELS = {ACADEMIC: '교무처', STUDENT: '학생처', CAREER: '취업지원센터'};

// ############ 공통 UI ############

function StatusFilter({value, onChange}) {
  return (
      <div className="d-flex gap-2 mb-3">
        {['PENDING', 'APPROVED', 'REJECTED'].map(s => (
            <button
                key={s}
                className={`btn btn-sm ${value === s ? 'btn-primary' : 'btn-outline-secondary'}`}
                onClick={() => onChange(s)}>
              {STATUS_MAP[s]}
            </button>
        ))}
      </div>
  );
}

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
              <div
                  key={t.key}
                  className={`mypage-sidebar-item${tab === t.key ? ' active' : ''}`}
                  onClick={() => setTab(t.key)}>
                {t.label}
              </div>
          ))}
        </div>
        <div className="mypage-content">
          {tab === 'clubs' && <ClubTab/>}
          {tab === 'reservations' && <ReservationTab/>}
          {tab === 'dorms' && <DormTab/>}
          {tab === 'counseling' && <CounselingTab/>}
        </div>
      </div>
  );
}

// ############ 동아리 탭 ############

function ClubStatusModal({club, onClose, onRefresh}) {
  const [status, setStatus] = useState(club.status === 'REJECTED' ? 'REJECTED' : 'APPROVED');
  const [reason, setReason] = useState(club.rejectReason || '');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    try {
      // club.slug 로 전달 — 백엔드 @PathVariable String slug 와 일치
      await updateAdminClubStatus(club.slug, {status, rejectReason: status === 'REJECTED' ? reason : null});
      alert('처리가 완료되었습니다.');
      onRefresh();
      onClose();
    } catch (err) {
      alert(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
      <AdminActionModal title={`${club.name} 신청 처리`} onClose={onClose} onSubmit={handleSubmit}
                        submitting={submitting}>
        <div className="mb-3">
          <label className="form-label">소개문</label>
          <textarea className="form-control" rows={5} value={club.description || ''} readOnly
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

function ClubTab() {
  const [apps, setApps] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState(null);
  const [statusFilter, setStatusFilter] = useState('PENDING');

  const load = useCallback(async (status) => {
    setLoading(true);
    try {
      setApps(await fetchAdminClubs(status));
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load(statusFilter);
  }, [load, statusFilter]);

  return (
      <div>
        <div className="mypage-section-header">
          <h3>동아리 신청 관리</h3>
          <p className="mypage-section-sub">신규 동아리 개설 신청을 심사합니다</p>
        </div>
        <StatusFilter value={statusFilter} onChange={setStatusFilter}/>
        {loading ? (
            <div className="mypage-loading">데이터를 불러오는 중...</div>
        ) : (
        <table className="table mypage-table">
          <thead>
          <tr>
            <th>동아리명</th>
            <th>회장</th>
            <th>식별자(Slug)</th>
            <th>신청일</th>
            <th>상태</th>
            <th>관리</th>
          </tr>
          </thead>
          <tbody>
          {apps.length === 0 ? (
              <tr>
                <td colSpan={6} style={{textAlign: 'center', color: '#A0AEC0'}}>데이터가 없습니다.</td>
              </tr>
          ) : apps.map(a => (
              <tr key={a.id}>
                <td>{a.name}</td>
                <td>{a.presidentName}</td>
                <td><code>{a.slug}</code></td>
                <td>{a.createdAt?.split('T')[0]}</td>
                <td><span className={`mypage-badge ${STATUS_CLASSES[a.status]}`}>{STATUS_MAP[a.status]}</span></td>
                <td>
                  <button className="btn btn-sm btn-outline-primary" onClick={() => setSelected(a)}>처리</button>
                </td>
              </tr>
          ))}
          </tbody>
        </table>
        )}
        {selected && <ClubStatusModal club={selected} onClose={() => setSelected(null)} onRefresh={() => load(statusFilter)}/>}
      </div>
  );
}

// ############ 시설 예약 탭 ############

function ReservationStatusModal({res, onClose, onRefresh}) {
  const [status, setStatus] = useState(res.status === 'REJECTED' ? 'REJECTED' : 'APPROVED');
  const [reason, setReason] = useState(res.rejectReason || '');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    try {
      await updateAdminReservationStatus(res.id, {status, rejectReason: status === 'REJECTED' ? reason : null});
      alert('처리가 완료되었습니다.');
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
          <strong>시작:</strong> {res.startTime?.replace('T', ' ')}<br/>
          <strong>종료:</strong> {res.endTime?.replace('T', ' ')}<br/>
          <strong>목적:</strong> {res.purpose}
        </div>
        <div className="mb-3">
          <label className="form-label d-block">결정</label>
          <div className="btn-group w-100">
            <input type="radio" className="btn-check" name="res_st" id="res_st_app" value="APPROVED"
                   checked={status === 'APPROVED'} onChange={e => setStatus(e.target.value)}/>
            <label className="btn btn-outline-success" htmlFor="res_st_app">승인</label>
            <input type="radio" className="btn-check" name="res_st" id="res_st_rej" value="REJECTED"
                   checked={status === 'REJECTED'} onChange={e => setStatus(e.target.value)}/>
            <label className="btn btn-outline-danger" htmlFor="res_st_rej">거절</label>
          </div>
        </div>
        {status === 'REJECTED' && (
            <textarea className="form-control" placeholder="거절 사유를 입력하세요" value={reason}
                      onChange={e => setReason(e.target.value)} required/>
        )}
      </AdminActionModal>
  );
}

function ReservationTab() {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selected, setSelected] = useState(null);
  const [statusFilter, setStatusFilter] = useState('PENDING');

  const load = useCallback(async (status) => {
    setLoading(true);
    setError('');
    try {
      setData(await fetchAdminReservations(status));
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load(statusFilter);
  }, [load, statusFilter]);

  function formatDateTime(dateStr) {
    if (!dateStr) return '-';
    return dateStr.substring(5, 16).replace('T', ' ');
  }

  return (
      <div>
        <div className="mypage-section-header">
          <h3>시설 예약 관리</h3>
          <p>시설 대관 신청 목록입니다.</p>
        </div>
        <StatusFilter value={statusFilter} onChange={setStatusFilter}/>
        {loading ? (
            <div className="mypage-loading">데이터를 불러오는 중...</div>
        ) : error ? (
            <div className="alert alert-danger">{error}</div>
        ) : (
            <div className="table-responsive">
              <table className="table mypage-table">
                <thead>
                <tr>
                  <th>장소</th>
                  <th>예약자</th>
                  <th>시작일시</th>
                  <th>종료일시</th>
                  <th>상태</th>
                  <th>관리</th>
                </tr>
                </thead>
                <tbody>
                {data.length === 0 ? (
                    <tr>
                      <td colSpan={6} style={{textAlign: 'center', color: '#A0AEC0'}}>데이터가 없습니다.</td>
                    </tr>
                ) : data.map(r => (
                    <tr key={r.id}>
                      <td>{r.buildingName} {r.roomCode}</td>
                      <td>{r.userName} {r.clubName && <small className="text-muted">({r.clubName})</small>}</td>
                      <td>{formatDateTime(r.startTime)}</td>
                      <td>{formatDateTime(r.endTime)}</td>
                      <td>
                        <span className={`mypage-badge ${STATUS_CLASSES[r.status]}`}>{STATUS_MAP[r.status]}</span>
                      </td>
                      <td>
                        {r.status !== 'CANCELLED' ? (
                            <button className="btn btn-sm btn-outline-primary"
                                    onClick={() => setSelected(r)}>처리</button>
                        ) : '-'}
                      </td>
                    </tr>
                ))}
                </tbody>
              </table>
            </div>
        )}
        {selected && (
            <ReservationStatusModal res={selected} onClose={() => setSelected(null)}
                                    onRefresh={() => load(statusFilter)}/>
        )}
      </div>
  );
}

// ############ 기숙사 탭 ############

function DormStatusModal({app, onClose, onRefresh}) {
  const [status, setStatus] = useState(app.status === 'REJECTED' ? 'REJECTED' : 'APPROVED');
  const [reason, setReason] = useState(app.rejectReason || '');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    try {
      await updateAdminDormStatus(app.id, {status, rejectReason: status === 'REJECTED' ? reason : null});
      alert('처리가 완료되었습니다.');
      onRefresh();
      onClose();
    } catch (err) {
      alert(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
      <AdminActionModal title="기숙사 신청 심사" onClose={onClose} onSubmit={handleSubmit} submitting={submitting}>
        <div className="p-3 mb-3 border rounded bg-light">
          <strong>호실:</strong> {app.roomNumber}<br/>
          <strong>신청인:</strong> {app.studentsName || '-'}<br/>
          <strong>학기:</strong> {app.semester} ({app.period})
        </div>
        <div className="mb-3">
          <label className="form-label d-block">결정</label>
          <div className="btn-group w-100">
            <input type="radio" className="btn-check" name="dorm_st" id="dorm_st_app" value="APPROVED"
                   checked={status === 'APPROVED'} onChange={e => setStatus(e.target.value)}/>
            <label className="btn btn-outline-success" htmlFor="dorm_st_app">승인</label>
            <input type="radio" className="btn-check" name="dorm_st" id="dorm_st_rej" value="REJECTED"
                   checked={status === 'REJECTED'} onChange={e => setStatus(e.target.value)}/>
            <label className="btn btn-outline-danger" htmlFor="dorm_st_rej">거절</label>
          </div>
        </div>
        {status === 'REJECTED' && (
            <textarea className="form-control" placeholder="거절 사유를 입력하세요" value={reason}
                      onChange={e => setReason(e.target.value)} required/>
        )}
      </AdminActionModal>
  );
}

function DormTab() {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selected, setSelected] = useState(null);
  const [statusFilter, setStatusFilter] = useState('PENDING');

  const load = useCallback(async (status) => {
    setLoading(true);
    setError('');
    try {
      setData(await fetchAdminDorms(status));
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load(statusFilter);
  }, [load, statusFilter]);

  return (
      <div>
        <div className="mypage-section-header">
          <h3>기숙사 신청 관리</h3>
          <p>기숙사 입주 신청을 관리합니다.</p>
        </div>
        <StatusFilter value={statusFilter} onChange={setStatusFilter}/>
        {loading ? (
            <div className="mypage-loading">데이터를 불러오는 중...</div>
        ) : error ? (
            <div className="alert alert-danger">{error}</div>
        ) : (
            <div className="table-responsive">
              <table className="table mypage-table">
                <thead>
                <tr>
                  <th>호실</th>
                  <th>신청인</th>
                  <th>기간</th>
                  <th>신청일</th>
                  <th>상태</th>
                  <th>관리</th>
                </tr>
                </thead>
                <tbody>
                {data.length === 0 ? (
                    <tr>
                      <td colSpan={6} style={{textAlign: 'center', color: '#A0AEC0'}}>데이터가 없습니다.</td>
                    </tr>
                ) : data.map(d => (
                    <tr key={d.id}>
                      <td>{d.roomNumber}</td>
                      <td>{d.studentsName || '-'}</td>
                      <td>{d.semester} ({d.period})</td>
                      <td>{d.createdAt?.split('T')[0]}</td>
                      <td>
                        <span className={`mypage-badge ${STATUS_CLASSES[d.status]}`}>{STATUS_MAP[d.status]}</span>
                      </td>
                      <td>
                        {d.status !== 'CANCELLED' ? (
                            <button className="btn btn-sm btn-outline-primary"
                                    onClick={() => setSelected(d)}>처리</button>
                        ) : '-'}
                      </td>
                    </tr>
                ))}
                </tbody>
              </table>
            </div>
        )}
        {selected && (
            <DormStatusModal app={selected} onClose={() => setSelected(null)}
                             onRefresh={() => load(statusFilter)}/>
        )}
      </div>
  );
}

// ############ 상담 예약 탭 ############

function CounselingStatusModal({item, onClose, onRefresh}) {
  const [status, setStatus] = useState(item.status === 'REJECTED' ? 'REJECTED' : 'APPROVED');
  const [reason, setReason] = useState(item.rejectReason || '');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    try {
      await updateAdminCounselingStatus(item.id, {status, rejectReason: status === 'REJECTED' ? reason : null});
      alert('처리가 완료되었습니다.');
      onRefresh();
      onClose();
    } catch (err) {
      alert(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
      <AdminActionModal title="상담 예약 심사" onClose={onClose} onSubmit={handleSubmit} submitting={submitting}>
        <div className="p-3 mb-3 border rounded bg-light">
          <strong>상담사:</strong> {item.counselorName}<br/>
          <strong>부서:</strong> {DEPT_LABELS[item.department] || item.department}<br/>
          <strong>날짜:</strong> {item.date}<br/>
          <strong>시간:</strong> {item.startTime?.substring(0, 5)} ~ {item.endTime?.substring(0, 5)}<br/>
          <strong>주제:</strong> {item.topic || '-'}
        </div>
        <div className="mb-3">
          <label className="form-label d-block">결정</label>
          <div className="btn-group w-100">
            <input type="radio" className="btn-check" name="coun_st" id="coun_st_app" value="APPROVED"
                   checked={status === 'APPROVED'} onChange={e => setStatus(e.target.value)}/>
            <label className="btn btn-outline-success" htmlFor="coun_st_app">승인</label>
            <input type="radio" className="btn-check" name="coun_st" id="coun_st_rej" value="REJECTED"
                   checked={status === 'REJECTED'} onChange={e => setStatus(e.target.value)}/>
            <label className="btn btn-outline-danger" htmlFor="coun_st_rej">거절</label>
          </div>
        </div>
        {status === 'REJECTED' && (
            <textarea className="form-control" placeholder="거절 사유를 입력하세요" value={reason}
                      onChange={e => setReason(e.target.value)} required/>
        )}
      </AdminActionModal>
  );
}

function CounselingTab() {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selected, setSelected] = useState(null);
  const [statusFilter, setStatusFilter] = useState('PENDING');

  const load = useCallback(async (status) => {
    setLoading(true);
    setError('');
    try {
      setData(await fetchAdminCounseling(status));
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load(statusFilter);
  }, [load, statusFilter]);

  return (
      <div>
        <div className="mypage-section-header">
          <h3>상담 예약 관리</h3>
          <p>상담 예약 신청을 관리합니다.</p>
        </div>
        <StatusFilter value={statusFilter} onChange={setStatusFilter}/>
        {loading ? (
            <div className="mypage-loading">데이터를 불러오는 중...</div>
        ) : error ? (
            <div className="alert alert-danger">{error}</div>
        ) : (
            <div className="table-responsive">
              <table className="table mypage-table">
                <thead>
                <tr>
                  <th>상담사</th>
                  <th>부서</th>
                  <th>날짜</th>
                  <th>시간</th>
                  <th>주제</th>
                  <th>상태</th>
                  <th>관리</th>
                </tr>
                </thead>
                <tbody>
                {data.length === 0 ? (
                    <tr>
                      <td colSpan={7} style={{textAlign: 'center', color: '#A0AEC0'}}>데이터가 없습니다.</td>
                    </tr>
                ) : data.map(r => (
                    <tr key={r.id}>
                      <td>{r.counselorName}</td>
                      <td>{DEPT_LABELS[r.department] || r.department}</td>
                      <td>{r.date}</td>
                      <td>{r.startTime?.substring(0, 5)} ~ {r.endTime?.substring(0, 5)}</td>
                      <td>{r.topic || '-'}</td>
                      <td>
                        <span className={`mypage-badge ${STATUS_CLASSES[r.status]}`}>{STATUS_MAP[r.status]}</span>
                      </td>
                      <td>
                        {r.status !== 'CANCELLED' ? (
                            <button className="btn btn-sm btn-outline-primary"
                                    onClick={() => setSelected(r)}>처리</button>
                        ) : '-'}
                      </td>
                    </tr>
                ))}
                </tbody>
              </table>
            </div>
        )}
        {selected && (
            <CounselingStatusModal item={selected} onClose={() => setSelected(null)}
                                   onRefresh={() => load(statusFilter)}/>
        )}
      </div>
  );
}