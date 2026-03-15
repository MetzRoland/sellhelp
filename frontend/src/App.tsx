import { Route, Routes, useLocation } from "react-router";
import Register from "./components/Register/Register";
import Login from "./components/Login/Login";
import FinishGoogleRegister from "./components/FinishGoogleRegister/FinishGoogleRegister";
import FullUserProfile from "./components/FullUserProfile/FullUserProfile";
import AuthenticatedRouterLayout from "./components/Routes/AuthenticatedRouterLayout";
import PublicRouterLayout from "./components/Routes/PublicRouterLayout";
import PageNotFound from "./components/PageNotFound/PageNotFound";
import ScrollToTop from "./components/ScrollToTop/ScrollToTop";
import SuperUserRouterLayout from "./components/Routes/SuperUserRouterLayout";
import UserOnlyRouterLayout from "./components/Routes/UserOnlyRouterLayout";
import UserList from "./components/UserList/UserList";
import ProfileBanned from "./components/ProfileBanned/ProfileBanned";
import DelayedLayout from "./components/DelayedLayout";
import SetupMfa from "./components/SetupMfa/SetupMfa";
import ResetPassword from "./components/ResetPassword/ResetPassword";
import PostsListComponent from "./components/PostsListComponent/PostsListComponent";
import NewPostComponent from "./components/NewPostComponent/NewPostComponent";
import FullPostView from "./components/FullPostView/FullPostView";
import NonSuperUserRouterLayout from "./components/Routes/NonSuperUserRouterLayout";
import ForgetPasswordEmail from "./components/ForgetPasswordEmail/ForgetPasswordEmail";

function App() {
  const location = useLocation();

  return (
    <>
      <ScrollToTop />
      <Routes location={location}>
        <Route element={<DelayedLayout />}>
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
            <Route path="/forgotPassword" element={<ResetPassword forgotPassword={true} />} />
            <Route path="/forgotPasswordEmail" element={<ForgetPasswordEmail />} />
          </Route>

          <Route element={<AuthenticatedRouterLayout />}>
            <Route path="/home" element={<FullUserProfile />} />
            <Route path="/home/settings" element={<FullUserProfile settings={true} />} />
            <Route path="/profile" element={<FullUserProfile />} />
            <Route path="/setupmfa" element={<SetupMfa />} />
          </Route>

          <Route element={<UserOnlyRouterLayout />}>
            <Route path="/myposts" element={<PostsListComponent postFetchingEndpoint="/post/myposts" title="Saját posztok" />} />
            <Route path="/posts/involved" element={<PostsListComponent postFetchingEndpoint="/post/posts/involved" title="Elvállalt posztok" />} />
            <Route path="/posts/new" element={<NewPostComponent />} />
            <Route path="/users" element={<UserList />} />
            <Route path="/resetPassword" element={<ResetPassword />} />
          </Route>

          <Route element={<SuperUserRouterLayout />}>
            <Route path="/userManagement" element={<UserList isAdmin={true} />} />
            <Route path="/postManagement" element={<PostsListComponent postFetchingEndpoint="/superuser/posts" navigateToPostEndpoint="/postManagement/posts/" title="Posztok kezelése" />} />
            <Route path="/postManagement/posts/:id" element={<FullPostView fetchEndpoint="/superuser/posts/"/>} />
          </Route>

          <Route element={<NonSuperUserRouterLayout />}>
            <Route path="/posts" element={<PostsListComponent postFetchingEndpoint="/post/posts" title="Új posztok" />} />
            <Route path="/posts/:id" element={<FullPostView />} />
          </Route>
          
          <Route path="/users/:id" element={<FullUserProfile />} />

          <Route path="*" element={<PageNotFound />} />
        </Route>
      </Routes>
    </>
  );
}

export default App;
