import { Route, Routes } from 'react-router';
import Bejelentkezettfejlc from './components/bejelentkezettfejlc';
import './App.css';

function App() {

  return (
    <>
      <Routes>
        <Route path='/register' element={<Bejelentkezettfejlc />} />
      </Routes>
    </>
  );
}

export default App;
