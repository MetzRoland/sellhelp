import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import './index.css';
import App from './App.tsx';
import { AuthProvider } from './contextProviders/AuthProvider/AuthProvider.tsx';
import { ProccessLoadProvider } from './contextProviders/ProccessLoadProvider/ProccessLoadProvider.tsx';

createRoot(document.getElementById('root')!).render(
  <BrowserRouter>
    <StrictMode>
      <ProccessLoadProvider>
        <AuthProvider>
          <App />
        </AuthProvider>
      </ProccessLoadProvider>
    </StrictMode>
  </BrowserRouter>
);
