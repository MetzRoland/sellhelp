import type { User } from "../../contextProviders/AuthProvider/AuthProviderTypes";
import Header from "../Header/Header";
import Footer from "../Footer/Footer";

interface UserDashboardProps {
  user: User | null;
}

function UserDashboard({ user }: UserDashboardProps) {
  if (!user) {
    return <div>Nincs bejelentkezve</div>;
  }

  return (
    <>
      <Header />
      <div className="main-container">
        <h1>Üdv, {user.lastName} {user.firstName}</h1>

        <p>Email: {user.email}</p>
        <p>Vezekéknév: {user.lastName}</p>
        <p>Keresztnév: {user.firstName}</p>
        <p>Születési dátum: {user.birthDate.toString().split("-").join(".")}</p>
        <p>Város: {user.cityName}</p>
        <p>{user.mfa ? "Kétfaktoros hitelesítés bekapcsolva" : "Kétfaktoros hitelesítés kipapcsolva"}</p>
        <p>Szerepkör: {user.role}</p>
        <p>{!user.banned ? "A fiók aktív" : "A fiók letiltva adminok által"}</p>
      </div>
      <Footer />
    </>
  );
}

export default UserDashboard;
