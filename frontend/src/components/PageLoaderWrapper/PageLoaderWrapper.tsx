import { useEffect } from "react";
import { useLocation } from "react-router";
import { useLoading } from "../../contextProviders/ProccessLoadProvider/ProccessLoadContext";

interface PageLoaderWrapperProps {
  children: React.ReactNode;
  duration?: number;
  message?: string;
}

export function PageLoaderWrapper({ children, duration = 1000, message }: PageLoaderWrapperProps) {
  const location = useLocation();
  const { setIsLoading, setLoadingMessage } = useLoading();

  useEffect(() => {
    if (message) setLoadingMessage(message);

    setIsLoading(true);
    const timer = setTimeout(() => {
      setIsLoading(false);
      if (message) setLoadingMessage("");
    }, duration);

    return () => clearTimeout(timer);
  }, [location.pathname, setIsLoading, setLoadingMessage, duration, message]);

  return <>{children}</>;
}
