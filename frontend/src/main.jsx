import {StrictMode} from 'react';
import {createRoot} from 'react-dom/client';
import {BrowserRouter, Route, Routes} from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';
import './styles/index.css';
import {Header} from './components/Header.jsx';
import {AuthProvider} from './context/AuthContext.jsx';
import {BuildingPage} from './page/BuildingPage.jsx';
import {CafeteriaPage} from './page/CafeteriaPage.jsx';
import {CampusMapPage} from './page/CampusMapPage.jsx';

createRoot(document.getElementById('root')).render(<StrictMode>
  <BrowserRouter>
    <AuthProvider>
      <Header/>
      <Routes>
        <Route path="/" element={<CampusMapPage/>}/>
        <Route path="/building/:buildingKey" element={<BuildingPage/>}/>
        <Route path="/cafeteria" element={<CafeteriaPage/>}/>
      </Routes>
    </AuthProvider>
  </BrowserRouter>
</StrictMode>);
