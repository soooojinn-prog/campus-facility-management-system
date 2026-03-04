import {useState} from 'react';
import {useAuth} from '../context/AuthContext.jsx';
import {loginApi, signupApi} from '../data/api.js';

/// 한글 실명 검증 (2~5자, 완성형 한글만 허용)
function isKorean(str) {
  return /^[가-힣]{2,5}$/.test(str);
}

/// 로그인/회원가입 모달
/// - 로그인: 학번(userNumber) + 비밀번호 → 백엔드 /api/auth/login
/// - 회원가입: 역할 선택 + 이름 + 학번 + 이메일 + 비밀번호 → 백엔드 /api/auth/register
/// - 회원가입 성공 시 자동 로그인 없이 로그인 탭으로 이동 (백엔드가 201만 반환)
export function AuthModal({onClose}) {
  const {login} = useAuth();
  const [tab, setTab] = useState('login'); // 'login' | 'signup'
  const [selectedRole, setSelectedRole] = useState(null);
  const [selectedGender, setSelectedGender] = useState(null);

  // 로그인 폼
  const [loginId, setLoginId] = useState('');
  const [loginPw, setLoginPw] = useState('');

  // 회원가입 폼
  const [signupName, setSignupName] = useState('');
  const [signupId, setSignupId] = useState('');
  const [signupEmail, setSignupEmail] = useState('');
  const [signupPw, setSignupPw] = useState('');
  const [signupPw2, setSignupPw2] = useState('');

  async function handleLogin() {
    if (!loginId || !loginPw) {
      alert('아이디와 비밀번호를 입력해주세요.');
      return;
    }
    try {
      // 백엔드 응답: { accessToken, user: {...} } → user만 추출하여 Context에 저장
      const res = await loginApi(loginId, loginPw);
      login(res.user);
      onClose();
    } catch (e) {
      alert('아이디 또는 비밀번호가 올바르지 않습니다.');
    }
  }

  async function handleSignup() {
    if (!selectedRole) {
      alert('회원 유형을 선택해주세요.');
      return;
    }
    if (!selectedGender) {
      alert('성별을 선택해주세요.');
      return;
    }
    if (!isKorean(signupName)) {
      alert('이름은 한글 실명(2~5자)으로 입력해주세요.\n닉네임은 사용할 수 없습니다.');
      return;
    }
    if (!signupId) {
      alert('학번/교번을 입력해주세요.');
      return;
    }
    if (!signupEmail) {
      alert('이메일을 입력해주세요.');
      return;
    }
    if (signupPw.length < 8) {
      alert('비밀번호는 8자 이상이어야 합니다.');
      return;
    }
    if (signupPw !== signupPw2) {
      alert('비밀번호가 일치하지 않습니다.');
      return;
    }

    try {
      const role = selectedRole === '교수' ? 'ROLE_PROFESSOR' : 'ROLE_STUDENT';
      await signupApi(signupId, signupPw, signupName, signupEmail, selectedGender, role);
      alert(`회원가입이 완료되었습니다!\n${signupName}님, 로그인해주세요.`);
      setTab('login');
    } catch (e) {
      alert(e.message || '회원가입에 실패했습니다.');
    }
  }

  const roles = [{key: '학생', icon: '🎓', label: '학생', desc: '동아리 가입 가능\n시설 예약 및 상담 예약 가능'}, {
    key: '교수', icon: '👨‍🏫', label: '교수', desc: '수업 개설\n반복 예약 가능',
  }];

  return (<div className="modal-bg show" onClick={e => {
    if (e.target === e.currentTarget) onClose();
  }}>
    <div className="modal-box" style={{width: 440}}>
      <div className="modal-hd">
        <button className="modal-close" onClick={onClose}>✕</button>
        <h3>한빛대학교 시설 관리 시스템</h3>
      </div>
      <div className="modal-bd">
        {/* 탭 */}
        <div className="auth-tabs">
          <div className={`auth-tab${tab === 'login' ? ' active' : ''}`} onClick={() => setTab('login')}>로그인</div>
          <div className={`auth-tab${tab === 'signup' ? ' active' : ''}`} onClick={() => setTab('signup')}>회원가입
          </div>
        </div>

        {tab === 'login' ? (<form onSubmit={e => {
          e.preventDefault();
          handleLogin();
        }}>
          <div className="mb-3">
            <label className="form-label">학번 / 교번</label>
            <input type="text" className="form-control" placeholder="학번 또는 교번을 입력하세요" value={loginId}
                   onChange={e => setLoginId(e.target.value)}/>
          </div>
          <div className="mb-3">
            <label className="form-label">비밀번호</label>
            <input type="password" className="form-control" placeholder="비밀번호를 입력하세요" value={loginPw}
                   onChange={e => setLoginPw(e.target.value)}/>
          </div>
          <button type="submit" className="btn btn-primary w-100">로그인</button>
        </form>) : (<form onSubmit={e => {
          e.preventDefault();
          handleSignup();
        }}>
          <div className="mb-3">
            <label className="form-label">회원 유형 선택</label>
            <div className="role-cards">
              {roles.map(r => (<div
                  key={r.key}
                  className={`role-card${selectedRole === r.key ? ' selected' : ''}`}
                  onClick={() => setSelectedRole(r.key)}
              >
                <div className="role-icon">{r.icon}</div>
                <div className="role-label">{r.label}</div>
                <div className="role-desc">{r.desc.split('\n').map((line, i) => <span
                    key={i}>{line}<br/></span>)}</div>
              </div>))}
            </div>
          </div>
          <div className="mb-3">
            <label className="form-label">성별 선택</label>
            <div className="role-cards">
              {[{key: 'MALE', icon: '\uD83D\uDE4B\u200D\u2642\uFE0F', label: '남성'}, {
                key: 'FEMALE',
                icon: '\uD83D\uDE4B\u200D\u2640\uFE0F',
                label: '여성',
              }].map(g => (<div
                  key={g.key}
                  className={`role-card${selectedGender === g.key ? ' selected' : ''}`}
                  onClick={() => setSelectedGender(g.key)}
              >
                <div className="role-icon">{g.icon}</div>
                <div className="role-label">{g.label}</div>
              </div>))}
            </div>
          </div>
          <div className="mb-3">
            <label className="form-label">이름 (한글)</label>
            <input type="text" className="form-control" placeholder="실명을 입력하세요 (예: 홍길동)" value={signupName}
                   onChange={e => setSignupName(e.target.value)}/>
            <div className="form-text text-muted" style={{fontSize: '.75rem'}}>닉네임은 사용 불가하며, 반드시 한글 실명을
              입력해주세요.
            </div>
          </div>
          <div className="mb-3">
            <label className="form-label">학번 / 교번</label>
            <input type="text" className="form-control" placeholder="학번 또는 교번을 입력하세요" value={signupId}
                   onChange={e => setSignupId(e.target.value)}/>
          </div>
          <div className="mb-3">
            <label className="form-label">이메일</label>
            <input type="email" className="form-control" placeholder="이메일을 입력하세요" value={signupEmail}
                   onChange={e => setSignupEmail(e.target.value)}/>
          </div>
          <div className="mb-3">
            <label className="form-label">비밀번호</label>
            <input type="password" className="form-control" placeholder="8자 이상" value={signupPw}
                   onChange={e => setSignupPw(e.target.value)}/>
          </div>
          <div className="mb-3">
            <label className="form-label">비밀번호 확인</label>
            <input type="password" className="form-control" placeholder="비밀번호를 다시 입력하세요" value={signupPw2}
                   onChange={e => setSignupPw2(e.target.value)}/>
          </div>
          <button type="submit" className="btn btn-primary w-100">회원가입</button>
        </form>)}
      </div>
    </div>
  </div>);
}
