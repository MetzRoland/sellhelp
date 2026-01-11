import { Outlet, Navigate } from "react-router";
import { useAuth } from "../../contextProviders/AuthProvider/AuthContext";

const PublicRouterLayout = () => {
  const { isAuthenticated, authLoading } = useAuth();

  if (authLoading) {
    return <div>Loading...</div>;
  }

  if (isAuthenticated) {
    return <Navigate to="/home" replace />;
  }

  return <Outlet />;
};

export default PublicRouterLayout;
