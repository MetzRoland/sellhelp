import { Outlet, Navigate } from "react-router";
import { useAuth } from "../../contextProviders/AuthProvider/AuthContext";
import ProccessLoad from "../ProcessLoad/ProccessLoad";

const SuperUserRouterLayout = () => {
  const { isAuthenticated, authLoading, user } = useAuth();

  if (authLoading) {
    return <ProccessLoad />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if(user?.role !== "ROLE_MODERATOR" && user?.role !== "ROLE_ADMIN"){
    return <Navigate to="/home" replace />;
  }

  return <Outlet />;
};

export default SuperUserRouterLayout;
