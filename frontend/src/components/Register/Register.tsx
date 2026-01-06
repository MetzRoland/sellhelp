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
  const inputs: {
    name: keyof RegisterForm;
    type: string;
    placeholder: string;
  }[] = [
    { name: "lastName", type: "text", placeholder: "Vezetéknév" },
    { name: "firstName", type: "text", placeholder: "Keresztnév" },
    { name: "birthDate", type: "date", placeholder: "Születési dátum" },
    { name: "cityName", type: "text", placeholder: "Település" },
    { name: "email", type: "text", placeholder: "Email" },
    { name: "password", type: "text", placeholder: "Jelszó" },
  ];

  const [formData, setFormData] = useState<RegisterForm>({
    lastName: "",
    firstName: "",
    birthDate: "",
    cityName: "",
    email: "",
    password: "",
  });

  const [validationErrors, setValidationErrors] =
    useState<RegisterValidationErrors>({});

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
          {inputs.map((input) => (
            <div className="input-container" key={input.name}>
              <input
                type={input.type}
                name={input.name}
                value={formData[input.name] || ""}
                placeholder={input.placeholder}
                onChange={handleRegisterInput}
              />
              {validationErrors[input.name] && (
                <span className="error-span">
                  {validationErrors[input.name]}
                </span>
              )}
            </div>
          ))}

          <button type="submit" disabled={loading}>
            Regisztráció
          </button>

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
