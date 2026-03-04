import {useCallback, useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {AuthModal} from '../components/AuthModal.jsx';
import {useAuth} from '../context/AuthContext.jsx';
import {
  fetchCongestion,
  fetchNotice,
  fetchNotices,
  fetchReadingRooms,
  fetchReadingRoomSeats,
  fetchStudyRooms,
  fetchStudyRoomSlots,
  reserveSeat,
  reserveStudyRoom,
  searchBooks,
} from '../data/api.js';

// ── 상수 ──
const NAV_TABS = [
  {key: 'reading', label: '열람실 현황'},
  {key: 'books', label: '도서 검색'},
  {key: 'study', label: '스터디룸 예약'},
  {key: 'congestion', label: '혼잡도'},
  {key: 'notices', label: '공지사항'},
];

// 열람실 표시 순서: 층 문자열 기준 정렬 (4F → 3F → 2F → B1)
function floorSortKey(floor) {
  if (floor.startsWith('B')) return -parseInt(floor.substring(1)); // B1 → -1
  return parseInt(floor); // 2F → 2, 3F → 3, 4F → 4
}

const BOOK_CATEGORIES = ['컴퓨터/IT', 'AI/ML', '수학', '인문/사회', '교양'];
const STUDY_TIMES = ['09:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00', '18:00'];

// ── 유틸 ──
function congestionColor(rate) {
  if (rate >= 80) return '#e53e3e';
  if (rate >= 60) return '#d69e2e';
  return '#38a169';
}

function congestionLabel(rate) {
  if (rate >= 80) return '혼잡';
  if (rate >= 60) return '보통';
  return '여유';
}

function seatColor(s) {
  return s === 'OCCUPIED' ? '#e53e3e' : s === 'RESERVED' ? '#d69e2e' : '#38a169';
}

function studyStatusLabel(s) {
  return s === 'AVAILABLE' ? '사용 가능' : s === 'OCCUPIED' ? '사용 중' : '점검 중';
}

function studyStatusColor(s) {
  return s === 'AVAILABLE' ? '#38a169' : s === 'OCCUPIED' ? '#e53e3e' : '#d69e2e';
}

function noticeTypeColor(t) {
  return t === '긴급' ? '#e53e3e' : t === '이벤트' ? '#38a169' : t === '안내' ? '#d69e2e' : '#4299e1';
}

// ── 공통 컴포넌트 ──
function LoadingBox() {
  return (
      <div style={{padding: '40px', color: '#a0aec0', display: 'flex', alignItems: 'center', gap: 12}}>
        <div style={{
          width: 20,
          height: 20,
          border: '2px solid #e2e8f0',
          borderTopColor: '#2c5282',
          borderRadius: '50%',
          animation: 'lib-spin .7s linear infinite',
        }}/>
        로딩 중...
      </div>
  );
}

function ErrorBox({msg, onRetry}) {
  return (
      <div style={{
        margin: 20,
        padding: '16px 20px',
        background: '#fff5f5',
        border: '1px solid #fed7d7',
        borderRadius: 8,
        color: '#c53030',
        fontSize: '.88rem',
        display: 'flex',
        alignItems: 'center',
        gap: 12,
      }}>
        ⚠️ {msg}
        {onRetry && <button className="btn btn-sm btn-outline-secondary" onClick={onRetry}>재시도</button>}
      </div>
  );
}

// 도서 행 공통 컴포넌트
function BookRow({b}) {
  return (
      <div className="room-row" style={{display: 'flex', alignItems: 'center', gap: 14}}>
        <div style={{
          width: 34, height: 44, flexShrink: 0,
          background: 'linear-gradient(135deg,#ebf8ff,#e9d8fd)',
          borderRadius: 4, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.1rem',
        }}>📖
        </div>
        <div style={{flex: 1}}>
          <div className="room-name">{b.title}</div>
          <div style={{
            fontSize: '.78rem',
            color: '#718096',
            marginTop: 2,
          }}>{b.author} · {b.publisher}{b.category ? ` · ${b.category}` : ''}</div>
        </div>
        <div style={{display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 4}}>
        <span style={{
          padding: '2px 10px', borderRadius: 20, fontSize: '.72rem', fontWeight: 600,
          background: (b.available ? '#38a169' : '#e53e3e') + '15',
          color: b.available ? '#38a169' : '#e53e3e',
          border: `1px solid ${b.available ? '#38a169' : '#e53e3e'}40`,
        }}>
          {b.available ? '대출 가능' : '대출 중'}
        </span>
        </div>
      </div>
  );
}

// 1. 열람실 현황

function ReadingSection({onNeedLogin}) {
  const {currentUser} = useAuth();
  const [rooms, setRooms] = useState([]);
  const [selectedId, setSelectedId] = useState(null); // 기본값 2F(제1열람실) = id 1
  const [seatData, setSeatData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [seatLoading, setSeatLoading] = useState(false);
  const [error, setError] = useState(null);
  // 로컬 예약 상태: { seatNo: 'RESERVED' } — 실제 예약 후 즉시 UI 반영용
  const [localReserved, setLocalReserved] = useState({});
  // 오늘 이미 예약 완료 여부 (1인 1좌석 제한용)
  const [alreadyReserved, setAlreadyReserved] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchReadingRooms();
      setRooms(data);
      // 기본값: id=1 (제1열람실 2F)
      await loadSeats(1, data);
      setSelectedId(1);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  async function loadSeats(id, roomList) {
    setSeatLoading(true);
    setSeatData(null);
    setLocalReserved({});
    try {
      setSeatData(await fetchReadingRoomSeats(id));
    } catch {
      setSeatData(null);
    } finally {
      setSeatLoading(false);
    }
  }

  async function handleSelectRoom(room) {
    setSelectedId(room.id);
    await loadSeats(room.id);
  }

  // 좌석 클릭 — 이용 가능인 경우만 반응
  async function handleSeatClick(seat) {
    if (seat.status !== 'AVAILABLE') return;
    if (!currentUser) {
      alert('로그인 후 예약이 가능합니다.');
      onNeedLogin();
      return;
    }
    // 1인 1좌석 — 이미 오늘 예약한 경우 클라이언트에서 선차단
    if (alreadyReserved) {
      alert('오늘 이미 열람실 좌석을 예약하셨습니다.\n1인 1좌석만 예약 가능합니다. 내 예약에서 확인하세요.');
      return;
    }
    // DB 저장 — reserveSeat API 호출 (서버에서도 1인1좌석 재검증)
    try {
      await reserveSeat(selectedId, seat.seatNo);
      // 즉시 UI 반영
      setLocalReserved(prev => ({...prev, [seat.seatNo]: 'RESERVED'}));
      setAlreadyReserved(true); // 이후 다른 좌석 클릭 차단
      alert(`${seat.seatNo}번 좌석 예약이 완료되었습니다.\n내 예약에서 확인하실 수 있습니다.`);
    } catch (e) {
      // 서버 에러 메시지 그대로 표시 (1인1좌석, 이미예약 등)
      alert(e.message || '예약에 실패했습니다.');
    }
  }

  if (loading) return <LoadingBox/>;
  if (error) return <ErrorBox msg={error} onRetry={load}/>;

  // API 응답의 floor 기준 내림차순 정렬 (4F → 3F → 2F → B1)
  const sortedRooms = [...rooms].sort((a, b) => floorSortKey(b.floor) - floorSortKey(a.floor));

  const selected = rooms.find(r => r.id === selectedId);
  const totalSeats = selected ? selected.totalSeats : 0;

  return (
      <div className="caf-foodcourt">
        {/* 좌측: 열람실 목록 (B1 → 2F → 3F → 4F 순, 아래서 위로) */}
        <div className="floor-list">
          {sortedRooms.map(r => {
            const total = r.totalSeats;
            // usedSeats는 서버에서 이미 100% 이내로 보정돼 옴
            const used = Math.min(r.usedSeats, total);
            const rate = Math.min(Math.round((used / total) * 100), 100);
            const avail = Math.max(total - used, 0);
            const color = congestionColor(rate);
            const isActive = r.id === selectedId;
            return (
                <div
                    key={r.id}
                    className={`floor-item${isActive ? ' active' : ''}`}
                    onClick={() => handleSelectRoom(r)}
                >
                  <div className="floor-num" style={{fontSize: '.9rem', minWidth: 32}}>{r.floor}</div>
                  <div style={{flex: 1}}>
                    <div style={{fontWeight: 500, fontSize: '.9rem'}}>{r.name}</div>
                    <div className="floor-desc">총 {total}석 · 잔여 {avail}석</div>
                    <div style={{
                      marginTop: 5,
                      background: isActive ? 'rgba(255,255,255,.25)' : '#edf2f7',
                      borderRadius: 4, height: 4, overflow: 'hidden',
                    }}>
                      <div style={{
                        width: `${rate}%`,
                        background: isActive ? 'rgba(255,255,255,.8)' : color,
                        height: '100%', borderRadius: 4, transition: 'width .5s',
                      }}/>
                    </div>
                  </div>
                  <div className="floor-pin" style={{
                    background: isActive ? 'rgba(255,255,255,.2)' : color + '20',
                    color: isActive ? '#fff' : color,
                    border: `1px solid ${isActive ? 'rgba(255,255,255,.3)' : color + '40'}`,
                    fontWeight: 700, fontSize: '.72rem',
                  }}>
                    {rate}%
                  </div>
                </div>
            );
          })}
        </div>

        {/* 우측: 좌석 배치도 */}
        <div className="floor-detail">
          {!selected && <div style={{padding: 40, color: '#718096'}}>열람실을 선택해주세요.</div>}
          {selected && seatLoading && <LoadingBox/>}
          {selected && !seatLoading && seatData && (() => {
            const total = seatData.totalSeats;

            // 서버에서 받은 좌석 데이터 사용 + 로컬 예약(방금 클릭) 반영
            const seats = (seatData.seats || []).map(s => ({
              ...s,
              status: localReserved[s.seatNo] ? 'RESERVED' : s.status,
            }));

            const usedCount = seats.filter(s => s.status === 'OCCUPIED').length;
            const reservedCount = seats.filter(s => s.status === 'RESERVED').length;
            const availCount = Math.max(total - usedCount - reservedCount, 0);
            // 퍼센트: 100% 절대 초과 불가
            const rate = Math.min(Math.round((usedCount / total) * 100), 100);

            return (<>
              <div className="floor-detail-header">
                <h3>{selected.name}</h3>
                <div className="floor-subtitle">
                  총 {total}석 · 사용 중 {usedCount}석 · 예약됨 {reservedCount}석 · 잔여 {availCount}석
                </div>
              </div>
              <div className="room-category">
                <div className="room-category-title">
                  좌석 현황
                  <span className="room-category-count">{total}석 · {rate}% 사용 중</span>
                </div>
                {/* 범례 */}
                <div style={{display: 'flex', gap: 14, marginBottom: 12}}>
                  {[['AVAILABLE', '이용 가능', '#38a169'], ['OCCUPIED', '사용 중', '#e53e3e'], ['RESERVED', '예약됨', '#d69e2e']].map(([k, v, c]) => (
                      <div key={k} style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: 5,
                        fontSize: '.73rem',
                        color: '#718096',
                      }}>
                        <div style={{width: 10, height: 10, borderRadius: 2, background: c}}/>
                        {v}
                        {k === 'AVAILABLE' && <span style={{color: '#a0aec0', fontSize: '.65rem'}}>(클릭=예약)</span>}
                      </div>
                  ))}
                </div>
                {/* 좌석 그리드 — 한 줄 8개, 네모 최소화 */}
                <div style={{display: 'grid', gridTemplateColumns: 'repeat(8, 1fr)', gap: 4}}>
                  {seats.map(s => {
                    const c = seatColor(s.status);
                    const clickable = s.status === 'AVAILABLE';
                    return (
                        <div
                            key={s.seatNo}
                            title={`${s.seatNo}번 · ${s.status === 'AVAILABLE' ? '이용 가능 — 클릭하여 예약' : s.status === 'OCCUPIED' ? '사용 중' : '예약됨'}`}
                            onClick={() => handleSeatClick(s)}
                            style={{
                              width: '100%', paddingBottom: '75%', position: 'relative',
                              borderRadius: 4,
                              background: c + '1a',
                              border: `1px solid ${c}50`,
                              cursor: clickable ? 'pointer' : 'default',
                              transition: 'transform .1s, box-shadow .1s',
                            }}
                            onMouseEnter={e => {
                              if (clickable) {
                                e.currentTarget.style.transform = 'scale(1.15)';
                                e.currentTarget.style.boxShadow = `0 0 0 2px ${c}70`;
                                e.currentTarget.style.zIndex = 2;
                              }
                            }}
                            onMouseLeave={e => {
                              e.currentTarget.style.transform = '';
                              e.currentTarget.style.boxShadow = '';
                              e.currentTarget.style.zIndex = '';
                            }}
                        >
                      <span style={{
                        position: 'absolute', top: 0, left: 0, right: 0, bottom: 0,
                        display: 'flex', alignItems: 'center', justifyContent: 'center',
                        fontSize: '.55rem', fontWeight: 600, color: c,
                      }}>
                        {s.seatNo}
                      </span>
                        </div>
                    );
                  })}
                </div>
              </div>
            </>);
          })()}
          {selected && !seatLoading && !seatData && (
              <div style={{padding: 40, color: '#a0aec0'}}>좌석 정보를 불러올 수 없습니다.</div>
          )}
        </div>
      </div>
  );
}

