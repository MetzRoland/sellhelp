import { Outlet, Navigate } from "react-router";
import { useAuth } from "../../contextProviders/AuthProvider/AuthContext";
import ProccessLoad from "../ProcessLoad/ProccessLoad";

const NonSuperUserRouterLayout = () => {
  const { authLoading, user } = useAuth();

  if (authLoading) {
    return <ProccessLoad />;
  }

  if (user?.role === "ROLE_ADMIN" || user?.role === "ROLE_MODERATOR") {
    return <Navigate to="/home" replace />;
  }

  return <Outlet />;
};

export default NonSuperUserRouterLayout;
