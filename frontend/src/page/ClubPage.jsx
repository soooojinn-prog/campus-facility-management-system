// 동아리 페이지 — /clubs
// 탭: 동아리 목록 | 동아리 개설 신청
// 목록 카드 클릭 → 상세 뷰 (내부 state 전환)
//
// API 의존:
//   GET  /api/clubs?q=&page=          — 동아리 목록 (공개)
//   GET  /api/clubs/{slug}            — 상세 (공개)
//   GET  /api/clubs/{slug}/members    — 부원 목록 (공개)
//   POST /api/clubs                   — 개설 신청 (인증)
//   POST /api/clubs/{slug}/members    — 가입 신청 (인증)
//   PATCH /api/clubs/{slug}           — 정보 수정 (인증)
import {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {useAuth} from '../context/AuthContext.jsx';
import {createClub, fetchClubDetail, fetchClubMembers, fetchClubs, joinClub, updateClub} from '../data/api.js';

const ROLE_LABELS = {ROLE_PRESIDENT: '회장', ROLE_VICE_PRESIDENT: '부회장', ROLE_MEMBER: '부원'};
const STATUS_LABELS = {APPROVED: '승인됨', PENDING: '승인 대기', REJECTED: '거절됨'};
const STATUS_BADGE = {APPROVED: 'mypage-badge-approved', PENDING: 'mypage-badge-pending', REJECTED: 'mypage-badge-rejected'};

export function ClubPage() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('list'); // list | create
  const [activeView, setActiveView] = useState('list'); // list | detail
  const [selectedSlug, setSelectedSlug] = useState(null);

  function handleCardClick(slug) {
    setSelectedSlug(slug);
    setActiveView('detail');
  }

  function handleBackToList() {
    setActiveView('list');
    setSelectedSlug(null);
  }

  return (
      <div>
        {/* 상단 정보 */}
        <div className="store-info-area">
          <div className="container">
            <div className="breadcrumb-nav">
              <a href="/">캠퍼스 맵</a><span className="sep">/</span><span className="current">동아리</span>
            </div>
            <h2 className="store-name" style={{marginTop: '1rem'}}>
              동아리
              <a className="back-link" onClick={() => navigate('/')}>← 캠퍼스 지도로 돌아가기</a>
            </h2>
            <p style={{color: '#718096', fontSize: '.9rem', marginTop: '.5rem'}}>동아리를 검색하고, 가입하거나 새로운 동아리를 개설 신청하세요.</p>
          </div>
        </div>

        {/* 탭 */}
        <div className="building-tabs">
          <button className={activeTab === 'list' ? 'active' : ''} onClick={() => { setActiveTab('list'); handleBackToList(); }}>
            동아리 목록
          </button>
          <button className={activeTab === 'create' ? 'active' : ''} onClick={() => setActiveTab('create')}>
            동아리 개설 신청
          </button>
        </div>

        {/* 본문 */}
        {activeTab === 'list' && activeView === 'list' && <ClubListView onCardClick={handleCardClick}/>}
        {activeTab === 'list' && activeView === 'detail' && selectedSlug && (
            <ClubDetailView slug={selectedSlug} onBack={handleBackToList}/>
        )}
        {activeTab === 'create' && <ClubCreateForm onCreated={() => { setActiveTab('list'); handleBackToList(); }}/>}
      </div>
  );
}

