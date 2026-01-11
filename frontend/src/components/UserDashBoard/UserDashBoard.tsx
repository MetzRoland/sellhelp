import type { User } from "../../contextProviders/AuthProvider/AuthProvider";
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
      <div className="container">
        <h1>Üdv, {user.email}</h1>
      </div>
      <Footer />
    </>
  );
}

export default UserDashboard;
