import { Route, Routes } from "react-router";
import Register from "./components/Register/Register";
import Login from "./components/Login/Login";
import FinishGoogleRegister from "./components/FinishGoogleRegister/FinishGoogleRegister";
import UserDashboard from "./components/UserDashBoard/UserDashBoard";
import { useAuth } from "./contextProviders/AuthProvider/AuthContext";
import PrivateRouterLayout from "./components/Routes/PrivateRouterLayout";
import PublicRouterLayout from "./components/Routes/PublicRouterLayout";
import { PageLoaderWrapper } from "./components/PageLoaderWrapper/PageLoaderWrapper";

import "./App.css";

function App() {
  const { user } = useAuth();

  return (
    <>
      <PageLoaderWrapper duration={1000} message="Az oldal betöltése...">
        <Routes>
          <Route element={<PublicRouterLayout />}>
            <Route index element={<div>Főoldal</div>} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route
              path="/finishGoogleRegistration"
              element={<FinishGoogleRegister />}
            />
          </Route>

          <Route element={<PrivateRouterLayout />}>
            <Route path="/home" element={<UserDashboard user={user} />} />
          </Route>
        </Routes>
      </PageLoaderWrapper>
    </>
  );
}

export default App;