// ── 동아리 목록 뷰 ──
function ClubListView({onCardClick}) {
  const [query, setQuery] = useState('');
  const [clubs, setClubs] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => { loadClubs(); }, []);

  function loadClubs(q = '') {
    setLoading(true);
    fetchClubs(q).then(setClubs).catch(() => setClubs([])).finally(() => setLoading(false));
  }

  function handleSearch(e) {
    e.preventDefault();
    loadClubs(query);
  }

  return (
      <div style={{background: '#FAFBFC', minHeight: 'calc(100vh - 280px)', padding: '32px 0'}}>
        <div className="container">
          {/* 검색 */}
          <form onSubmit={handleSearch} style={{display: 'flex', gap: '8px', marginBottom: '24px', maxWidth: '500px'}}>
            <input type="text" className="form-control" placeholder="동아리 이름으로 검색..."
                   value={query} onChange={e => setQuery(e.target.value)}/>
            <button type="submit" className="btn btn-primary" style={{whiteSpace: 'nowrap'}}>검색</button>
          </form>

          {loading && <div className="mypage-loading">동아리 목록을 불러오는 중...</div>}

          {!loading && clubs.length === 0 && (
              <div className="mypage-empty">등록된 동아리가 없습니다.</div>
          )}

          {/* 카드 그리드 */}
          {!loading && clubs.length > 0 && (
              <div style={{display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '20px'}}>
                {clubs.filter(c => c.status === 'APPROVED').map(club => (
                    <div key={club.id} onClick={() => onCardClick(club.slug)}
                         style={{
                           background: '#FFF', border: '1px solid #E2E8F0', borderRadius: '12px',
                           padding: '24px', cursor: 'pointer', transition: 'transform .15s, box-shadow .15s',
                         }}
                         onMouseEnter={e => { e.currentTarget.style.transform = 'translateY(-4px)'; e.currentTarget.style.boxShadow = '0 8px 24px rgba(0,0,0,.08)'; }}
                         onMouseLeave={e => { e.currentTarget.style.transform = ''; e.currentTarget.style.boxShadow = ''; }}>
                      <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '12px'}}>
                        <h4 style={{color: '#1A365D', fontSize: '1.1rem', fontWeight: 700, margin: 0}}>{club.name}</h4>
                        <span className={`mypage-badge ${STATUS_BADGE[club.status]}`}>{STATUS_LABELS[club.status]}</span>
                      </div>
                      <div style={{color: '#718096', fontSize: '.85rem', display: 'flex', flexDirection: 'column', gap: '4px'}}>
                        <span>회장: {club.presidentName}</span>
                        <span>부원 수: {club.memberCount}명</span>
                        <span>개설일: {club.createdAt?.substring(0, 10)}</span>
                      </div>
                    </div>
                ))}
              </div>
          )}
        </div>
      </div>
  );
}