// 2. 도서 검색

function BooksSection({onNeedLogin}) {
  const {currentUser} = useAuth();
  const [query, setQuery] = useState('');
  const [results, setResults] = useState(null); // null=미검색, []~=검색결과
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // 'search' | 'top5' | 'new' | 'return' | null
  const [quickMode, setQuickMode] = useState(null);

  async function handleSearch(q = query, {publisher = '', category = ''} = {}) {
    setLoading(true);
    setError(null);
    setQuickMode('search');
    setResults(null);
    try {
      setResults(await searchBooks({q, publisher, category}));
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleQuickNew() {
    setQuickMode('new');
    setResults(null);
    setLoading(true);
    setError(null);
    try {
      setResults(await searchBooks({q: ''}));
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleQuickTop5() {
    setQuickMode('top5');
    setLoading(true);
    setError(null);
    try {
      const all = await searchBooks({q: ''});
      setResults(all.slice(0, 5));
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  function handleQuickReturn() {
    if (!currentUser) {
      alert('로그인 후 확인이 가능합니다.');
      onNeedLogin();
      return;
    }
    setQuickMode('return');
    setResults([]); // TODO: 내 반납 예정 도서 API 연동
  }

  const quickCards = [
    {
      key: 'new', icon: '📦', title: '신규 입수', desc: '전체 도서',
      onClick: handleQuickNew,
    },
    {
      key: 'top5', icon: '🔥', title: '인기 도서', desc: 'Top 5',
      onClick: handleQuickTop5,
    },
    {
      key: 'return', icon: '📅', title: '반납 예정', desc: '내 반납',
      onClick: handleQuickReturn,
    },
  ];

  return (
      <div className="floor-detail" style={{padding: '32px 40px', maxWidth: '100%'}}>
        <div className="floor-detail-header">
          <h3>도서 검색</h3>
          <div className="floor-subtitle">제목 · 저자 · 출판사로 검색하세요</div>
        </div>

        {/* 검색 바 */}
        <div style={{display: 'flex', gap: 8, marginBottom: 10}}>
          <input
              placeholder="도서명, 저자 검색..."
              value={query}
              onChange={e => setQuery(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && handleSearch()}
              style={{
                flex: 1, padding: '10px 14px',
                border: '1px solid #e2e8f0', borderRadius: 8,
                fontSize: '.9rem', fontFamily: 'Noto Sans KR, sans-serif', outline: 'none',
              }}
          />
          <button className="btn btn-primary" onClick={() => handleSearch()}
                  style={{borderRadius: 8, whiteSpace: 'nowrap'}}>
            검색
          </button>
        </div>

        {/* 카테고리 빠른 검색 */}
        <div style={{display: 'flex', flexWrap: 'wrap', gap: 6, marginBottom: 24}}>
          {BOOK_CATEGORIES.map(c => (
              <button key={c} onClick={() => {
                setQuery('');
                handleSearch('', {category: c});
              }}
                      style={{
                        padding: '4px 12px',
                        border: '1px solid #e2e8f0',
                        borderRadius: 20,
                        background: '#f7fafc',
                        color: '#718096',
                        fontSize: '.78rem',
                        cursor: 'pointer',
                        fontFamily: 'Noto Sans KR, sans-serif',
                      }}>
                {c}
              </button>
          ))}
        </div>

        {/* 퀵 카드 — 검색 전에 항상 표시, hover 효과 */}
        {results === null && !loading && (
            <div style={{display: 'grid', gridTemplateColumns: 'repeat(3,1fr)', gap: 16, marginBottom: 0}}>
              {quickCards.map(card => (
                  <div
                      key={card.key}
                      onClick={card.onClick}
                      style={{
                        background: '#f7fafc', border: `2px solid ${quickMode === card.key ? '#2c5282' : '#e2e8f0'}`,
                        borderRadius: 10, padding: '22px 16px', textAlign: 'center',
                        cursor: 'pointer', transition: 'all .18s',
                      }}
                      onMouseEnter={e => {
                        e.currentTarget.style.background = '#ebf8ff';
                        e.currentTarget.style.borderColor = '#4299e1';
                        e.currentTarget.style.transform = 'translateY(-3px)';
                        e.currentTarget.style.boxShadow = '0 6px 20px rgba(44,82,130,.15)';
                      }}
                      onMouseLeave={e => {
                        e.currentTarget.style.background = '#f7fafc';
                        e.currentTarget.style.borderColor = quickMode === card.key ? '#2c5282' : '#e2e8f0';
                        e.currentTarget.style.transform = '';
                        e.currentTarget.style.boxShadow = '';
                      }}
                  >
                    <div style={{fontSize: '1.8rem', marginBottom: 6}}>{card.icon}</div>
                    <div style={{fontWeight: 700, fontSize: '.9rem', color: '#2d3748'}}>{card.title}</div>
                    <div style={{color: '#a0aec0', fontSize: '.72rem', marginTop: 2}}>{card.desc}</div>
                  </div>
              ))}
            </div>
        )}

        {loading && <LoadingBox/>}
        {error && <ErrorBox msg={error}/>}

        {/* 검색 결과 */}
        {!loading && results !== null && !error && (
            <div className="room-category">
              <div className="room-category-title"
                   style={{display: 'flex', alignItems: 'center', justifyContent: 'space-between'}}>
            <span>
              {quickMode === 'top5' ? '🔥 인기 도서 Top 5' :
                  quickMode === 'new' ? '📦 신규 입수 도서' :
                      quickMode === 'return' ? '📅 반납 예정 도서' : '검색 결과'}
              <span className="room-category-count">{results.length}건</span>
            </span>
                <button
                    style={{
                      fontSize: '.78rem',
                      color: '#a0aec0',
                      background: 'none',
                      border: 'none',
                      cursor: 'pointer',
                    }}
                    onClick={() => {
                      setResults(null);
                      setQuickMode(null);
                      setQuery('');
                    }}
                >
                  ✕ 닫기
                </button>
              </div>
              {results.length === 0
                  ? <div style={{padding: '30px 0', color: '#a0aec0', textAlign: 'center'}}>
                    {quickMode === 'return' ? '반납 예정 도서가 없습니다.' : '검색 결과가 없습니다.'}
                  </div>
                  : results.map(b => (
                      <BookRow key={b.id} b={b}/>
                  ))
              }
            </div>
        )}
      </div>
  );
}

// 3. 스터디룸 예약

function StudySection({onNeedLogin}) {
  const {currentUser} = useAuth();
  const [rooms, setRooms] = useState([]);
  const [selectedId, setSelectedId] = useState(null);
  const [slotData, setSlotData] = useState(null);
  const [selectedTime, setSelectedTime] = useState(null);
  const [loading, setLoading] = useState(true);
  const [slotLoading, setSlotLoading] = useState(false);
  const [reserving, setReserving] = useState(false);
  const [done, setDone] = useState(false);
  const [error, setError] = useState(null);
  // 로컬 예약 완료된 시간 — 방 이동해도 유지 (시간 중복 방지)
  const [localOccupied, setLocalOccupied] = useState([]); // 이 방의 예약된 슬롯
  const [myReservedTimes, setMyReservedTimes] = useState([]); // 내가 오늘 예약한 시간 전체

  const today = new Date().toISOString().split('T')[0];

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setRooms(await fetchStudyRooms());
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  async function handleSelectRoom(room) {
    if (room.status !== 'AVAILABLE') return;
    setSelectedId(room.id);
    setSelectedTime(null);
    setSlotData(null);
    setSlotLoading(true);
    setDone(false);
    setLocalOccupied([]);
    try {
      setSlotData(await fetchStudyRoomSlots(room.id, today));
    } catch {
      setSlotData({occupiedSlots: []});
    } finally {
      setSlotLoading(false);
    }
  }

  async function handleTimeClick(t, occupied) {
    if (occupied) return;
    if (!currentUser) {
      alert('로그인 후 예약이 가능합니다.');
      onNeedLogin();
      return;
    }
    // 같은 시간대 중복 예약 클라이언트 선차단
    if (myReservedTimes.includes(t)) {
      alert(`\n${t} 시간대는 이미 다른 스터디룸을 예약하셨습니다.\n같은 시간에 중복 예약은 불가능합니다.`);
      return;
    }
    // 로그인 상태 — confirm 창 (window.confirm: 취소/확인)
    const room = rooms.find(r => r.id === selectedId);
    const endTime = `${String(parseInt(t) + 1).padStart(2, '0')}:00`;
    const msg =
        `📍 ${room?.name}  (${room?.floor} · 최대 ${room?.capacity}인)\n` +
        `🛠 시설: ${room?.amenities?.join(', ')}\n\n` +
        `🗓 ${today}  ${t} ~ ${endTime} (1시간)\n\n` +
        `위 일정으로 예약하시겠습니까?`;
    if (!window.confirm(msg)) return;

    // 확인 — 즉시 예약 처리
    setSelectedTime(t);
    setReserving(true);
    try {
      await reserveStudyRoom(selectedId, {date: today, startHour: parseInt(t)});
      setLocalOccupied(prev => [...prev, t]);
      setMyReservedTimes(prev => [...prev, t]); // 모든 방 시간대 추적
      setDone(true);
    } catch (e) {
      // 서버 에러 메시지 그대로 표시 (시간중복, 방중복 등)
      alert(e.message || '예약에 실패했습니다.');
    } finally {
      setReserving(false);
    }
  }

  // 하단 예약 버튼은 confirm 흐름으로 대체 — 사용 안 함
  async function handleReserve() {
  }

  if (loading) return <LoadingBox/>;
  if (error) return <ErrorBox msg={error} onRetry={load}/>;

  const selected = rooms.find(r => r.id === selectedId);

  return (
      <div className="caf-foodcourt">
        {/* 좌측: 스터디룸 목록 */}
        <div className="floor-list">
          {rooms.map(r => {
            const isActive = r.id === selectedId;
            const sColor = studyStatusColor(r.status);
            return (
                <div
                    key={r.id}
                    className={`floor-item${isActive ? ' active' : ''}`}
                    onClick={() => handleSelectRoom(r)}
                    style={{
                      opacity: r.status !== 'AVAILABLE' ? 0.55 : 1,
                      cursor: r.status !== 'AVAILABLE' ? 'not-allowed' : 'pointer',
                    }}
                >
                  <div className="floor-num" style={{fontSize: '1.4rem', minWidth: 40}}>🚪</div>
                  <div style={{flex: 1}}>
                    <div style={{fontWeight: 500, fontSize: '.9rem'}}>{r.name}</div>
                    <div className="floor-desc">{r.floor} · 최대 {r.capacity}인</div>
                    <div className="floor-desc">{r.amenities?.join(' · ')}</div>
                  </div>
                  <div style={{
                    padding: '2px 8px', borderRadius: 10, fontSize: '.65rem', fontWeight: 700,
                    background: isActive ? 'rgba(255,255,255,.2)' : sColor + '20',
                    color: isActive ? '#fff' : sColor,
                    border: `1px solid ${isActive ? 'rgba(255,255,255,.3)' : sColor + '40'}`,
                    whiteSpace: 'nowrap',
                  }}>
                    {studyStatusLabel(r.status)}
                  </div>
                </div>
            );
          })}
        </div>

        {/* 우측: 시간 선택 */}
        <div className="floor-detail">
          {!selected && <div style={{padding: 40, color: '#718096'}}>스터디룸을 선택해주세요.</div>}
          {selected && slotLoading && <LoadingBox/>}
          {selected && !slotLoading && !done && slotData && (<>
            <div className="floor-detail-header">
              <h3>{selected.name}</h3>
              <div className="floor-subtitle">{today} · {selected.floor} · 최대 {selected.capacity}인</div>
            </div>
            <div className="room-category">
              <div className="room-category-title">
                시간 선택
                <span className="room-category-count">1시간 단위</span>
              </div>
              <div style={{display: 'grid', gridTemplateColumns: 'repeat(5,1fr)', gap: 8, marginBottom: 20}}>
                {STUDY_TIMES.map(t => {
                  // occupied: 이 방 예약 + 로컬 예약 + 내가 다른 방에서 같은 시간 예약
                  const occupied = slotData?.occupiedSlots?.includes(t) || localOccupied.includes(t) || myReservedTimes.includes(t);
                  const isActive = selectedTime === t;
                  return (
                      <button
                          key={t}
                          disabled={occupied}
                          onClick={() => handleTimeClick(t, occupied)}
                          style={{
                            padding: '10px 6px', borderRadius: 8,
                            border: isActive ? '2px solid #2c5282' : '1.5px solid #e2e8f0',
                            background: isActive ? '#ebf8ff' : occupied ? '#f7fafc' : '#fff',
                            color: isActive ? '#1a365d' : occupied ? '#cbd5e0' : '#4a5568',
                            fontWeight: isActive ? 700 : 400,
                            fontSize: '.82rem', cursor: occupied ? 'not-allowed' : 'pointer',
                            fontFamily: 'Noto Sans KR, sans-serif', transition: 'all .15s',
                          }}
                      >
                        {t}
                        {occupied && <div style={{fontSize: '.6rem', marginTop: 2, color: '#cbd5e0'}}>예약됨</div>}
                      </button>
                  );
                })}
              </div>
              <button
                  className="btn btn-primary w-100"
                  disabled={true}
                  style={{borderRadius: 8, opacity: selectedTime ? 0 : 0.45, display: selectedTime ? 'none' : 'block'}}
              >
                시간을 클릭하면 바로 예약 확인창이 뜹니다
              </button>
            </div>
          </>)}
          {selected && done && (
              <div style={{padding: '60px 40px', textAlign: 'center'}}>
                <div style={{fontSize: '3rem', marginBottom: 12}}>✅</div>
                <div style={{fontSize: '1.2rem', fontWeight: 900, color: '#1a365d', marginBottom: 8}}>예약 완료!</div>
                <div style={{color: '#718096', fontSize: '.9rem'}}>{selected.name} · {today} · {selectedTime} (1시간)
                </div>
                <div style={{color: '#a0aec0', fontSize: '.78rem', marginTop: 4}}>내 예약에서 확인하실 수 있습니다.</div>
                <button className="btn btn-outline-secondary mt-4"
                        onClick={() => {
                          setDone(false);
                          setSelectedId(null);
                          setSelectedTime(null);
                        }}>
                  새 예약하기
                </button>
              </div>
          )}
        </div>
      </div>
  );
}

// 4. 혼잡도

function CongestionSection() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const currentH = new Date().getHours();

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setData(await fetchCongestion());
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  if (loading) return <LoadingBox/>;
  if (error) return <ErrorBox msg={error} onRetry={load}/>;

  return (
      <div className="caf-foodcourt">
        <div className="floor-list">
          <div style={{padding: '16px 20px', background: '#f7fafc', borderBottom: '1px solid #edf2f7'}}>
            <div style={{
              fontSize: '.72rem',
              color: '#a0aec0',
              fontWeight: 700,
              letterSpacing: '.5px',
              marginBottom: 4,
            }}>전체 혼잡도
            </div>
            <div style={{fontSize: '2rem', fontWeight: 900, color: congestionColor(data.overallRate), lineHeight: 1.2}}>
              {data.overallRate}%
            </div>
            <span style={{
              display: 'inline-block', marginTop: 4, padding: '2px 10px', borderRadius: 20,
              fontSize: '.72rem', fontWeight: 600,
              background: congestionColor(data.overallRate) + '20',
              color: congestionColor(data.overallRate),
              border: `1px solid ${congestionColor(data.overallRate)}40`,
            }}>
            {congestionLabel(data.overallRate)}
          </span>
          </div>
          {/* 층 목록: 서버에서 4F→3F→2F→1F→B1 순서로 내려오므로 프론트 재정렬 불필요 */}
          {data.floors.map(f => (
              <div key={f.name} className="floor-item" style={{cursor: 'default'}}>
                <div style={{flex: 1}}>
                  <div style={{fontWeight: 500, fontSize: '.85rem'}}>{f.name}</div>
                  <div className="floor-desc">수용 {f.capacity}명</div>
                  <div style={{marginTop: 5, background: '#edf2f7', borderRadius: 4, height: 4, overflow: 'hidden'}}>
                    <div style={{
                      width: `${f.rate}%`,
                      background: congestionColor(f.rate),
                      height: '100%',
                      borderRadius: 4,
                      transition: 'width .6s',
                    }}/>
                  </div>
                </div>
                <div className="floor-pin" style={{
                  background: congestionColor(f.rate) + '20',
                  color: congestionColor(f.rate),
                  border: `1px solid ${congestionColor(f.rate)}40`,
                  fontWeight: 700, fontSize: '.72rem',
                }}>
                  {f.rate}%
                </div>
              </div>
          ))}
        </div>
        <div className="floor-detail">
          <div className="floor-detail-header">
            <h3>시간대별 혼잡도 추이</h3>
            <div className="floor-subtitle">파란색 바가 현재 시간대</div>
          </div>
          <div className="room-category">
            <div className="room-category-title">시간대별 현황</div>
            <div style={{display: 'flex', alignItems: 'flex-end', gap: 4, height: 150, marginBottom: 8}}>
              {data.hourlyTrend.map(({hour, rate}) => {
                const isCurrent = parseInt(hour) === currentH;
                return (
                    <div key={hour}
                         style={{flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4}}>
                      <div style={{
                        width: '100%', height: `${rate * 1.3}px`,
                        background: isCurrent ? '#2c5282' : congestionColor(rate) + '70',
                        border: isCurrent ? '1.5px solid #2c5282' : 'none',
                        borderRadius: '3px 3px 0 0', minHeight: 4, transition: 'height .5s',
                      }}/>
                      <div style={{
                        fontSize: '.58rem',
                        color: isCurrent ? '#2c5282' : '#a0aec0',
                        fontWeight: isCurrent ? 700 : 400,
                      }}>
                        {hour}
                      </div>
                    </div>
                );
              })}
            </div>
            <div style={{
              marginTop: 16,
              padding: '14px 16px',
              background: '#f7fafc',
              borderRadius: 8,
              border: '1px solid #e2e8f0',
            }}>
              <div style={{fontSize: '.75rem', color: '#a0aec0'}}>💡 추천 방문 시간</div>
              <div style={{fontWeight: 600, fontSize: '.9rem', color: '#2d3748', marginTop: 3}}>오전 9시 이전 또는 오후 7시 이후
              </div>
            </div>
          </div>
        </div>
      </div>
  );
}

// 5. 공지사항

function NoticesSection() {
  const [notices, setNotices] = useState([]);
  const [selectedId, setSelectedId] = useState(null);
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [detailLoading, setDetailLoading] = useState(false);
  const [error, setError] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchNotices();
      setNotices(data);
      if (data.length > 0) await handleSelect(data[0]);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  async function handleSelect(notice) {
    setSelectedId(notice.id);
    setDetailLoading(true);
    setDetail(null);
    try {
      setDetail(await fetchNotice(notice.id));
    } catch {
      setDetail(notice);
    } finally {
      setDetailLoading(false);
    }
  }

  if (loading) return <LoadingBox/>;
  if (error) return <ErrorBox msg={error} onRetry={load}/>;

  return (
      <div className="caf-foodcourt">
        <div className="floor-list">
          {notices.map(n => {
            const isActive = n.id === selectedId;
            const tColor = noticeTypeColor(n.type);
            return (
                <div key={n.id} className={`floor-item${isActive ? ' active' : ''}`} onClick={() => handleSelect(n)}>
                  <div style={{flex: 1}}>
                    <div style={{display: 'flex', alignItems: 'center', gap: 6, marginBottom: 4}}>
                  <span style={{
                    padding: '1px 7px', borderRadius: 10, fontSize: '.65rem', fontWeight: 600,
                    background: isActive ? 'rgba(255,255,255,.25)' : tColor + '20',
                    color: isActive ? '#fff' : tColor,
                    border: `1px solid ${isActive ? 'rgba(255,255,255,.3)' : tColor + '40'}`,
                  }}>
                    {n.type}
                  </span>
                    </div>
                    <div style={{fontWeight: 500, fontSize: '.85rem', lineHeight: 1.4}}>{n.title}</div>
                    <div className="floor-desc" style={{marginTop: 3}}>{n.date} · 조회 {n.views?.toLocaleString()}</div>
                  </div>
                </div>
            );
          })}
        </div>
        <div className="floor-detail">
          {!selectedId && <div style={{padding: 40, color: '#718096'}}>공지를 선택해주세요.</div>}
          {selectedId && detailLoading && <LoadingBox/>}
          {selectedId && !detailLoading && detail && (<>
            <div className="floor-detail-header">
            <span style={{
              display: 'inline-block', marginBottom: 10, padding: '2px 10px', borderRadius: 20,
              fontSize: '.75rem', fontWeight: 600,
              background: noticeTypeColor(detail.type) + '15',
              color: noticeTypeColor(detail.type),
              border: `1px solid ${noticeTypeColor(detail.type)}40`,
            }}>
              {detail.type}
            </span>
              <h3 style={{lineHeight: 1.5}}>{detail.title}</h3>
              <div className="floor-subtitle">{detail.date} · 조회 {detail.views?.toLocaleString()}</div>
            </div>
            <div className="room-category">
              <div style={{
                borderTop: '1px solid #e2e8f0',
                paddingTop: 16,
                fontSize: '.9rem',
                color: '#4a5568',
                lineHeight: 1.9,
              }}>
                {detail.content}
              </div>
            </div>
          </>)}
        </div>
      </div>
  );
}

// ── 메인: LibraryPage ──

export function LibraryPage({building}) {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('reading');
  const [tabKey, setTabKey] = useState(0); // 같은 탭 재클릭 시 리셋용
  const [showAuth, setShowAuth] = useState(false); // 로그인 모달 제어
  const [summaryData, setSummaryData] = useState(null); // 상단 현황 데이터
  const now = new Date();

  // 상단 "현재 이용 현황" 5줄 — 실제 API 데이터 기반
  // 열람실 4개(디지털/제2/제1/야간) + 스터디룸 가용 수를 한눈에 표시
  // name 기반 조회: DB ID 순서 변동에 안전 (DevLoader 시딩 순서 무관)
  useEffect(() => {
    (async () => {
      try {
        const [rooms, studyRooms] = await Promise.all([
          fetchReadingRooms(),
          fetchStudyRooms(),
        ]);
        // ReadingSection의 rate 계산과 완전 동일: Math.min(round(usedSeats/totalSeats*100), 100)
        const calcRate = r => r ? Math.min(Math.round((r.usedSeats / r.totalSeats) * 100), 100) : 0;
        const room1 = rooms.find(r => r.name === '제1열람실');
        const room2 = rooms.find(r => r.name === '제2열람실');
        const room4 = rooms.find(r => r.name === '디지털열람실');
        const roomB1 = rooms.find(r => r.name === '야간열람실');
        const availStudy = studyRooms.filter(r => r.status === 'AVAILABLE').length;
        setSummaryData({
          r1Rate: calcRate(room1),
          r2Rate: calcRate(room2),
          r4Rate: calcRate(room4),
          rB1Rate: calcRate(roomB1),
          availStudy,
          totalStudy: studyRooms.length,
        });
      } catch { /* 실패해도 무시 — 하드코딩 fallback */
      }
    })();
  }, []);

  // 로그인 필요 시 호출 — alert 이후 모달 띄우기
  function handleNeedLogin() {
    setShowAuth(true);
  }

  function renderTab() {
    switch (activeTab) {
      case 'reading':
        return <ReadingSection key={tabKey} onNeedLogin={handleNeedLogin}/>;
      case 'books':
        return <BooksSection key={tabKey} onNeedLogin={handleNeedLogin}/>;
      case 'study':
        return <StudySection key={tabKey} onNeedLogin={handleNeedLogin}/>;
      case 'congestion':
        return <CongestionSection key={tabKey}/>;
      case 'notices':
        return <NoticesSection key={tabKey}/>;
      default:
        return null;
    }
  }

  return (
      <>
        <div id="libraryView">
          {/* ── 브레드크럼 ── */}
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
              <span className="current">{building?.name ?? '도서관'}</span>
              <span className="sep">/</span>
              <span className="current">{NAV_TABS.find(t => t.key === activeTab)?.label}</span>
            </div>
          </div>

          {/* ── 상단 정보 ── */}
          <div className="store-info-area">
            <div className="container">
              <div className="store-name">
                <span>{building?.name ?? '도서관'}</span>
                <a className="back-link" onClick={() => navigate('/')}>← 캠퍼스 지도로 돌아가기</a>
              </div>
              <div className="info-grid">
                <div className="info-col">
                  <div className="semester-box">
                    <div className="semester-icon">📚</div>
                    <div className="semester-label">시설 안내</div>
                    <div className="semester-value">열람실 · 자료실 · 스터디룸</div>
                    <div className="semester-sub">지하 1층 ~ 지상 4층</div>
                  </div>
                </div>
                <div className="info-col">
                  <ul className="hours-list">
                    <li><span className="hours-icon">⏰</span> 평일 07:30 ~ 23:00</li>
                    <li><span className="hours-icon">⏰</span> 토요일 09:00 ~ 18:00</li>
                    <li><span className="hours-icon">⏰</span> 일·공휴일 11:00 ~ 17:00</li>
                    <li><span className="hours-icon">📋</span> 스터디룸 예약: 정시 1시간 단위</li>
                  </ul>
                  <div className="hours-today">
                    오늘은 <strong>{now.getMonth() + 1}월 {now.getDate()}일</strong>입니다.
                  </div>
                </div>
                <div className="info-col">
                  <div className="caf-today-summary">
                    <div className="caf-summary-title">현재 이용 현황</div>
                    {[
                      {
                        icon: '💻',
                        label: '디지털열람실',
                        value: summaryData ? `${summaryData.r4Rate}% ${congestionLabel(summaryData.r4Rate)}` : '로딩 중...',
                      },
                      {
                        icon: '📚',
                        label: '제2열람실',
                        value: summaryData ? `${summaryData.r2Rate}% ${congestionLabel(summaryData.r2Rate)}` : '로딩 중...',
                      },
                      {
                        icon: '📚',
                        label: '제1열람실',
                        value: summaryData ? `${summaryData.r1Rate}% ${congestionLabel(summaryData.r1Rate)}` : '로딩 중...',
                      },
                      {
                        icon: '🌙',
                        label: '야간열람실',
                        value: summaryData ? `${summaryData.rB1Rate}% ${congestionLabel(summaryData.rB1Rate)}` : '로딩 중...',
                      },
                      {
                        icon: '🚪',
                        label: '스터디룸',
                        value: summaryData ? `${summaryData.availStudy}/${summaryData.totalStudy} 사용 가능` : '로딩 중...',
                      },
                    ].map(item => (
                        <div key={item.label} className="caf-summary-row">
                          <span className="caf-summary-icon">{item.icon}</span>
                          <span className="caf-summary-type">{item.label}</span>
                          <span className="caf-summary-time">{item.value}</span>
                        </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* ── 탭 ── */}
          <div className="building-tabs">
            {NAV_TABS.map(tab => (
                <button
                    key={tab.key}
                    className={activeTab === tab.key ? 'active' : ''}
                    onClick={() => {
                      if (activeTab === tab.key) setTabKey(k => k + 1);
                      else setActiveTab(tab.key);
                    }}
                >
                  {tab.label}
                </button>
            ))}
          </div>

          {/* ── 탭 콘텐츠 ── */}
          {renderTab()}
        </div>

        {/* ── 로그인 모달 ── */}
        {showAuth && <AuthModal onClose={() => setShowAuth(false)}/>}
      </>
  );
}
