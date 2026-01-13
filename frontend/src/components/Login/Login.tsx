import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router";
import Header from "../Header/Header";
import Footer from "../Footer/Footer";
import { useAuth } from "../../contextProviders/AuthProvider/AuthContext";
import type { LoginForm } from "./LoginTypes";
import InputForm from "../Reusables/InputForm/InputForm";

import "./Login.css";

function Login() {
  const {
    loginLocal,
    tempToken,
    verifyTotp,
    validationErrors,
    setValidationErrors,
    authError,
    user,
  } = useAuth();

  const [formData, setFormData] = useState<LoginForm>({
    email: "",
    password: "",
    totpCode: "",
  });

  const navigate = useNavigate();

  useEffect(() => {
    if (user) {
      setTimeout(() => {
        navigate("/home");
      }, 2000);
    }
  }, [user, navigate]);

  const handleInputChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    setValidationErrors({
      email: "",
      password: "",
      totpCode: "",
    });
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    try {
      await loginLocal({
        email: formData.email,
        password: formData.password,
      });

      if (tempToken) {
        await verifyTotp({ totpCode: formData.totpCode, tempToken });
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleGoogleLogin = () => {
    window.location.href = "http://localhost:8080/auth/login/google";
  };

  const loginInputs = [
    { name: "email", type: "text", placeholder: "Email" },
    { name: "password", type: "password", placeholder: "Jelszó" },
  ] as const;

  const totpInputs = [
    { name: "totpCode", type: "text", placeholder: "Hitelesítő kód" },
  ] as const;

  return (
    <>
      <Header />

      <div className="main-container">
        <h1 className="container-title">Bejelentkezés</h1>

        <form className="content-container login-form" onSubmit={handleSubmit}>
          {!tempToken ? (
            <InputForm<LoginForm>
              inputs={loginInputs}
              formData={formData}
              handleFunction={handleInputChange}
              errorMessage={validationErrors}
            />
          ) : (
            <InputForm<LoginForm>
              inputs={totpInputs}
              formData={formData}
              handleFunction={handleInputChange}
              errorMessage={validationErrors}
            />
          )}

          <button className="btn border-btn btn-highlight" type="submit">
            {tempToken ? "Ellenőrzés" : "Bejelentkezés"}
          </button>

          <button
            className="btn border-btn"
            type="button"
            onClick={handleGoogleLogin}
          >
            Folytatás Google fiókkal
          </button>

          {authError && (
            <p className="message error error-process-status">{authError}</p>
          )}

          {user && (
            <p className="message success-message">Sikeres Bejelentkezés!</p>
          )}
        </form>

        <div className="content-container back-to-register-container">
          <h2>Nincs még fiókod?</h2>
          <Link to="/register" className="btn">
            Regisztráció
          </Link>
        </div>
      </div>

      <Footer />
    </>
  );
}

export default Login;
