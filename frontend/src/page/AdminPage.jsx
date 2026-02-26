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
import {fetchClubs} from '../data/api.js';

const TABS = [
  {key: 'clubs', label: '동아리 신청 관리'},
  {key: 'reservations', label: '시설 예약 관리'},
  {key: 'dorms', label: '기숙사 신청 관리'},
];

const STATUS_CLUB = {APPROVED: '승인', PENDING: '대기 중', REJECTED: '거부'};
const STATUS_RESERVATION = {APPROVED: '승인', CANCELLED: '취소', PENDING: '대기 중', REJECTED: '거부'};
const STATUS_DORM = {APPROVED: '승인', CANCELLED: '취소', PENDING: '대기 중', REJECTED: '거부'};

const STATUS_CLASSES = {
  APPROVED: 'mypage-badge-approved',
  CANCELLED: 'mypage-badge-cancelled',
  PENDING: 'mypage-badge-pending',
  REJECTED: 'mypage-badge-rejected',
};

export function AdminPage() {
  const {currentUser} = useAuth();
  const navigate = useNavigate();
  const [tab, setTab] = useState('clubs');

  useEffect(() => {
    if (!currentUser) navigate('/');
  }, [currentUser, navigate]);

  if (!currentUser) return null;

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

// 동아리 소개 모달
function DescriptionModal({name, description, onClose}) {
  return (
      <div className="modal-bg show" onClick={e => {
        if (e.target === e.currentTarget) onClose();
      }}>
        <div className="modal-box">
          <div className="modal-hd">
            <button className="modal-close" onClick={onClose}>✕</button>
            <h3>{name} 동아리 소개문</h3>
          </div>
          <div className="modal-bd">
            <div className="mb-3">
              <label className="form-label">소개문</label>
              <textarea className="form-control" rows={7} value={description} readOnly={true}
                        style={{background: '#F7FAFC'}}/>
            </div>
          </div>
        </div>
      </div>
  );
}

// 동아리 신청 처리 모달
function ClubStatusModal({club, onClose, onRefresh}) {
  const [status, setStatus] = useState('APPROVED');
  const [reason, setReason] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    try {
      const response = await fetch(`/api/admin/clubs/${club.id}/status`, {
        method: 'PATCH',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({status, rejectReason: status === 'REJECTED' ? reason : null}),
      });

      if (!response.ok) throw new Error('처리 중 오류 발생');

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
      <div className="modal-bg show" onClick={e => {
        if (e.target === e.currentTarget) onClose();
      }}>
        <div className="modal-box">
          <div className="modal-hd">
            <button className="modal-close" onClick={onClose}>✕</button>
            <h3>{club.name} (<code>{club.slug}</code>) 동아리 개설 신청 처리</h3>
          </div>
          <form onSubmit={handleSubmit} className="modal-bd">
            <div className="mb-3">
              <label className="form-label">소개문</label>
              <textarea className="form-control" rows={7} value={club.description} readOnly={true}
                        style={{background: '#F7FAFC'}}/>
            </div>
            <div className="row mb-3">
              <div className="col">
                <label className="form-label">동아리 회장</label>
                <input className="form-control" value={club.president} readOnly={true} style={{background: '#F7FAFC'}}/>
              </div>
              <div className="col">
                <label className="form-label">신청일</label>
                <input className="form-control" value={club.createdAt} readOnly={true} style={{background: '#F7FAFC'}}/>
              </div>
            </div>
            <hr/>
            <div className="mb-3">
              <label className="form-label d-block">승인 여부</label>
              <div className="form-check form-check-inline">
                <input className="form-check-input" type="radio" name="status" id="st_app" value="APPROVED"
                       checked={status === 'APPROVED'} onChange={e => setStatus(e.target.value)}/>
                <label className="form-check-label" htmlFor="st_app">{STATUS_CLUB.APPROVED}</label>
              </div>
              <div className="form-check form-check-inline">
                <input className="form-check-input" type="radio" name="status" id="st_rej" value="REJECTED"
                       checked={status === 'REJECTED'} onChange={e => setStatus(e.target.value)}/>
                <label className="form-check-label" htmlFor="st_rej">{STATUS_CLUB.REJECTED}</label>
              </div>
            </div>
            {status === 'REJECTED' && (
                <div className="mb-3">
                  <label className="form-label">거절 사유</label>
                  <textarea className="form-control" rows={7} placeholder="(선택) 거절 사유를 입력하세요" value={reason}
                            onChange={e => setReason(e.target.value)}/>
                </div>
            )}
            <div className="modal-ft d-flex justify-content-end gap-2">
              <button type="button" className="btn btn-secondary" onClick={onClose}>취소</button>
              <button type="submit" className="btn btn-primary" disabled={submitting}>
                {submitting ? '처리 중...' : '확인'}
              </button>
            </div>
          </form>
        </div>
      </div>
  );
}

// 동아리 탭
function ClubTab() {
  const [apps, setApps] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedClub, setSelectedClub] = useState(null);
  const [viewDesc, setViewDesc] = useState(null);

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const data = await fetchClubs('PENDING');
      setApps(data);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData().then(() => {
    });
  }, [loadData]);

  if (loading) return <div className="mypage-loading">데이터를 불러오는 중...</div>;

  return (
      <div>
        <div className="mypage-section-header">
          <h3>동아리 신청 내역</h3>
          <p className="mypage-section-sub">현재 승인 대기 중인 동아리 신청 현황을 확인할 수 있습니다</p>
        </div>
        {apps.length === 0 ? (
            <div className="mypage-empty">현재 승인 대기 중인 동아리가 없습니다.</div>
        ) : (
            <>
              <div className="table-responsive">
                <table className="table mypage-table">
                  <thead>
                  <tr>
                    <th>동아리 이름</th>
                    <th>동아리 소개</th>
                    <th>동아리 식별자</th>
                    <th>동아리장</th>
                    <th>상태</th>
                    <th>신청일</th>
                    <th></th>
                  </tr>
                  </thead>
                  <tbody>
                  {apps.map(a => (
                      <tr key={a.id}>
                        <td>{a.name}</td>
                        <td>
                          <button className="btn btn-primary btn-sm" onClick={() => setViewDesc(a)}>설명 보기</button>
                        </td>
                        <td>{a.slug}</td>
                        <td>{a.president}</td>
                        <td>
                        <span
                            className={`mypage-badge ${STATUS_CLASSES[a.status] || ''}`}>{STATUS_CLUB[a.status] || a.status}</span>
                        </td>
                        <td>{a.createdAt?.substring(0, 10)}</td>
                        <td>
                          <button className="btn btn-outline-danger btn-sm" onClick={() => setSelectedClub(a)}>처리
                          </button>
                        </td>
                      </tr>
                  ))}
                  </tbody>
                </table>
              </div>
              {viewDesc && (
                  <DescriptionModal
                      name={viewDesc.name}
                      description={viewDesc.description}
                      onClose={() => setViewDesc(null)}
                  />
              )}
              {selectedClub && (
                  <ClubStatusModal
                      club={selectedClub}
                      onClose={() => setSelectedClub(null)}
                      onRefresh={loadData}
                  />
              )}
            </>
        )}
      </div>
  );
}

// 시설 예약 탭
function ReservationTab() {
  return (<div>bbbbbb</div>);
}

// 기숙사 탭
function DormTab() {
  return (<div>cccccc</div>);
}

