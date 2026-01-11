import { Route, Routes } from 'react-router';
import Register from './components/Register/Register';
import Login from './components/Login/Login';
import UserDashboard from './components/UserDashBoard/UserDashBoard';
import { useAuth } from './contextProviders/AuthProvider/AuthContext';
import PrivateRouterLayout from './components/Routes/PrivateRouterLayout';
import PublicRouterLayout from './components/Routes/PublicRouterLayout';

import './App.css';

function App() {
  const { user } = useAuth();
  
  return (
    <>
      <Routes>
        <Route element={<PublicRouterLayout />}>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
        </Route>

        <Route element={<PrivateRouterLayout />}>
          <Route path='/home' element={<UserDashboard user={user} />} />
        </Route>
      </Routes>
    </>
  );
}

export default App;