// ── 동아리 상세 뷰 ──
function ClubDetailView({slug, onBack}) {
  const {currentUser} = useAuth();
  const [club, setClub] = useState(null);
  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);
  const [editForm, setEditForm] = useState({name: '', description: '', autoApprove: false});

  useEffect(() => {
    setLoading(true);
    Promise.all([fetchClubDetail(slug), fetchClubMembers(slug)])
        .then(([detail, memberList]) => {
          setClub(detail);
          setMembers(memberList);
          setEditForm({name: detail.name, description: detail.description || '', autoApprove: detail.autoApprove || false});
        })
        .catch(() => {})
        .finally(() => setLoading(false));
  }, [slug]);

  const isPresident = currentUser && members.some(m => m.userId === currentUser.id && m.role === 'ROLE_PRESIDENT');
  const isMember = currentUser && members.some(m => m.userId === currentUser.id);

  async function handleJoin() {
    if (!currentUser) { alert('로그인이 필요합니다.'); return; }
    try {
      await joinClub(slug);
      alert(club.autoApprove ? '동아리에 가입되었습니다.' : '가입 신청이 완료되었습니다. 승인을 기다려주세요.');
      // 멤버 목록 재조회
      const memberList = await fetchClubMembers(slug);
      setMembers(memberList);
      const detail = await fetchClubDetail(slug);
      setClub(detail);
    } catch (err) { alert(err.message); }
  }

  async function handleKick(userId) {
    if (!confirm('정말 이 부원을 추방하시겠습니까?')) return;
    try {
      await fetch(`/api/clubs/${slug}/members/${userId}`, {method: 'DELETE'});
      setMembers(prev => prev.filter(m => m.userId !== userId));
      const detail = await fetchClubDetail(slug);
      setClub(detail);
    } catch (err) { alert('추방에 실패했습니다.'); }
  }

  async function handleRoleChange(userId, newRole) {
    try {
      const res = await fetch(`/api/clubs/${slug}/members/${userId}/role`, {
        method: 'PATCH', headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({clubRole: newRole}),
      });
      if (!res.ok) throw new Error(await res.text());
      const updated = await res.json();
      setMembers(prev => prev.map(m => m.userId === userId ? updated : m));
    } catch (err) { alert('역할 변경에 실패했습니다.'); }
  }

  async function handleUpdateClub(e) {
    e.preventDefault();
    try {
      const updated = await updateClub(slug, editForm);
      setClub(updated);
      setEditing(false);
      alert('동아리 정보가 수정되었습니다.');
    } catch (err) { alert(err.message); }
  }

  if (loading) return <div className="mypage-loading" style={{minHeight: 'calc(100vh - 280px)'}}>동아리 정보를 불러오는 중...</div>;
  if (!club) return <div className="mypage-empty" style={{margin: '48px'}}>동아리를 찾을 수 없습니다.</div>;

  return (
      <div style={{background: '#FAFBFC', minHeight: 'calc(100vh - 280px)', padding: '32px 0'}}>
        <div className="container">
          {/* 뒤로가기 */}
          <button className="btn btn-outline-secondary btn-sm" onClick={onBack} style={{marginBottom: '20px'}}>
            ← 목록으로
          </button>

          {/* 기본 정보 */}
          <div className="mypage-profile-card">
            <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '16px'}}>
              <h3 style={{color: '#1A365D', fontSize: '1.5rem', fontWeight: 900, margin: 0}}>{club.name}</h3>
              <span className={`mypage-badge ${STATUS_BADGE[club.status]}`}>{STATUS_LABELS[club.status]}</span>
            </div>
            {club.description && (
                <p style={{color: '#4A5568', fontSize: '.9rem', marginBottom: '16px', whiteSpace: 'pre-line'}}>{club.description}</p>
            )}
            <div style={{display: 'flex', gap: '24px', flexWrap: 'wrap', color: '#718096', fontSize: '.85rem'}}>
              <span>회장: <strong style={{color: '#2D3748'}}>{club.presidentName}</strong></span>
              <span>부원 수: <strong style={{color: '#2D3748'}}>{club.memberCount}명</strong></span>
              <span>개설일: <strong style={{color: '#2D3748'}}>{club.createdAt?.substring(0, 10)}</strong></span>
              <span>자동 승인: <strong style={{color: '#2D3748'}}>{club.autoApprove ? '예' : '아니오'}</strong></span>
            </div>

            {/* 가입 버튼 */}
            {currentUser && !isMember && club.status === 'APPROVED' && (
                <button className="btn btn-primary" onClick={handleJoin} style={{marginTop: '16px'}}>가입 신청</button>
            )}
            {!currentUser && (
                <p style={{color: '#A0AEC0', fontSize: '.82rem', marginTop: '12px'}}>가입하려면 로그인이 필요합니다.</p>
            )}

            {/* 회장: 정보 수정 버튼 */}
            {isPresident && !editing && (
                <button className="btn btn-outline-primary btn-sm" onClick={() => setEditing(true)} style={{marginTop: '12px', marginLeft: '8px'}}>
                  동아리 정보 수정
                </button>
            )}
          </div>

          {/* 정보 수정 폼 (회장만) */}
          {isPresident && editing && (
              <div className="mypage-edit-section" style={{marginBottom: '24px'}}>
                <h4>동아리 정보 수정</h4>
                <form onSubmit={handleUpdateClub}>
                  <div className="mb-3">
                    <label className="form-label">동아리 이름</label>
                    <input type="text" className="form-control" value={editForm.name}
                           onChange={e => setEditForm({...editForm, name: e.target.value})}/>
                  </div>
                  <div className="mb-3">
                    <label className="form-label">설명</label>
                    <textarea className="form-control" rows="4" value={editForm.description}
                              onChange={e => setEditForm({...editForm, description: e.target.value})}/>
                  </div>
                  <div className="mb-3 form-check">
                    <input type="checkbox" className="form-check-input" id="autoApproveEdit"
                           checked={editForm.autoApprove} onChange={e => setEditForm({...editForm, autoApprove: e.target.checked})}/>
                    <label className="form-check-label" htmlFor="autoApproveEdit">가입 자동 승인</label>
                  </div>
                  <div style={{display: 'flex', gap: '8px'}}>
                    <button type="submit" className="btn btn-primary btn-sm">저장</button>
                    <button type="button" className="btn btn-secondary btn-sm" onClick={() => setEditing(false)}>취소</button>
                  </div>
                </form>
              </div>
          )}

          {/* 부원 목록 */}
          <div className="mypage-profile-card">
            <h4 style={{color: '#1A365D', fontWeight: 700, marginBottom: '16px'}}>부원 목록</h4>
            {members.length === 0 ? (
                <div className="mypage-empty">등록된 부원이 없습니다.</div>
            ) : (
                <div style={{overflowX: 'auto'}}>
                  <table className="table mypage-table" style={{width: '100%'}}>
                    <thead>
                    <tr>
                      <th>이름</th>
                      <th>학번</th>
                      <th>역할</th>
                      <th>가입일</th>
                      {isPresident && <th>관리</th>}
                    </tr>
                    </thead>
                    <tbody>
                    {members.map(m => (
                        <tr key={m.userId}>
                          <td>{m.name}</td>
                          <td>{m.userNnumber}</td>
                          <td><span className={`mypage-badge ${m.role === 'ROLE_PRESIDENT' ? 'mypage-badge-approved' : 'mypage-badge-pending'}`}>
                            {ROLE_LABELS[m.role] || m.role}
                          </span></td>
                          <td>{m.joinedAt?.substring(0, 10)}</td>
                          {isPresident && (
                              <td>
                                {m.role !== 'ROLE_PRESIDENT' && (
                                    <div style={{display: 'flex', gap: '4px', flexWrap: 'wrap'}}>
                                      <select className="form-select form-select-sm" style={{width: 'auto', fontSize: '.78rem'}}
                                              value={m.role} onChange={e => handleRoleChange(m.userId, e.target.value)}>
                                        <option value="ROLE_MEMBER">부원</option>
                                        <option value="ROLE_VICE_PRESIDENT">부회장</option>
                                      </select>
                                      <button className="btn btn-outline-danger btn-sm" style={{fontSize: '.75rem', padding: '2px 8px'}}
                                              onClick={() => handleKick(m.userId)}>추방</button>
                                    </div>
                                )}
                              </td>
                          )}
                        </tr>
                    ))}
                    </tbody>
                  </table>
                </div>
            )}
          </div>
        </div>
      </div>
  );
}

