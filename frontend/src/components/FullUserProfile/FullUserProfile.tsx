import type { User } from "../../contextProviders/AuthProvider/AuthProviderTypes";
import { useAuth } from "../../contextProviders/AuthProvider/AuthContext";
import { useParams } from "react-router";
import { useEffect, useState } from "react";
import { useLoading } from "../../contextProviders/ProccessLoadProvider/ProccessLoadContext";
import Header from "../Header/Header";
import Footer from "../Footer/Footer";
import { privateAxios } from "../../config/axiosConfig";
import PageNotFound from "../PageNotFound/PageNotFound";
import InputForm from "../Reusables/InputForm/InputForm";
import type { UserUpdateForm, UserUpdateValidationErrors } from "./UserUpdateTypes";
import { AxiosError } from "axios";

interface FullUserProfileProps{
  settings?: boolean;
}

function FullUserProfile({ settings }: FullUserProfileProps) {
  const { user: authUser } = useAuth();
  const { id } = useParams();

  const [user, setUser] = useState<User | null>(null);
  const { setIsLoading, setLoadingMessage, isLoading } = useLoading();
  const [userUpdateError, setUserUpdateError] = useState("");
  const [success, setSuccess] = useState(false);
  const [validationErrors, setValidationErrors] =
    useState<UserUpdateValidationErrors>({});

  const userUpdateInputs = [
    { name: "lastName", type: "text", placeholder: "Vezetéknév" },
    { name: "firstName", type: "text", placeholder: "Keresztnév" },
    { name: "birthDate", type: "date", placeholder: "Születési dátum" },
    { name: "cityName", type: "select", placeholder: "Település" },
    { name: "email", type: "text", placeholder: "Email" },
    { name: "mfa", type: "text", placeholder: "Kétfaktoros hitelesítés" },
    { name: "role", type: "text", placeholder: "Szerepkör" },
    { name: "isBanned", type: "text", placeholder: "Fiók hozzáférés" }
  ] as const;

  // <p>
  //   {user.mfa
  //     ? "Kétfaktoros hitelesítés bekapcsolva"
  //     : "Kétfaktoros hitelesítés kikapcsolva"}
  // </p>
  // <p>Szerepkör: {getUserRoleLabel(user.role)}</p>
  // <p>{!user.banned ? "A fiók aktív" : "A fiók letiltva adminok által"}</p>

  const [formData, setFormData] = useState<UserUpdateForm>({
    lastName: "",
    firstName: "",
    birthDate: "",
    cityName: "",
    email: "",
    mfa: "",
    role: "",
    isBanned: ""
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




  const handleUpdateSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    try {
      setIsLoading(true);
      setLoadingMessage("Adatok frissítése...");

      const response = await privateAxios.patch(
        "/user/update/details",
        formData
      );
      console.log("PATCH /user/update/details-ből:")
      console.log(response);

      if (response.status === 200)
      {
        setSuccess(true);
        setValidationErrors({});
        setUserUpdateError("");

        setFormData({
          lastName: "",
          firstName: "",
          birthDate: "",
          cityName: "",
          email: "",
        });
      }

    } catch (err) {
      const error = err as AxiosError<{ message?: string; errors?: UserUpdateForm }>;

      setSuccess(false);
      setValidationErrors(
          error.response?.data?.errors ?? {}
      );

      setUserUpdateError(
          error.response?.data?.message ??
              "Sikertelen frissítés!"
      );
    } finally {
      setIsLoading(false);
    }
  };

  const handleUpdateInput = (
      e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
      const { name, value } = e.target;

      setFormData((prev) => ({
          ...prev,
          [name]: value,
      }));

      setValidationErrors({});
      setUserUpdateError("");
  };

  function getUserRoleLabel(role: string): string
  {
    switch (role)
    {
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

  return (
    <>
      <Header />
      <div className="main-container">
        <h1>
          Üdv, {user.lastName} {user.firstName}
        </h1>


        {userUpdateError && (
            <p className="message error error-process-status">
                {userUpdateError}
            </p>
        )}

        {success && (
            <p className="message success-message">
                Sikeres regisztráció!
            </p>
        )}

        <form
          className="content-container login-form"
          onSubmit={handleUpdateSubmit}
        >

          {/* If settings, Add disable and button */}
          <InputForm<UserUpdateForm>
            inputs={userUpdateInputs}
            formData={formData}
            handleFunction={handleUpdateInput}
            errorMessage={validationErrors}
          />

        </form>
        {/* 
        <input
          type="text"
          value={user.email}
          disabled
        />
        {settings && (<button className="btn" type="button">Módosít</button>)}

        <input
          type="text"
          value={user.lastName}
          disabled
        />
        {settings && (<button className="btn" type="button">Módosít</button>)}

        <input
          type="text"
          value={user.firstName}
          disabled
        />
        {settings && (<button className="btn" type="button">Módosít</button>)}

        <input
          type="text"
          value={user.birthDate.toString().split("-").join(".")}
          disabled
        />
        {settings && (<button className="btn" type="button">Módosít</button>)}

        <input
          type="text"
          value={user.cityName}
          disabled
        />
        {settings && (<button className="btn" type="button">Módosít</button>)}
        */}

      </div>
      <Footer />
    </>
  );
}

export default FullUserProfile;