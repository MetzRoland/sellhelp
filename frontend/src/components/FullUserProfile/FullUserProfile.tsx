import type { User } from "../../contextProviders/AuthProvider/AuthProviderTypes";
import Header from "../Header/Header";
import Footer from "../Footer/Footer";
import { useLocation } from "react-router";
import { privateAxios } from "../../config/axiosConfig";
import { useEffect, useState } from "react";
import { useParams } from "react-router";
import axios from "axios";

interface FullUserProfileProps {
  user: User | null;
}

function FullUserProfile({ user }: FullUserProfileProps) {
  const { id } = useParams();
  const location = useLocation();
  if (!user &&  !location.pathname.startsWith("/user/")) {
    return <div>Nincs bejelentkezve</div>;
  }

  const [localUser, setLocalUser] = useState<User | null>(null);
  
  useEffect( () => {
    const fetchUser = async () => {
      const response = await privateAxios.get(`/users/${id}`);
      setLocalUser(response.data);
    }
    fetchUser();
  });

  if (!user)
  {
    user = {...localUser};
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

export default FullUserProfile;
