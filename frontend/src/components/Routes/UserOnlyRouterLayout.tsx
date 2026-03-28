import { Outlet, Navigate } from "react-router";
import { useAuth } from "../../contextProviders/AuthProvider/AuthContext";
import ProccessLoad from "../ProcessLoad/ProccessLoad";

const UserOnlyRouterLayout = () => {
  const { isAuthenticated, authLoading, user } = useAuth();

  if (authLoading) {
    return <ProccessLoad />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (user?.role !== "ROLE_USER") {
    return <Navigate to="/home" replace />;
  }

  return <Outlet />;
};

export default UserOnlyRouterLayout;
