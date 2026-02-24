import {useRef, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {BUILDING_POLYGONS} from '../data/buildings.js';

/// 캠퍼스 지도 페이지
/// - SVG polygon overlay로 건물 영역 표시 (좌표는 buildings.js에 정의)
/// - 건물 hover 시 툴팁(이름, 정보, 대여 가능 여부) 표시
/// - 대여 가능 건물 클릭 시 /building/:key 상세 페이지로 이동
/// - TODO: 백엔드 /api/buildings/map 구현 후 API 데이터로 교체
export function CampusMapPage() {
  const buildings = BUILDING_POLYGONS;
  const [tooltip, setTooltip] = useState({visible: false, x: 0, y: 0, name: '', info: '', rentable: false});
  const mapRef = useRef(null);
  const navigate = useNavigate();

  function handleMouseEnter(b) {
    setTooltip(prev => ({...prev, visible: true, name: b.name, info: b.info, rentable: b.rentable}));
  }

  // 툴팁이 지도 영역 밖으로 넘어가지 않도록 위치 보정
  function handleMouseMove(e) {
    const rect = mapRef.current.getBoundingClientRect();
    let x = e.clientX - rect.left + 15;
    let y = e.clientY - rect.top - 10;
    if (x + 220 > rect.width) x = e.clientX - rect.left - 220;
    setTooltip(prev => ({...prev, x, y}));
  }

  function handleMouseLeave() {
    setTooltip(prev => ({...prev, visible: false}));
  }

  // customRoute가 있으면 해당 경로로, 없으면 기본 /building/:key로 이동
  // 현재 customRoute 사용 건물: 식당 → /cafeteria
  function handleClick(b) {
    if (!b.rentable) return;
    navigate(b.customRoute || `/building/${b.key}`);
  }

  return (<div id="mapView">
    <div className="container mt-4">
      <p className="map-instruction">
        건물 위에 마우스를 올리면 정보를 확인할 수 있습니다. <strong>대여 가능 건물</strong>을 클릭하면 층별 안내로 이동합니다.
      </p>
    </div>

    <div ref={mapRef} className="map-container">
      <img src="/assets/img/campus_map.jpg" alt="캠퍼스 지도" style={{width: '100%', display: 'block'}}/>

      <svg className="map-overlay" viewBox="0 0 900 582" preserveAspectRatio="xMidYMid meet">
        {buildings.map(b => (<polygon
            key={b.key}
            className={b.rentable ? 'rentable' : 'not-rentable'}
            points={b.points}
            onMouseEnter={() => handleMouseEnter(b)}
            onMouseMove={handleMouseMove}
            onMouseLeave={handleMouseLeave}
            onClick={() => handleClick(b)}
        />))}
      </svg>

      <div className={`building-tooltip${tooltip.visible ? ' visible' : ''}`}
           style={{left: tooltip.x, top: tooltip.y}}>
        <div className="tt-name">{tooltip.name}</div>
        <div className="tt-info">{tooltip.info}</div>
        <span className={`tt-badge ${tooltip.rentable ? 'yes' : 'no'}`}>
            {tooltip.rentable ? '✓ 클릭하여 상세보기' : '✕ 대여 불가'}
          </span>
      </div>

      <div className="map-legend">
        <div className="legend-item">
          <div className="legend-color" style={{background: 'rgba(66,133,244,.35)'}}/>
          <span>대여 가능</span>
        </div>
        <div className="legend-item">
          <div className="legend-color" style={{background: 'rgba(150,150,150,.3)'}}/>
          <span>대여 불가</span>
        </div>
      </div>
    </div>
  </div>);
}
