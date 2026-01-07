import { useState, useEffect } from "react";
import { Link } from "react-router";
import Header from "./../Header/Header";
import { publicAxois } from "../../config/axoisConfig";

import "./Register.css";

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

interface City {
  id: number;
  cityName: string;
  county: string;
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

  const [isRegistrationError, setRegistrationError] = useState<boolean>(false);

  const [success, setSuccess] = useState<boolean>(false);

  const [loading, setLoading] = useState<boolean>(false);

  const [cities, setCities] = useState<City[]>([]);

  useEffect(() => {
    const fetchCities = async () => {
      const response = await publicAxois.get("/api/public/cities");

      setCities(response.data);
    };

    fetchCities();
  }, []);

  const handleRegisterInput = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    setValidationErrors({});

    setRegistrationError(false);

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

        setRegistrationError(false);

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

      setRegistrationError(true);
    }
  };

  return (
    <>
      <Header />
      <div className="container register-container">
        <h1>Regisztráció</h1>

        <form
          className="content-container registration-form"
          action=""
          onSubmit={handleRequestSubmit}
        >
          {inputs.map((input) => (
            <div className="input-container" key={input.name}>
              {validationErrors[input.name] && (
                <span className="message error error-span">
                  {validationErrors[input.name]}
                </span>
              )}
              {input.name !== "cityName" ? (
                <input
                  type={input.type}
                  name={input.name}
                  value={formData[input.name] || ""}
                  placeholder={input.placeholder}
                  onChange={handleRegisterInput}
                  className="input-element"
                />
              ) : (
                <select
                  name={input.name}
                  id={input.name}
                  className="input-element select-input-element"
                  onChange={handleRegisterInput}
                  defaultValue="city"
                >
                  <option value="city" disabled hidden>
                    Válasszon települést!
                  </option>
                  {cities.map((city) => {
                    return (
                      <option key={city.id} value={city.cityName}>
                        {city.cityName}
                      </option>
                    );
                  })}
                </select>
              )}
            </div>
          ))}

          <button className="btn" type="submit" disabled={loading}>
            Regisztráció
          </button>

          {isRegistrationError && (
            <p className="message error error-process-status">
              Sikertelen regisztráció!
            </p>
          )}

          {success && <p className="message success-message">Sikeres regisztráció!</p>}
        </form>

        <div className="content-container login-container">
          <h2>Van már fiókod?</h2>

          <Link to="#" className="btn">
            Bejelentkezés
          </Link>
        </div>
      </div>
    </>
  );
}

export default Register;
