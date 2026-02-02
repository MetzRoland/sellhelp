// DelayedLayout.js
import { useState, useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import ProccessLoad from './ProcessLoad/ProccessLoad';

const DelayedLayout = ({ delay = 1000 }) => {
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const timer = setTimeout(() => setIsLoading(false), delay);
    return () => clearTimeout(timer);
  }, [delay]);

  return isLoading ? <ProccessLoad /> : <Outlet />;
};

export default DelayedLayout;
