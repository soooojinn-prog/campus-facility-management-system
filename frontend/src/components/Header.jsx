import {useState} from 'react';
import {Link} from 'react-router-dom';
import {useAuth} from '../context/AuthContext.jsx';
import {AuthModal} from './AuthModal.jsx';

const ROLE_LABELS = {
  ROLE_STUDENT: '학생', ROLE_PROFESSOR: '교수', ROLE_ADMIN: '관리자',
};

export function Header() {
  const {currentUser, logout} = useAuth();
  const [showAuth, setShowAuth] = useState(false);
  const roleLabel = currentUser ? (ROLE_LABELS[currentUser.role] || currentUser.role) : '';

  return (<>
    <header className="campus-header">
      <div className="container d-flex align-items-center justify-content-between">
        <div>
          <h1 style={{fontSize: '1.4rem', fontWeight: 700, margin: 0}}>
            <Link to="/" style={{color: 'white', textDecoration: 'none'}}>
              Campus 시설 관리 시스템
            </Link>
          </h1>
          <div className="subtitle">캠퍼스 시설 예약 및 동아리 관리</div>
        </div>
        <div className="d-flex gap-2 align-items-center">
          {!currentUser ? <button className="btn btn-light btn-sm" onClick={() => setShowAuth(true)}>로그인</button> : (
              <div className="user-display">
                <span className="user-role">{roleLabel}</span>
                <span className="user-name">{currentUser.name}님 환영합니다</span>
                {currentUser.role === 'ROLE_STUDENT' &&
                    <Link to="/clubs" className="btn btn-outline-light btn-sm">동아리</Link>}
                <Link to="/mypage" className="btn btn-outline-light btn-sm">마이페이지</Link>
                {currentUser.role === 'ROLE_ADMIN' &&
                    <Link to="/admin" className="btn btn-outline-light btn-sm">관리페이지</Link>}
                <button className="btn btn-outline-light btn-sm" onClick={logout}>로그아웃</button>
              </div>)}
        </div>
      </div>
    </header>
    {showAuth && <AuthModal onClose={() => setShowAuth(false)}/>}
  </>);
}
