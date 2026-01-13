import { Outlet, Navigate } from "react-router";
import { useAuth } from "../../contextProviders/AuthProvider/AuthContext";
import ProccessLoad from "../ProcessLoad/ProccessLoad";

const PublicRouterLayout = () => {
  const { isAuthenticated, authLoading } = useAuth();

  if (authLoading) {
    return <ProccessLoad />;
  }

  if (isAuthenticated) {
    return <Navigate to="/home" replace />;
  }

  return <Outlet />;
};

export default PublicRouterLayout;
