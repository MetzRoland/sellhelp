import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import { privateAxios } from "../../config/axiosConfig";
import type { User } from "../../contextProviders/AuthProvider/AuthProviderTypes";
import { useLoading } from "../../contextProviders/ProccessLoadProvider/ProccessLoadContext";
import Header from "../Header/Header";
import Footer from "../Footer/Footer";
import UserProfileView from "../UserProfileView/UserProfileView";

function UserBanning() {
  const registerInputs = [
    { name: "userName", type: "text", placeholder: "Felhasználónév" },
    { name: "email", type: "text", placeholder: "Email" },
    { name: "banned", type: "select", placeholder: "Fiók állapota" },
    { name: "role", type: "select", placeholder: "Szerepkör" },
  ] as const;

  const [formData, setFormData] = useState<RegisterForm>({
    userName: "",
    email: "",
    banned: "",
    role: "",
  });

  const { setIsLoading, setLoadingMessage } = useLoading();
  const [userAccounts, setUserAccounts] = useState<User[]>([]);

  const navigate = useNavigate();

  useEffect(() => {
    const fetchUserAccounts = async () => {
      setIsLoading(true);
      setLoadingMessage("Felhasználói fiókok betöltése...");

      try {
        const response = await privateAxios.get("/user/users");
        const users: User[] = response.data;

        await Promise.all(
          users.map(async (user) => {
            user.profilePicture = await fetchProfilePicture(user.id);
          }),
        );

        setUserAccounts(users);
      } catch (error) {
        console.error(error);
        setUserAccounts([]);
      } finally {
        setIsLoading(false);
        setLoadingMessage("");
      }
    };

    fetchUserAccounts();
  }, [setIsLoading, setLoadingMessage]);

  const fetchProfilePicture = async (userId: number) => {
    const response = await privateAxios.get(`/user/files/${userId}/pp`);

    return response.data.profilePictureUrl;
  };

  const handleUserBanning = async (userId: number, isBanned: boolean) => {
    setIsLoading(true);
    setLoadingMessage(!isBanned ? "Fiók tiltása..." : "Fiók engedélyezése...");

    setUserAccounts((prev) =>
      prev.map((user) =>
        user.id === userId ? { ...user, banned: !isBanned } : user,
      ),
    );

    try {
      const endpoint = !isBanned
        ? `/superuser/users/ban/${userId}`
        : `/superuser/users/unban/${userId}`;

      const response = await privateAxios.put(endpoint);

      if (response.data) {
        setUserAccounts((prev) =>
          prev.map((user) =>
            user.id === userId ? { ...user, ...response.data } : user,
          ),
        );
      }
    } catch (error) {
      console.error(error);

      setUserAccounts((prev) =>
        prev.map((user) =>
          user.id === userId ? { ...user, banned: isBanned } : user,
        ),
      );
    } finally {
      setIsLoading(false);
      setLoadingMessage("");
    }
  };

  return (
    <>
      <Header />

      <div className="main-container">
        {userAccounts.map((userAccount) => (
          <UserProfileView
            key={userAccount.id}
            adminMode={true}
            userAccount={userAccount}
            handleUserBanning={handleUserBanning}
            handleRedirectToProfile={() => navigate(`/users/${userAccount.id}`)}
          />
        ))}
      </div>

      <Footer />
    </>
  );
}

export default UserBanning;
