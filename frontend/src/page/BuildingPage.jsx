import {useState} from 'react';
import {useNavigate, useParams} from 'react-router-dom';
import {FloorGuide} from '../components/FloorGuide.jsx';
import {MiniCalendar} from '../components/MiniCalendar.jsx';
import {ReservationView} from '../components/ReservationView.jsx';
import {BUILDING_DATA, getCurrentSemester} from '../data/buildings.js';

const MONTH_NAMES = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];

/// 건물 상세 페이지
/// - URL 파라미터 :buildingKey로 건물 식별 (e.g. 'lecture1', 'gym')
/// - 층별 안내 탭: FloorGuide 컴포넌트 (호실 목록 + 상태)
/// - 예약 현황 탭: ReservationView 컴포넌트 (타임라인 UI)
/// - TODO: 백엔드 API 구현 후 mock 데이터(buildings.js)를 API 호출로 교체
export function BuildingPage() {
  const {buildingKey} = useParams();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('floor');
  const [jumpToRoom, setJumpToRoom] = useState(null);

  // 백엔드 미구현으로 mock 데이터 직접 참조
  const data = BUILDING_DATA[buildingKey];
  const semester = getCurrentSemester();

  if (!data) return <div className="container mt-4">건물 정보를 찾을 수 없습니다.</div>;

  const now = new Date();
  const y = now.getFullYear();
  const m = now.getMonth();

  function handleTabChange(tab) {
    setActiveTab(tab);
    setJumpToRoom(null);
  }

  // 층별 안내에서 호실 클릭 시 → 예약 현황 탭으로 전환 + 해당 호실 자동 선택
  function handleRoomClick(roomId) {
    setJumpToRoom(roomId);
    setActiveTab('schedule');
  }

  return (<div id="buildingView" className="active">
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
        <span className="current">{data.name}</span>
        <span className="sep">/</span>
        <span className="current">{activeTab === 'floor' ? '층별 안내' : '예약 현황'}</span>
      </div>
    </div>

    <div className="store-info-area">
      <div className="container">
        <div className="store-name">
          <span>{data.name}</span>
          <a className="back-link" onClick={() => navigate('/')}>← 캠퍼스 지도로 돌아가기</a>
        </div>
        <div className="info-grid">
          <div className="info-col">
            <div className="semester-box">
              <div className="semester-icon">📚</div>
              <div className="semester-label">현재 학기</div>
              <div className="semester-value">{semester?.name || ''}</div>
              <div className="semester-sub">
                {semester ? `${semester.start.replace(/-/g, '.')} ~ ${semester.end.replace(/-/g, '.')}` : ''}
              </div>
            </div>
          </div>
          <div className="info-col">
            <ul className="hours-list">
              <li><span className="hours-icon">⏰</span> 평일 이용시간 09:00 ~ 22:00</li>
              <li><span className="hours-icon">⏰</span> 주말 이용시간 10:00 ~ 18:00</li>
              <li><span className="hours-icon">📋</span> 예약 가능 단위: 정시 1시간</li>
              <li><span className="hours-icon">ℹ️</span> 시설 예약은 관리자 승인 후 확정</li>
            </ul>
            <div className="hours-today">오늘은 <strong>09:00</strong>부터 <strong>22:00</strong>까지 이용 가능합니다.</div>
          </div>
          <div className="info-col">
            <div className="calendar-box">
              <div className="cal-header">
                <span className="cal-month-num">{String(m + 1).padStart(2, '0')}</span>
                <div>
                  <div className="cal-month-name">{MONTH_NAMES[m]}</div>
                  <div className="cal-year">{y}</div>
                </div>
              </div>
              <MiniCalendar buildingKey={buildingKey}/>
              <div className="cal-legend">
                <div className="cal-legend-item">
                  <div className="cal-legend-dot full"/>
                  마감
                </div>
                <div className="cal-legend-item">
                  <div className="cal-legend-dot partial"/>
                  일부
                </div>
                <div className="cal-legend-item">
                  <div className="cal-legend-dot empty"/>
                  가능
                </div>
                <div className="cal-legend-item">
                  <div className="cal-legend-dot today-dot"/>
                  오늘
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div className="building-tabs">
      <button className={activeTab === 'floor' ? 'active' : ''} onClick={() => handleTabChange('floor')}>층별 안내
      </button>
      <button className={activeTab === 'schedule' ? 'active' : ''} onClick={() => handleTabChange('schedule')}>예약
        현황
      </button>
    </div>

    {activeTab === 'floor' ? (<FloorGuide buildingData={data} onRoomClick={handleRoomClick}/>) : (
        <ReservationView buildingKey={buildingKey} buildingData={data} jumpToRoom={jumpToRoom}/>)}
  </div>);
}
