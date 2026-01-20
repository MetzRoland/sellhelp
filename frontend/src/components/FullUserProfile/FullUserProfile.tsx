import type { User } from "../../contextProviders/AuthProvider/AuthProviderTypes";
import { useAuth } from "../../contextProviders/AuthProvider/AuthContext";
import { useParams } from "react-router";
import { useEffect, useState } from "react";
import { useLoading } from "../../contextProviders/ProccessLoadProvider/ProccessLoadContext";
import Header from "../Header/Header";
import Footer from "../Footer/Footer";
import { privateAxios } from "../../config/axiosConfig";
import PageNotFound from "../PageNotFound/PageNotFound";

interface FullUserProfileProps{
  settings?: boolean;
}

function FullUserProfile({ settings }: FullUserProfileProps) {
  const { user: authUser } = useAuth();
  const { id } = useParams();

  const [user, setUser] = useState<User | null>(null);

  const { setIsLoading, setLoadingMessage, isLoading } = useLoading();

  useEffect(() => {
    if (!id) {
      if (!authUser) return;

      setIsLoading(true);
      setLoadingMessage("A fiók adatainak betöltése...");

      setUser(authUser);

      setIsLoading(false);
      return;
    }

    const fetchUserById = async () => {
      setIsLoading(true);
      try {
        setLoadingMessage("A fiók adatainak betöltése...");

        const response = await privateAxios.get(`/user/users/${id}`);
        setUser(response.data);
      } catch {
        setUser(null);
      } finally {
        setIsLoading(false);
      }
    };

    fetchUserById();
  }, [id, authUser, setIsLoading, setLoadingMessage]);

  if(isLoading && !user){
    return;
  }

  if(!user){
    return <PageNotFound message="A fiók nem található!"/>;
  }

  return (
    <>
      <Header />
      <div className="main-container">
        <h1>
          Üdv, {user.lastName} {user.firstName}
        </h1>

        <p>Email: {user.email}</p>
        {settings && (
          <button>Módosít</button>
        )}
        <p>Vezetéknév: {user.lastName}</p>
        <p>Keresztnév: {user.firstName}</p>
        <p>Születési dátum: {user.birthDate.toString().split("-").join(".")}</p>
        <p>Város: {user.cityName}</p>
        <p>
          {user.mfa
            ? "Kétfaktoros hitelesítés bekapcsolva"
            : "Kétfaktoros hitelesítés kikapcsolva"}
        </p>
        <p>Szerepkör: {user.role}</p>
        <p>{!user.banned ? "A fiók aktív" : "A fiók letiltva adminok által"}</p>
      </div>
      <Footer />
    </>
  );
}

export default FullUserProfile;
