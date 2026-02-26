import {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {fetchFoodCourtStores, fetchTodayMeals} from '../data/api.js';

// 푸드코트 가게 카테고리 → 아이콘 매핑 (cafeteria.js의 category 필드와 대응)
const STORE_ICONS = {
  '한식': '🍱', '분식': '🍢', '면류': '🍜', '치킨': '🍗', '돈까스': '🥩', '양식': '🍕', '백반': '🍚', '카페': '☕',
};

/// 식당 페이지 — 캠퍼스 지도에서 식당 건물 클릭 시 이동 (customRoute: '/cafeteria')
/// - 오늘의 학식 탭: 조식/중식/석식 카드 3개 (메뉴 + 가격 + 할인)
/// - 푸드코트 탭: 좌측 가게 목록 + 우측 메뉴 상세 (BuildingPage의 floor-list/floor-detail 패턴 재활용)
/// - 레이아웃: breadcrumb → info-area → tabs → content (BuildingPage와 동일 구조)
/// - TODO: 백엔드 /api/cafeteria API 구현 후 mock 데이터 교체
export function CafeteriaPage() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('meal');
  const [selectedStore, setSelectedStore] = useState(null);
  const [mealsData, setMealsData] = useState(null);
  const [stores, setStores] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([fetchTodayMeals(), fetchFoodCourtStores()])
        .then(([meals, storeList]) => {
          setMealsData(meals);
          setStores(storeList);
          if (storeList.length > 0) setSelectedStore(storeList[0].id);
        })
        .finally(() => setLoading(false));
  }, []);

  const now = new Date();
  const store = stores.find(s => s.id === selectedStore);

  if (loading) {
    return <div style={{padding: 40, textAlign: 'center', color: '#718096'}}>로딩 중...</div>;
  }

  return (<div id="cafeteriaView">
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
        <span className="current">식당</span>
        <span className="sep">/</span>
        <span className="current">{activeTab === 'meal' ? '오늘의 학식' : '푸드코트'}</span>
      </div>
    </div>

    {/* 상단 정보 영역 */}
    <div className="store-info-area">
      <div className="container">
        <div className="store-name">
          <span>식당</span>
          <a className="back-link" onClick={() => navigate('/')}>← 캠퍼스 지도로 돌아가기</a>
        </div>
        <div className="info-grid">
          <div className="info-col">
            <div className="semester-box">
              <div className="semester-icon">🍽️</div>
              <div className="semester-label">시설 안내</div>
              <div className="semester-value">학생식당 · 푸드코트</div>
              <div className="semester-sub">1층 단일 건물</div>
            </div>
          </div>
          <div className="info-col">
            <ul className="hours-list">
              <li><span className="hours-icon">⏰</span> 학생식당 08:00 ~ 19:00</li>
              <li><span className="hours-icon">⏰</span> 푸드코트 09:00 ~ 21:00</li>
              <li><span className="hours-icon">📋</span> 주말 및 공휴일 휴무</li>
              <li><span className="hours-icon">ℹ️</span> 메뉴는 매일 변경됩니다</li>
            </ul>
            <div className="hours-today">
              오늘은 <strong>{now.getMonth() + 1}월 {now.getDate()}일</strong>입니다.
            </div>
          </div>
          <div className="info-col">
            <div className="caf-today-summary">
              <div className="caf-summary-title">오늘의 학식 요약</div>
              {(mealsData?.meals || []).map(meal => (
                  <div key={meal.type} className="caf-summary-row">
                    <span className="caf-summary-icon">{meal.icon}</span>
                    <span className="caf-summary-type">{meal.type}</span>
                    <span className="caf-summary-time">{meal.time}</span>
                  </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>

    {/* 탭 */}
    <div className="building-tabs">
      <button className={activeTab === 'meal' ? 'active' : ''} onClick={() => setActiveTab('meal')}>
        오늘의 학식
      </button>
      <button className={activeTab === 'foodcourt' ? 'active' : ''} onClick={() => setActiveTab('foodcourt')}>
        푸드코트
      </button>
    </div>

    {/* 탭 콘텐츠 */}
    {activeTab === 'meal'
        ? <MealSection meals={mealsData?.meals || []}/>
        : <FoodCourtSection stores={stores} selectedStore={selectedStore}
                            onSelectStore={setSelectedStore} store={store}/>
    }
  </div>);
}

/// 오늘의 학식 — 조식/중식/석식 카드 3개
function MealSection({meals}) {
  return (
      <div className="caf-meal-section">
        <div className="container">
          <div className="caf-meal-grid">
            {meals.map(meal => (
                <div key={meal.type} className="caf-meal-card">
                  <div className="caf-meal-header">
                    <span className="caf-meal-icon">{meal.icon}</span>
                    <div>
                      <div className="caf-meal-type">{meal.type}</div>
                      <div className="caf-meal-time">{meal.time}</div>
                    </div>
                  </div>
                  <div className="caf-meal-items">
                    {meal.items.map((item, i) => (
                        <div key={i} className={`caf-meal-item${item.discount ? ' has-discount' : ''}`}>
                          <div className="caf-item-name">
                            {item.name}
                            {item.discount && <span className="caf-discount-badge">{item.discount.label}</span>}
                          </div>
                          <div className="caf-item-price">
                            {item.discount ? (<>
                              <span className="caf-price-original">{item.price.toLocaleString()}원</span>
                              <span className="caf-price-discount">{item.discount.price.toLocaleString()}원</span>
                            </>) : (
                                <span>{item.price.toLocaleString()}원</span>
                            )}
                          </div>
                        </div>
                    ))}
                  </div>
                </div>
            ))}
          </div>
        </div>
      </div>
  )
}

/// 푸드코트 — 좌측 가게 목록 + 우측 메뉴 상세
function FoodCourtSection({stores, selectedStore, onSelectStore, store}) {
  return (
      <div className="caf-foodcourt">
        {/* 좌측: 가게 리스트 */}
        <div className="floor-list">
          {stores.map(s => (
              <div key={s.id}
                   className={`floor-item${s.id === selectedStore ? ' active' : ''}`}
                   onClick={() => onSelectStore(s.id)}>
                <div className="floor-num" style={{fontSize: '1.6rem'}}>
                  {STORE_ICONS[s.category] || '🍽️'}
                </div>
                <div>
                  <div style={{fontWeight: 500, fontSize: '0.9rem'}}>{s.name}</div>
                  <div className="floor-desc">{s.category} · {s.hours}</div>
                </div>
                <div className="floor-pin">{s.menu.length}</div>
              </div>
          ))}
        </div>

        {/* 우측: 선택된 가게 메뉴 상세 */}
        <div className="floor-detail">
          {store ? (<>
            <div className="floor-detail-header">
              <h3>{store.name}</h3>
              <div className="floor-subtitle">{store.desc} · {store.hours}</div>
            </div>

            {/* 전체 메뉴 */}
            <div className="room-category">
              <div className="room-category-title">
                메뉴<span className="room-category-count">{store.menu.length}</span>
              </div>
              {store.menu.map((item, i) => (
                  <div key={i} className={`room-row caf-menu-row${item.discount ? ' caf-has-discount' : ''}`}>
                    <div className="d-flex align-items-center">
                      {item.popular
                          ? <div className="caf-popular-dot"/>
                          : <div style={{width: 10, marginRight: 10}}/>
                      }
                      <div>
                        <div className="room-name">
                          {item.name}
                          {item.popular && <span className="caf-popular-tag">인기</span>}
                        </div>
                        {item.discount && <div className="caf-menu-discount-label">{item.discount.label}</div>}
                      </div>
                    </div>
                    <div className="caf-menu-price">
                      {item.discount ? (<>
                        <span className="caf-price-original">{item.price.toLocaleString()}원</span>
                        <span className="caf-price-discount">{item.discount.price.toLocaleString()}원</span>
                      </>) : (
                          <span>{item.price.toLocaleString()}원</span>
                      )}
                    </div>
                  </div>
              ))}
            </div>

            {/* 할인 메뉴 요약 */}
            {store.menu.some(m => m.discount) && (
                <div className="caf-discount-summary">
                  <div className="room-category-title">
                    할인 메뉴<span className="room-category-count">{store.menu.filter(m => m.discount).length}</span>
                  </div>
                  {store.menu.filter(m => m.discount).map((item, i) => (
                      <div key={i} className="caf-discount-item">
                        <span className="caf-discount-badge">{item.discount.label}</span>
                        <span className="caf-discount-name">{item.name}</span>
                        <span className="caf-discount-save">
                    {(item.price - item.discount.price).toLocaleString()}원 할인
                  </span>
                      </div>
                  ))}
                </div>
            )}
          </>) : (
              <div style={{padding: 40, color: '#718096'}}>가게를 선택해주세요.</div>
          )}
        </div>
      </div>
  )
}