// ── 동아리 개설 신청 폼 ──
function ClubCreateForm({onCreated}) {
  const {currentUser} = useAuth();
  const [form, setForm] = useState({name: '', slug: '', description: '', autoApprove: false});
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    if (!currentUser) { alert('로그인이 필요합니다.'); return; }
    if (!form.name.trim()) { setError('동아리 이름을 입력해주세요.'); return; }
    if (!form.slug.trim()) { setError('동아리 고유 식별자(slug)를 입력해주세요.'); return; }
    setSubmitting(true);
    try {
      await createClub(form);
      alert('동아리 개설 신청이 완료되었습니다. 관리자 승인 후 활성화됩니다.');
      setForm({name: '', slug: '', description: '', autoApprove: false});
      onCreated();
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  if (!currentUser) {
    return (
        <div style={{background: '#FAFBFC', minHeight: 'calc(100vh - 280px)', padding: '48px 0'}}>
          <div className="container">
            <div className="mypage-empty">동아리 개설 신청을 하려면 로그인이 필요합니다.</div>
          </div>
        </div>
    );
  }

  return (
      <div style={{background: '#FAFBFC', minHeight: 'calc(100vh - 280px)', padding: '32px 0'}}>
        <div className="container" style={{maxWidth: '600px'}}>
          <div className="mypage-edit-section">
            <h4>동아리 개설 신청</h4>
            <p style={{color: '#718096', fontSize: '.85rem', marginBottom: '20px'}}>
              신청 후 관리자 승인이 필요합니다. 승인되면 자동으로 회장으로 등록됩니다.
            </p>
            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label className="form-label">동아리 이름 <span className="text-danger">*</span></label>
                <input type="text" className="form-control" placeholder="예: 코딩 동아리"
                       value={form.name} onChange={e => setForm({...form, name: e.target.value})}/>
              </div>
              <div className="mb-3">
                <label className="form-label">고유 식별자 (slug) <span className="text-danger">*</span></label>
                <input type="text" className="form-control" placeholder="예: coding (영문, 최대 10자)"
                       maxLength={10} value={form.slug} onChange={e => setForm({...form, slug: e.target.value.replace(/[^a-zA-Z0-9-]/g, '')})}/>
                <div className="form-text">URL에 사용되는 고유 식별자입니다. 영문과 숫자, 하이픈만 사용 가능합니다.</div>
              </div>
              <div className="mb-3">
                <label className="form-label">설명</label>
                <textarea className="form-control" rows="4" placeholder="동아리 소개를 작성해주세요"
                          value={form.description} onChange={e => setForm({...form, description: e.target.value})}/>
              </div>
              <div className="mb-3 form-check">
                <input type="checkbox" className="form-check-input" id="autoApproveCreate"
                       checked={form.autoApprove} onChange={e => setForm({...form, autoApprove: e.target.checked})}/>
                <label className="form-check-label" htmlFor="autoApproveCreate">가입 자동 승인</label>
                <div className="form-text">체크하면 가입 신청 시 자동으로 승인됩니다.</div>
              </div>
              {error && <div className="alert alert-danger py-2">{error}</div>}
              <button type="submit" className="btn btn-primary" disabled={submitting}>
                {submitting ? '신청 중...' : '개설 신청'}
              </button>
            </form>
          </div>
        </div>
      </div>
  );
}
