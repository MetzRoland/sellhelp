import { useEffect } from "react";
import { useLocation } from "react-router";
import { useLoading } from "../../contextProviders/ProccessLoadProvider/ProccessLoadContext";

interface FetchLoaderWrapperProps {
  children: React.ReactNode;
  message?: string;
}

export function FetchLoaderWrapper({ children, message }: FetchLoaderWrapperProps) {
  const location = useLocation();
  const { setLoadingMessage } = useLoading();

  useEffect(() => {
    if (message) setLoadingMessage(message);
  }, [location.pathname, setLoadingMessage, message]);

  return <>{children}</>;
}
