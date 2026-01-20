import { Route, Routes, useLocation } from "react-router";
import Register from "./components/Register/Register";
import Login from "./components/Login/Login";
import FinishGoogleRegister from "./components/FinishGoogleRegister/FinishGoogleRegister";
import FullUserProfile from "./components/FullUserProfile/FullUserProfile";
import { useAuth } from "./contextProviders/AuthProvider/AuthContext";
import AuthenticatedRouterLayout from "./components/Routes/AuthenticatedRouterLayout";
import PublicRouterLayout from "./components/Routes/PublicRouterLayout";
import { PageLoaderWrapper } from "./components/PageLoaderWrapper/PageLoaderWrapper";
import PageNotFound from "./components/PageNotFound/PageNotFound";
import ScrollToTop from "./components/ScrollToTop/ScrollToTop";
import SuperUserRouterLayout from "./components/Routes/SuperUserRouterLayout";
import UserBanning from "./components/UserBanning/UserBanning";
import ProfileBanned from "./components/ProfileBanned/ProfileBanned";

import "./App.css";

function App() {
  const { user, isAuthenticated } = useAuth();
  const location = useLocation();

  return (
    <>
      <PageLoaderWrapper duration={1000} message="Az oldal betöltése...">
        <ScrollToTop />
        <Routes location={location} key={location.pathname}>
          <Route element={<PublicRouterLayout />}>
            <Route index element={<div>Főoldal</div>} />
            <Route path="/login" element={<Login />} />
            <Route path="/adminLogin" element={<Login isAdminLogin={true} />} />
            <Route path="/register" element={<Register />} />
            <Route
              path="/finishGoogleRegistration"
              element={<FinishGoogleRegister />}
            />
            <Route path="/profileInactive" element={<ProfileBanned />} />
          </Route>

          <Route element={<AuthenticatedRouterLayout />}>
            <Route path="/home" element={<FullUserProfile user={user} />} />
            <Route path="/home/settings" element={<FullUserProfile user={user} />} />
          </Route>

          <Route element={<SuperUserRouterLayout />}>
            <Route path="/banningPage" element={<UserBanning />} />
          </Route>

          <Route path="/user/:id" element={<FullUserProfile user={null} />} />

          <Route path="*" element={<PageNotFound />} />
        </Routes>
      </PageLoaderWrapper>
    </>
  );
}

export default App;
