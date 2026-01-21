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

  const [disabledInputs, setDisabledInputs] = useState(
    userUpdateInputs.reduce((acc, input) => {
      acc[input.name] = true; // or false if you want them enabled initially
      return acc;
    }, {})
  );

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

        const response = await privateAxios.get<User>(`/user/users/${id}`);
        setUser(response.data);

          setFormData({
            lastName: user?.lastName,
            firstName: user?.firstName,
            birthDate: user?.birthDate.toString(),
            cityName: user?.cityName,
            email: user?.email,
          });

      } catch {
        setUser(null);
        // more error handling
      } finally {
        setIsLoading(false);
      }
    };

    fetchUserById();
  }, [id, authUser, setIsLoading, setLoadingMessage]);

useEffect(()=> {
  setFormData({
    lastName: user?.lastName,
    firstName: user?.firstName,
    birthDate: user?.birthDate.toString(),
    cityName: user?.cityName,
    email: user?.email,
  });
}, [user]);

  if(isLoading && !user){
    return;
  }

  if(!user){
    return <PageNotFound message="A fiók nem található!"/>;
  }


  const toggleDisabled = (inputName: string) => {
    setDisabledInputs(prev => ({
      ...prev,
      [inputName]: !prev[inputName]  // flip the boolean
    }));
};



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

  const sendPassUpdate = async () =>
  {
    try {
      setIsLoading(true);
      setLoadingMessage("Email küldése...");

      const response = await privateAxios.get("/user/update/password/send");
      console.log("From the endpoint: /user/update/password/send")
      console.log(response);

      if (response.status === 200)
      {
        setSuccess(true);
        setValidationErrors({});
        setUserUpdateError("");
      }

    } catch (err) {
      // error handling
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <>
      <Header />
      <div className="main-container">
        <h1>
          Üdv, {user.lastName} {user.firstName}
        </h1>

        <form
          className="content-container login-form"
          onSubmit={handleUpdateSubmit}
        >
        {userUpdateError && (
            <p className="message error error-process-status">
                {userUpdateError}
            </p>
        )}

        {success && (
            <p className="message success-message">
                Frissítés sikeres
            </p>
        )}
        
          {/* If settings, Add disable and button */}
          <InputForm<UserUpdateForm>
            inputs={userUpdateInputs}
            formData={formData}
            handleFunction={handleUpdateInput}
            errorMessage={validationErrors}
            disabledInputsMap={disabledInputs}
            isSettings={settings}
          />
          <p>
            {user.mfa
              ? "Kétfaktoros hitelesítés bekapcsolva"
              : "Kétfaktoros hitelesítés kikapcsolva"}
          </p>
          <p>Szerepkör: {getUserRoleLabel(user.role)}</p>
          <p>{!user.banned ? "A fiók aktív" : "A fiók letiltva adminok által"}</p>
          
          <button className="btn" type="button" onClick={sendPassUpdate}>Jelszó frissítése</button>

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