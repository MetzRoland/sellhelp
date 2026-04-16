import type { User } from "../../contextProviders/AuthProvider/AuthProviderTypes";
import { useAuth } from "../../contextProviders/AuthProvider/AuthContext";
import { useParams, useNavigate } from "react-router";
import { useEffect, useState, useRef } from "react";
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
import type { UserUpdateFormFields } from "./FullUserProfileTypes";
import FileDisplay from "../Reusables/FileDisplay/FileDisplay";
import ProfilePictureComponent from "../ProfilePictureComponent/ProfilePictureComponent";
import type { Post } from "../PostsListComponent/PostsListComponentTypes";
import PostView from "../PostView/PostView";

import "./FullUserProfile.css";

interface FullUserProfileProps {
  settings?: boolean;
}

function FullUserProfile({ settings }: FullUserProfileProps) {
  const { user: authUser, isAuthenticated } = useAuth();
  const { id } = useParams();

  const [user, setUser] = useState<User | null>(null);
  const { setIsLoading, setLoadingMessage, isLoading } = useLoading();
  const [cities, setCities] = useState<City[]>([]);
  const [posts, setPosts] = useState<Post[]>([]);

  const navigator = useNavigate();

  const handleProfilePictureUpdate = async (
    e: React.ChangeEvent<HTMLInputElement>,
  ) => {
    if (!e.target.files?.[0]) return;

    const file: File = e.target.files?.[0];

    const formData = new FormData();
    formData.append("file", file);

    try {
      setIsLoading(true);
      setLoadingMessage("Profilkép frissítése...");

      await privateAxios.post("/user/files/pp", formData, {
        transformRequest: (data) => data,
      });

      setSuccess(true);
      setUserUpdateError("");

      location.reload();
    } catch {
      setUserUpdateError("Profilkép frissítése sikertelen!");
      setSuccess(false);
    } finally {
      setIsLoading(false);
      e.target.value = "";
    }
  };

  const userUpdateInputs = [
    { name: "lastName", type: "text", placeholder: "Vezetéknév" },
    { name: "firstName", type: "text", placeholder: "Keresztnév" },
    { name: "birthDate", type: "date", placeholder: "Születési dátum" },
    { name: "cityName", type: "select", placeholder: "Település" },
    { name: "email", type: "text", placeholder: "Email" },
    { name: "role", type: "text", placeholder: "Szerepkör" },
  ] as const;

  const [disabledInputsMap, setDisabledInputsMap] = useState<
    Record<string, boolean>
  >(
    userUpdateInputs.reduce(
      (acc, input) => {
        acc[input.name] = true;
        return acc;
      },
      {} as Record<string, boolean>,
    ),
  );

  const settingInputsMap: Record<string, boolean> = userUpdateInputs.reduce(
    (acc, input) => {
      if (input.name == "role") {
        acc[input.name] = false;
      } else if (!settings) {
        acc[input.name] = false;
      } else {
        acc[input.name] = true;
      }
      return acc;
    },
    {} as Record<string, boolean>,
  );

  const [userUpdateError, setUserUpdateError] = useState("");
  const [success, setSuccess] = useState(false);
  const [validationErrors, setValidationErrors] =
    useState<UserUpdateValidationErrors>({});

  const fileInputRef = useRef<HTMLInputElement | null>(null);

  const [formData, setFormData] = useState<UserUpdateFormFields>({
    lastName: "",
    firstName: "",
    birthDate: "",
    cityName: "",
    email: "",
    role: "",
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
  }, [setIsLoading, settings]);

  useEffect(() => {
    const fetchPostsForUser = async () => {
      try {
        setIsLoading(true);
        const response = await publicAxios.get<Post[]>("/post/posts");
        setPosts(
          response.data.filter((post) => post.publisher.id === user?.id),
        );
      } finally {
        setIsLoading(false);
      }
    };

    fetchPostsForUser();
  }, [setIsLoading, user?.id]);

  useEffect(() => {
    setFormData({
      lastName: user?.lastName,
      firstName: user?.firstName,
      birthDate: user?.birthDate?.toString(),
      cityName: user?.cityName,
      email: user?.email,
      role: getUserRoleLabel(user?.role),
    });
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

  const getUpdatedField = (
    original: User,
    updated: Partial<UserUpdateFormFields>,
    fieldName: keyof UserUpdateFormFields,
  ): Partial<UserUpdateFormFields> => {
    if (fieldName === "role") return {};

    const newValue = updated[fieldName];

    if (typeof newValue === "string" && newValue.trim() === "") return {};

    if (newValue === original[fieldName]) {
      return {};
    }

    return { [fieldName]: newValue };
  };

  const handleUpdateInput = (
    e: React.ChangeEvent<
      HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement
    >,
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

      case "ROLE_MODERATOR":
        return "Moderátor";

      case "ROLE_ADMIN":
        return "Adminisztrátor";

      default:
        return "Hiba";
    }
  }

  const toggleDisabled = async (inputName: keyof UserUpdateFormFields) => {
    const isCurrentlyDisabled = disabledInputsMap[inputName];

    setDisabledInputsMap((prev) => ({
      ...prev,
      [inputName]: !isCurrentlyDisabled,
    }));

    if (!isCurrentlyDisabled && user) {
      const payload = getUpdatedField(user, formData, inputName);

      if (Object.keys(payload).length === 0) {
        setFormData((prev) => ({ ...prev, [inputName]: user[inputName] }));
        return;
      }

      const endpoint =
        inputName === "email" ? "/user/update/email" : "/user/update/details";

      try {
        setIsLoading(true);
        setLoadingMessage("Adatok frissítése...");

        const response = await privateAxios.patch(endpoint, payload);

        if (response.status === 200) {
          setSuccess(true);
          setUser((prev) => (prev ? ({ ...prev, ...payload } as User) : prev));
          setUserUpdateError("");
          setValidationErrors({});
        }
      } catch (err) {
        const error = err as AxiosError<{
          message?: string;
          errors?: UserUpdateFormFields;
        }>;

        setSuccess(false);
        setValidationErrors(error.response?.data?.errors ?? {});
        setUserUpdateError(
          error.response?.data?.message ?? "Sikertelen frissítés!",
        );
      } finally {
        setIsLoading(false);
      }
    }
  };

  const sendPassUpdate = async () => {
    try {
      setIsLoading(true);
      setLoadingMessage("Email küldése...");

      const response = await privateAxios.get("/user/update/password/send");

      if (response.status === 200) {
        setSuccess(true);
        setValidationErrors({});
        setUserUpdateError("");
      }
    } catch {
      setUserUpdateError("Jelszó modósító email elküldése sikertelen!");
    } finally {
      setIsLoading(false);
    }
  };

  const disableMfa = async () => {
    try {
      setIsLoading(true);
      setLoadingMessage("Kétfaktoros hitelesítés kikapcsolása...");

      await privateAxios.get("/auth/disable2fa");
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

  const deleteProfilePicture = async () => {
    try {
      setIsLoading(true);
      setLoadingMessage("Profilkép törlése...");

      const response = await privateAxios.delete("/user/files/pp");

      if (response.status === 200) {
        setSuccess(true);
        setUserUpdateError("");

        setUser((prev) => (prev ? { ...prev } : prev));

        location.reload();
      }
    } catch {
      setUserUpdateError("Profilkép törlése sikertelen!");
      setSuccess(false);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <>
      <Header />
      <div className="main-container">
        <h1 className="content-title">{title}</h1>

        <form className="content-container content-container-has-pfp">
          <div className="profile-picture-container">
            <ProfilePictureComponent userId={user.id} />

            {settings && user.id === authUser?.id && (
              <>
                <input
                  type="file"
                  accept="image/*"
                  ref={fileInputRef}
                  onChange={handleProfilePictureUpdate}
                  style={{ display: "none" }}
                />

                <button
                  type="button"
                  className="setting-btn"
                  onClick={() => fileInputRef.current?.click()}
                >
                  Módosítás
                </button>

                <button
                  type="button"
                  className="setting-btn"
                  onClick={deleteProfilePicture}
                >
                  Törlés
                </button>
              </>
            )}
          </div>
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

          {user.banned && isAuthenticated && (
            <p className="message error">A felhasználót bannolták!</p>
          )}

          {settings && user.authProvider === "LOCAL" && (
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

          {user.role === "ROLE_USER" && (
            <FileDisplay
              type="user"
              id={user.id}
              canEdit={user.id === authUser?.id && settings}
            />
          )}
        </form>

        {user.id !== authUser?.id && (
          <>
            <h2 className="profile-posts-content-title">{user.lastName + " " + user.firstName + " posztjai:"}</h2>

            {posts.length === 0 && !isLoading && <p>Nincsenek posztok</p>}

            <div className="posts-list-container">
              {posts.map((post) => {
                return (
                  <PostView
                    key={post.id}
                    post={post}
                    handleOnClick={() => {
                      navigator("/posts/" + `${post.id}`);
                    }}
                  />
                );
              })}
            </div>
          </>
        )}
      </div>
      <Footer />
    </>
  );
}

export default FullUserProfile;
