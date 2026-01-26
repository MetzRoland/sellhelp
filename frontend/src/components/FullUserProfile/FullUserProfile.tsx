import type { User } from "../../contextProviders/AuthProvider/AuthProviderTypes";
import { useAuth } from "../../contextProviders/AuthProvider/AuthContext";
import { useParams, useNavigate } from "react-router";
import { useEffect, useState } from "react";
import { useLoading } from "../../contextProviders/ProccessLoadProvider/ProccessLoadContext";
import Header from "../Header/Header";
import Footer from "../Footer/Footer";
import { privateAxios } from "../../config/axiosConfig";
import { publicAxios } from "../../config/axiosConfig";
import PageNotFound from "../PageNotFound/PageNotFound";
import InputForm from "../Reusables/InputForm/InputForm";
import type {
  UserUpdateForm,
  UserUpdateValidationErrors,
} from "./FullUserProfileTypes";
import { AxiosError } from "axios";
import { type City } from "../Register/RegisterTypes";

interface FullUserProfileProps {
  settings?: boolean;
}

function FullUserProfile({ settings }: FullUserProfileProps) {
  const { user: authUser } = useAuth();
  const { id } = useParams();

  const [user, setUser] = useState<User | null>(null);
  const { setIsLoading, setLoadingMessage, isLoading } = useLoading();
  const [cities, setCities] = useState<City[]>([]);

  const navigator = useNavigate();

  const userUpdateInputs = [
    { name: "lastName", type: "text", placeholder: "Vezetéknév" },
    { name: "firstName", type: "text", placeholder: "Keresztnév" },
    { name: "birthDate", type: "date", placeholder: "Születési dátum" },
    { name: "cityName", type: "select", placeholder: "Település" },
    { name: "email", type: "text", placeholder: "Email" },
    { name: "role", type: "text", placeholder: "Szerepkör" },
  ] as const;

  const [disabledInputsMap, setDisabledInputsMap] = useState(
    userUpdateInputs.reduce((acc, input) => {
      acc[input.name] = true; // or false if you want them enabled initially
      return acc;
    }, {}),
  );

  const settingInputsMap = userUpdateInputs.reduce((acc, input) => {
    if (input.name == "role") {
      acc[input.name] = false;
    } else if (input.name == "isBanned") {
      acc[input.name] = false;
    } else if (!settings) {
      acc[input.name] = false;
    } else {
      acc[input.name] = true;
    }
    return acc;
  }, {});

  const [userUpdateError, setUserUpdateError] = useState("");
  const [success, setSuccess] = useState(false);
  const [validationErrors, setValidationErrors] =
    useState<UserUpdateValidationErrors>({});

  const [formData, setFormData] = useState<UserUpdateForm>({
    lastName: "",
    firstName: "",
    birthDate: "",
    cityName: "",
    email: "",
    role: "",
    isBanned: "",
  });

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

        const response = await privateAxios.get<User>(`/user/users/${id}`);
        setUser(response.data);
      } catch {
        setUser(null);
        // more error handling
      } finally {
        setIsLoading(false);
      }
    };

    fetchUserById();
  }, [id, authUser, setIsLoading, setLoadingMessage]);

  useEffect(() => {
    if (!settings) return;

    const fetchCities = async () => {
      try {
        setIsLoading(true);
        const response = await publicAxios.get("/api/public/cities");
        setCities(response.data);
      } finally {
        setIsLoading(false);
      }
    };

    fetchCities();
  }, [setIsLoading]);

  const setUserData = () => {
    setFormData({
      lastName: user?.lastName,
      firstName: user?.firstName,
      birthDate: user?.birthDate?.toString(),
      cityName: user?.cityName,
      email: user?.email,
      role: getUserRoleLabel(user?.role),
    });
  };

  useEffect(() => {
    setUserData();
  }, [user]);

  if (isLoading && !user) {
    return;
  }

  if (!user) {
    return <PageNotFound message="A fiók nem található!" />;
  }

  const cityOptions = cities.map((city) => ({
    id: city.id,
    value: city.cityName,
    label: city.cityName,
  }));

  const handleUpdateSubmit = async () => {
    const payload = getUpdatedFields(user, formData);
    let endpoint = "/user/update/details";

    if (Object.keys(payload).length === 0) {
      console.log("No changes to update");
      return;
    }

    if (Object.keys(payload).length === 1 && payload.email) {
      console.log("No changes to update");
      endpoint = "/user/update/email";
    }

    try {
      setIsLoading(true);
      setLoadingMessage("Adatok frissítése...");

      const response = await privateAxios.patch(endpoint, payload);
      console.log("PATCH /user/update/details-ből:");
      console.log(response);

      if (response.status === 200) {
        setSuccess(true);
        setValidationErrors({});
        setUserUpdateError("");

        // setuser payload alapján
        setUser(prev => prev ? { ...prev, ...payload } : prev);
      }
    } catch (err) {
      const error = err as AxiosError<{
        message?: string;
        errors?: UserUpdateForm;
      }>;

      setSuccess(false);
      setValidationErrors(error.response?.data?.errors ?? {});

      setUserUpdateError(
        error.response?.data?.message ?? "Sikertelen frissítés!",
      );
    } finally {
      setIsLoading(false);

      //setUserData();
    }
  };

  const getUpdatedFields = (
    original: User,
    updated: Record<string, any>,
  ): Partial<User> => {
    return Object.keys(updated).reduce((acc, key) => {
      const typedKey = key as keyof User;

      if (
        typedKey !== "role" &&
        updated[typedKey] !== "" &&
        updated[typedKey] !== original[typedKey]
      ) {
        acc[typedKey] = updated[typedKey];
      }

      return acc;
    }, {} as Partial<User>);
  };

  const handleUpdateInput = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>,
  ) => {
    const { name, value } = e.target;

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    setValidationErrors({});
    setUserUpdateError("");
  };

  function getUserRoleLabel(role: string | undefined): string {
    switch (role) {
      case "ROLE_USER":
        return "Felhasználó";

      case "ROLE_MANAGER":
        return "Moderátor";

      case "ROLE_ADMIN":
        return "Adminisztrátor";

      default:
        return "Hiba";
    }
  }

  const toggleDisabled = (inputName: string) => {
    if (!disabledInputsMap[inputName]) handleUpdateSubmit();

    setDisabledInputsMap((prev) => ({
      ...prev,
      [inputName]: !prev[inputName], // flip the boolean
    }));
  };

  const sendPassUpdate = async () => {
    try {
      setIsLoading(true);
      setLoadingMessage("Email küldése...");

      const response = await privateAxios.get("/user/update/password/send");
      console.log("From the endpoint: /user/update/password/send");
      console.log(response);

      if (response.status === 200) {
        setSuccess(true);
        // setValidationErrors({});
        setUserUpdateError("");
      }
    } catch (err) {
      // error handling
    } finally {
      setIsLoading(false);
    }
  };

  const disableMfa = async () => {
    try {
      setIsLoading(true);
      setLoadingMessage("Kétfaktoros hitelesítés kikapcsolása...");

      const response = await privateAxios.get("/auth/disable2fa");
      console.log(response.data);
    } catch (err) {
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  const mfaToggle = async () => {
    if (!user.mfa) {
      navigator("/setupmfa");
      return;
    }

    await disableMfa();

    const userInfo = await privateAxios.get<User>("/user/info");
    setUser(userInfo.data);
  };

  const title = settings
    ? "Adatok módosítása"
    : `${user.lastName} ${user.firstName}`;

  return (
    <>
      <Header />
      <div className="main-container">
        <h1 className="content-title">{title}</h1>

        <form className="content-container login-form">
          {userUpdateError && (
            <p className="message error error-process-status">
              {userUpdateError}
            </p>
          )}

          {success && (
            <p className="message success-message">Frissítés sikeres</p>
          )}

          {/* If settings, Add buttons */}
          <InputForm<UserUpdateForm>
            inputs={userUpdateInputs}
            formData={formData}
            handleFunction={handleUpdateInput}
            errorMessage={validationErrors}
            disabledInputsMap={disabledInputsMap}
            settingInputsMap={settingInputsMap}
            disabledToggle={toggleDisabled}
            options={{ cityName: cityOptions }}
          />

          {(settings && user.authProvider === "LOCAL") && (
            <>
              <button className="btn" type="button" onClick={sendPassUpdate}>
                Jelszó módosítása
              </button>
              <button className="btn" type="button" onClick={mfaToggle}>
                {user.mfa
                  ? "Két faktoros hitelesítés kikapcsolása"
                  : "Két faktoros hitelesítés bekapcsolása"}
              </button>
            </>
          )}
        </form>
      </div>
      <Footer />
    </>
  );
}

export default FullUserProfile;
