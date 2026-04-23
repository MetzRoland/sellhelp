import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import { privateAxios, publicAxios } from "../../config/axiosConfig";
import type { User } from "../../contextProviders/AuthProvider/AuthProviderTypes";
import { useLoading } from "../../contextProviders/ProccessLoadProvider/ProccessLoadContext";
import Header from "../Header/Header";
import Footer from "../Footer/Footer";
import UserProfileView from "../UserProfileView/UserProfileView";
import InputForm from "../Reusables/InputForm/InputForm";
import type { UserAccountFilter } from "./UserListTypes";
import type { UserInputField } from "./UserListTypes";
import type { UserRole } from "./UserListTypes";
import type { UserListProps } from "./UserListTypes";
import type { City } from "../Register/RegisterTypes";

import "./UserList.css";

function UserList({ isAdmin = false }: UserListProps) {
  const userUpdateInputs: UserInputField[] = [
    { name: "userName", type: "text", placeholder: "Felhasználónév" },
    { name: "email", type: "text", placeholder: "Email" },
    { name: "city", type: "select", placeholder: "Válasszon települést..." , userTitle: "Település"},
  ];

  if (isAdmin) {
    userUpdateInputs.push(
      { name: "role", type: "select", placeholder: "Válasszon szerepkört", userTitle: "Szerepkör" },
      {
        name: "banned",
        type: "select",
        placeholder: "Válasszon fiók állapotot",
        userTitle: "Állapot"
      },
    );
  }

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
  const [cities, setCities] = useState<City[]>([]);

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

  const cityOptions = cities.map((city) => ({
    id: city.id,
    value: city.cityName,
    label: city.cityName,
  }));

  useEffect(() => {
    const fetchUserAccounts = async () => {
      setIsLoading(true);
      setLoadingMessage("Felhasználói fiókok betöltése...");

      try {
        if (isAdmin) {
          const roleResponse =
            await privateAxios.get<UserRole[]>("/api/public/roles");
          setRoles(roleResponse.data);
        }

        const response = await privateAxios.get(
          isAdmin ? "/superuser/users" : "/user/users",
        );
        const users: User[] = response.data;

        const citiesResponse = await publicAxios.get("/api/public/cities");
        setCities(citiesResponse.data);

        setAllUserAccounts(users);
        setUserAccounts(users);
      } catch {
        setAllUserAccounts([]);
        setUserAccounts([]);
        setCities([]);
      } finally {
        setIsLoading(false);
        setLoadingMessage("");
      }
    };

    fetchUserAccounts();
  }, [setIsLoading, setLoadingMessage, isAdmin]);

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

      const matchesRole =
        !isAdmin || !formData.role || user.role === formData.role;

      const matchesCity = !formData.city || user.cityName === formData.city;

      return (
        matchesUserName &&
        matchesEmail &&
        matchesCity &&
        matchesBanned &&
        matchesRole
      );
    });

    setUserAccounts(filtered);
  }, [formData, allUserAccounts, isAdmin]);

  const handleInputUpdate = (
    e: React.ChangeEvent<
      HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement
    >,
  ) => {
    const { name, value } = e.target;

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
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
    } catch {
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
        <h1 className="container-title">Felhasználói fiókok</h1>

        <div className="content-container filter-container">
          <p className="message">Szűrési feltételek</p>

          <InputForm<UserAccountFilter>
            inputs={userUpdateInputs}
            formData={formData}
            handleFunction={handleInputUpdate}
            options={{
              role: roleOptions,
              banned: bannedOptions,
              city: cityOptions,
            }}
          />
        </div>

        {userAccounts.length === 0 && <h2>Nem találhatók fiókok!</h2>}

        {userAccounts.map((userAccount) => (
          <UserProfileView
            key={userAccount.id}
            adminMode={isAdmin}
            userAccount={userAccount}
            handleUserBanning={isAdmin ? handleUserBanning : undefined}
            handleRedirectToProfile={() => navigate(`/users/${userAccount.id}`)}
          />
        ))}
      </div>

      <Footer />
    </>
  );
}

export default UserList;
