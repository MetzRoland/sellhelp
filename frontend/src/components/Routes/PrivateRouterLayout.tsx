import { Outlet, Navigate } from "react-router";
import { useAuth } from "../../contextProviders/AuthProvider/AuthContext";
import ProccessLoad from "../ProcessLoad/ProccessLoad";

const PrivateRouterLayout = () => {
  const { isAuthenticated, authLoading } = useAuth();

  if (authLoading) {
    return <ProccessLoad />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
};

export default PrivateRouterLayout;
