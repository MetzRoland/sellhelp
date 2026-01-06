import { useState } from "react";
import { Link } from "react-router";
import Header from "./../Header/Header";
import { publicAxois } from "../../config/axoisConfig";

interface RegisterForm {
  lastName?: string;
  firstName?: string;
  birthDate?: string;
  cityName?: string;
  email?: string;
  password?: string;
}

interface RegisterValidationErrors {
  lastName?: string;
  firstName?: string;
  birthDate?: string;
  cityName?: string;
  email?: string;
  password?: string;
}

interface RegisterError {
  message?: string;
}

function Register() {
  const [formData, setFormData] = useState<RegisterForm>({
    lastName: "",
    firstName: "",
    birthDate: "",
    cityName: "",
    email: "",
    password: "",
  });

  const [validationErrors, setValidationErrors] = useState<RegisterValidationErrors>({});

  const [registrationError, setRegistrationError] = useState<RegisterError>({});

  const [success, setSuccess] = useState<boolean>(false);

  const [loading, setLoading] = useState<boolean>(false);

  const handleRegisterInput = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    setValidationErrors({});

    setRegistrationError({});

    console.log(formData);
  };

  const handleRequestSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    setLoading(true);

    try {
      const response = await publicAxois.post("/auth/register", formData);

      if (response.status === 201) {
        console.log(response.status);

        setSuccess(true);

        setLoading(false);

        setValidationErrors({});

        setRegistrationError({});

        setFormData({
          lastName: "",
          firstName: "",
          birthDate: "",
          cityName: "",
          email: "",
          password: "",
        });
      }
    } catch (error: unknown) {
      console.log(error.response.data);

      setSuccess(false);

      setLoading(false);

      setValidationErrors(error.response?.data?.errors ?? {});

      setRegistrationError({
        message:
          error.response?.data?.message ?? "Hiba törént a regisztráció során!",
      });
    }
  };

  return (
    <>
      <Header />
      <div className="register-container">
        <h1>Regisztráció</h1>

        <form
          className="registration-form"
          action=""
          onSubmit={handleRequestSubmit}
        >
          <div className="input-container">
            <input
              type="text"
              name="lastName"
              value={formData.lastName}
              placeholder="Vezetéknév"
              onChange={handleRegisterInput}
            />
            {validationErrors.lastName && (
              <span className="error-span">{validationErrors.lastName}</span>
            )}
          </div>
          <div className="input-container">
            <input
              type="text"
              name="firstName"
              value={formData.firstName}
              placeholder="Keresztnév"
              onChange={handleRegisterInput}
            />
            {validationErrors.firstName && (
              <span className="error-span">{validationErrors.firstName}</span>
            )}
          </div>
          <div className="input-container">
            <input
              type="date"
              name="birthDate"
              value={formData.birthDate}
              placeholder="Születési dátum"
              onChange={handleRegisterInput}
            />
            {validationErrors.birthDate && (
              <span className="error-span">{validationErrors.birthDate}</span>
            )}
          </div>
          <div className="input-container">
            <input
              type="text"
              name="cityName"
              value={formData.cityName}
              placeholder="Település"
              onChange={handleRegisterInput}
            />
            {validationErrors.cityName && (
              <span className="error-span">{validationErrors.cityName}</span>
            )}
          </div>
          <div className="input-container">
            <input
              type="text"
              name="email"
              value={formData.email}
              placeholder="Email"
              onChange={handleRegisterInput}
            />
            {validationErrors.email && (
              <span className="error-span">{validationErrors.email}</span>
            )}
          </div>
          <div className="input-container">
            <input
              type="text"
              name="password"
              value={formData.password}
              placeholder="Jelszó"
              onChange={handleRegisterInput}
            />
            {validationErrors.password && (
              <span className="error-span">{validationErrors.password}</span>
            )}
          </div>

          <button type="submit" disabled={loading}>Regisztráció</button>

          {!success ? (
            <p>{registrationError.message}</p>
          ) : (
            <p>Sikeres regisztráció!</p>
          )}
        </form>

        <div className="login-container">
          <h2>Van már fiókod?</h2>

          <Link to="#">Bejelentkezés</Link>
        </div>
      </div>
    </>
  );
}

export default Register;
