import { Route, Routes } from 'react-router';
import Register from './components/Register/Register';
import './App.css';

function App() {
  return (
    <>
      <Routes>
        <Route path='/register' element={<Register />} />
      </Routes>
    </>
  );
}

export default App;
