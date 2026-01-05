import { useEffect, useState } from 'react';
import { Route, Routes } from 'react-router';
import axios from 'axios';
import './App.css';

import TestComponent from './TestComponent';

interface City {
  id: number;
  cityName: string;
}

function App() {
  const [cities, setCities] = useState<City[]>([]);

  useEffect(() => {
    axios.get("http://localhost:8080/api/public/getcities")
      .then((response) => {
        setCities(response.data);
        console.log(response.data);
      });
  }, []);

  return (
    <>
      <h1>Hello world!</h1>

      {cities.map((city) => {
        return <p key={city.id}>{city.cityName}</p>
      })}

      <Routes>
        <Route path='/hi' element={<TestComponent />} />
      </Routes>
    </>
  );
}

export default App;
