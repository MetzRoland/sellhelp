import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import { privateAxios } from "../../config/axiosConfig";
import type { User } from "../../contextProviders/AuthProvider/AuthProviderTypes";
import { useLoading } from "../../contextProviders/ProccessLoadProvider/ProccessLoadContext";
import Header from "../Header/Header";
import Footer from "../Footer/Footer";
import UserProfileView from "../UserProfileView/UserProfileView";
import InputForm from "../Reusables/InputForm/InputForm";

import "./UserBanning.css";

interface UserAccountFilter {
  userName?: string;
  email?: string;
  banned?: string;
  role?: string;
}

interface UserRole {
  id: number;
  roleName: string;
}

function UserBanning() {
  const userUpdateInputs = [
    { name: "userName", type: "text", placeholder: "Felhasználónév" },
    { name: "email", type: "text", placeholder: "Email" },
    { name: "banned", type: "select", placeholder: "Válasszon fiók állapotot" },
    { name: "role", type: "select", placeholder: "Válasszon szerepkört" },
  ] as const;

  const [formData, setFormData] = useState<UserAccountFilter>({
    userName: "",
    email: "",
    banned: "",
    role: "",
  });

  const { setIsLoading, setLoadingMessage } = useLoading();

  const [allUserAccounts, setAllUserAccounts] = useState<User[]>([]);
  const [userAccounts, setUserAccounts] = useState<User[]>([]);

  const [roles, setRoles] = useState<UserRole[]>([]);
  const roleOptions = roles.map((role) => ({
    id: role.id,
    value: role.roleName,
    label:
      role.roleName === "ROLE_USER"
        ? "Felhasználó"
        : role.roleName === "ROLE_MODERATOR"
          ? "Moderátor"
          : "Admin",
  }));

  const bannedOptions = [
    { id: 1, value: "true", label: "Tiltott" },
    { id: 2, value: "false", label: "Engedélyezett" },
  ];

  const navigate = useNavigate();

  useEffect(() => {
    const fetchUserAccounts = async () => {
      setIsLoading(true);
      setLoadingMessage("Felhasználói fiókok betöltése...");

      try {
        await fetchUserRoles();
        const response = await privateAxios.get("/superuser/users");
        const users: User[] = response.data;

        await Promise.all(
          users.map(async (user) => {
            user.profilePicture = await fetchProfilePicture(user.id);
          }),
        );

        setAllUserAccounts(users);
        setUserAccounts(users);
      } catch (error) {
        console.error(error);
        setAllUserAccounts([]);
        setUserAccounts([]);
      } finally {
        setIsLoading(false);
        setLoadingMessage("");
      }
    };

    fetchUserAccounts();
  }, [setIsLoading, setLoadingMessage]);

  useEffect(() => {
    const filtered = allUserAccounts.filter((user) => {
      const userName = user.lastName + " " + user.firstName;

      const matchesUserName =
        !formData.userName ||
        userName?.toLowerCase().includes(formData.userName.toLowerCase());

      const matchesEmail =
        !formData.email ||
        user.email.toLowerCase().includes(formData.email.toLowerCase());

      const matchesBanned =
        !formData.banned || String(user.banned) === formData.banned;

      const matchesRole = !formData.role || user.role === formData.role;

      return matchesUserName && matchesEmail && matchesBanned && matchesRole;
    });

    setUserAccounts(filtered);
  }, [formData, allUserAccounts]);

  const handleInputUpdate = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>,
  ) => {
    const { name, value } = e.target;

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const fetchProfilePicture = async (userId: number) => {
    const response = await privateAxios.get(`/user/files/${userId}/pp`);
    return response.data.profilePictureUrl;
  };

  const fetchUserRoles = async () => {
    const response = await privateAxios.get<UserRole[]>("/api/public/roles");
    setRoles(response.data);
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
        setAllUserAccounts((prev) =>
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
        <h1 className="container-title">Felhasználói fiókok kezelése</h1>

        <div
          className="content-container user-account-filter-container"
        >
          <p className="message">
            Szűrési feltételek
          </p>

          <InputForm<UserAccountFilter>
            inputs={userUpdateInputs}
            formData={formData}
            handleFunction={handleInputUpdate}
            options={{ role: roleOptions, banned: bannedOptions }}
          />
        </div>

        {userAccounts.length === 0 && (
          <h2>Nem találhatók fiókok!</h2>
        )}

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
