import { Route, Routes } from 'react-router';
import Register from './components/Register/Register';
import Login from './components/Login/Login';
import './App.css';

function App() {
  return (
    <>
      <Routes>
        <Route path='/register' element={<Register />} />
        <Route path='/login' element={<Login />} />
      </Routes>
    </>
  );
}

export default App;
