import {createContext, useContext, useState} from 'react';
import {logoutApi} from '../data/api.js';

const AuthContext = createContext(null);

/// 인증 상태를 전역으로 관리하는 Provider
/// currentUser 구조: { id, name, userNumber, role } (백엔드 ResponseUserSimpleInfo 참고)
/// login(): 로그인 응답의 user 객체를 저장
/// logout(): 백엔드 쿠키 만료 API 호출 후 상태 초기화
export function AuthProvider({children}) {
  const [currentUser, setCurrentUser] = useState(null);

  function login(user) {
    setCurrentUser(user);
  }

  function logout() {
    logoutApi().then(() => setCurrentUser(null));
  }

  return (<AuthContext.Provider value={{currentUser, login, logout}}>
    {children}
  </AuthContext.Provider>);
}

export function useAuth() {
  return useContext(AuthContext);
}
