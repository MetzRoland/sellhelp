import { useState } from "react";
import { useNavigate, useLocation } from "react-router";
import { useLoading } from "../../contextProviders/ProccessLoadProvider/ProccessLoadContext";
import { AxiosError } from "axios";
import Header from "../Header/Header";
import Footer from "../Footer/Footer";
import InputForm from "../Reusables/InputForm/InputForm";
import { privateAxios } from "../../config/axiosConfig";

function ResetPassword() {
  interface ResetPasswordForm {
    password: string;
    confirm: string;
    token: string;
  }

  interface ResetPasswordValidationErrors {
    password?: string;
    confirm?: string;
  }

  const navigate = useNavigate();
  const location = useLocation();
  const params = new URLSearchParams(location.search);
  const token = params.get("token");

  console.log(token);

  const resetPasswordInputs = [
    { name: "password", type: "password", placeholder: "Jelszó" },
    { name: "confirm", type: "password", placeholder: "Jelszó mégegyszer" },
  ] as const;

  const { setIsLoading, setLoadingMessage } = useLoading();

  const [formData, setFormData] = useState<ResetPasswordForm>({
    password: "",
    confirm: "",
    token: ""
  });
  const [validationErrors, setValidationErrors] =
    useState<ResetPasswordValidationErrors>({});
  const [resetPasswordError, setResetPasswordError] = useState("");

  const handleInputChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>,
  ) => {
    const { name, value } = e.target;

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    setValidationErrors({ password: "", confirm: "" });
    setResetPasswordError("");
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if(formData.password !== formData.confirm){
        setResetPasswordError("A két jelszó nem egyezik!");
        return;
    }

    try {
      setIsLoading(true);
      setLoadingMessage("Jelszómódosítás...");

      await privateAxios.patch<ResetPasswordForm>("/user/update/password", {
        password: formData.password,
        token: token
      });

      setTimeout(() => {
        navigate("/home/settings");
      }, 2000);
    } catch (err) {
      const error = err as AxiosError<{
        message?: string;
        errors?: ResetPasswordValidationErrors;
      }>;

      setValidationErrors(error.response?.data?.errors ?? {});
      setResetPasswordError(error.response?.data?.message ?? "Sikertelen jelszómódosítás!");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <>
      <Header />

      <div className="main-container reset-password-container">
        <h1 className="container-title">Jelszó módosítás</h1>
        <form className="content-container reset-password-content-container" onSubmit={handleSubmit}>
          <InputForm<ResetPasswordForm>
            inputs={resetPasswordInputs}
            formData={formData}
            handleFunction={handleInputChange}
            errorMessage={validationErrors}
          />

          <button className="btn btn-highlight" type="submit">
            Módosít
          </button>

          {resetPasswordError && (
            <p className="message error error-process-status">{resetPasswordError}</p>
          )}
        </form>
      </div>

      <Footer />
    </>
  );
}

export default ResetPassword;
