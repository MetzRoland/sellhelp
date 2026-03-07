import { useState } from "react";
import { useNavigate, Link } from "react-router";
import { AxiosError } from "axios";
import Header from "../Header/Header";
import Footer from "../Footer/Footer";
import { useLoading } from "../../contextProviders/ProccessLoadProvider/ProccessLoadContext";
import InputForm from "../Reusables/InputForm/InputForm";
import { publicAxios } from "../../config/axiosConfig";
import type { ResetPasswordEmailForm, ResetPasswordEmailValidationErrors } from "./ForgetPasswordEmailTypes";

function ForgetPasswordEmail() {
  const navigator = useNavigate();

  const resetPasswordEmailInputs = [
    { name: "email", type: "text", placeholder: "Adja meg az email címét..." },
  ] as const;

  const { setIsLoading, setLoadingMessage } = useLoading();

  const [formData, setFormData] = useState<ResetPasswordEmailForm>({
    email: "",
  });

  const [validationErrors, setValidationErrors] =
    useState<ResetPasswordEmailValidationErrors>({});

  const [resetPasswordEmailError, setResetPasswordEmailError] = useState<string>("");

  const [success, setSuccess] = useState<boolean>(false);

  const handleInputChange = (
    e: React.ChangeEvent<
      HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement
    >,
  ) => {
    const { name, value } = e.target;

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    setValidationErrors({ email: "" });
    setResetPasswordEmailError("");
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    try {
      setIsLoading(true);
      setLoadingMessage("Jelszó helyreállító email küldése...");

      await publicAxios.patch<ResetPasswordEmailForm>(
        "/auth/forgotPasswordEmail",
        formData,
      );

      setSuccess(true);

      setTimeout(() => {
        navigator("/login");
      }, 2000);
    } catch (err) {
      const error = err as AxiosError<{
        message?: string;
        errors?: ResetPasswordEmailValidationErrors;
      }>;

      setValidationErrors(error.response?.data?.errors ?? {});
      setResetPasswordEmailError(
        error.response?.data?.message ?? "Hiba az email küldése során!",
      );
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <>
      <Header />

      <div className="main-container reset-password-container">
        <h1 className="container-title">Jelszó helyreállítás</h1>
        <form
          className="content-container reset-password-content-container"
          onSubmit={handleSubmit}
        >
          <InputForm<ResetPasswordEmailForm>
            inputs={resetPasswordEmailInputs}
            formData={formData}
            handleFunction={handleInputChange}
            errorMessage={validationErrors}
          />

          <button className="btn btn-highlight" type="submit">
            Email küldése
          </button>

          <Link to="/login" className="btn">Vissza</Link>

          {resetPasswordEmailError && (
            <p className="message error error-process-status">
              {resetPasswordEmailError}
            </p>
          )}

          {success && (
            <p className="message success-message">Jelszó helyreállító email elküldve!</p>
          )}
        </form>
      </div>

      <Footer />
    </>
  );
}

export default ForgetPasswordEmail;
