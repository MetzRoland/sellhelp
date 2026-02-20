import { Outlet, Navigate } from "react-router";
import { useAuth } from "../../contextProviders/AuthProvider/AuthContext";
import ProccessLoad from "../ProcessLoad/ProccessLoad";

const UserOnlyRouterLayout = () => {
  const { isAuthenticated, authLoading, user } = useAuth();

  if (authLoading || !user) {
    return <ProccessLoad />;
  }

  if (!isAuthenticated || user.role !== "ROLE_USER") {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
};

export default UserOnlyRouterLayout;
