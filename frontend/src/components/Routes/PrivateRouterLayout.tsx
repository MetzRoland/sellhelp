import { Outlet, Navigate } from "react-router";
import { useAuth } from "../../contextProviders/AuthProvider/AuthContext";

const PrivateRouterLayout = () => {
  const { isAuthenticated, authLoading } = useAuth();

  if (authLoading) {
    return <div>Loading...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
};

export default PrivateRouterLayout;
