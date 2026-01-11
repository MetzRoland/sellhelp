import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router";
import Header from "./../Header/Header";
import Footer from "../Footer/Footer";
import InputForm from "../Reusables/InputForm/InputForm";
import type {
  RegisterForm,
  RegisterValidationErrors,
  City,
} from "./RegisterTypes";
import { publicAxios } from "../../config/axiosConfig";

import "./Register.css";

function Register() {
  const registerInputs = [
  { name: "lastName", type: "text", placeholder: "Vezetéknév" },
  { name: "firstName", type: "text", placeholder: "Keresztnév" },
  { name: "birthDate", type: "date", placeholder: "Születési dátum" },
  { name: "cityName", type: "select", placeholder: "Település" },
  { name: "email", type: "text", placeholder: "Email" },
  { name: "password", type: "password", placeholder: "Jelszó" },
] as const;

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

  const [registrationError, setRegistrationError] = useState<string>("");

  const [success, setSuccess] = useState<boolean>(false);

  const [loading, setLoading] = useState<boolean>(false);

  const [cities, setCities] = useState<City[]>([]);

  const navigator = useNavigate();

  useEffect(() => {
    const fetchCities = async () => {
      const response = await publicAxios.get("/api/public/cities");

      setCities(response.data);
    };

    fetchCities();
  }, []);

  const cityOptions = cities.map((city) => ({
    id: city.id,
    value: city.cityName,
    label: city.cityName,
  }));

  const handleRegisterInput = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    setValidationErrors({});

    setRegistrationError("");

    console.log(formData);
  };

  const handleRequestSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    setLoading(true);

    try {
      const response = await publicAxios.post("/auth/register", formData);

      if (response.status === 201) {
        console.log(response.status);

        setSuccess(true);

        setLoading(false);

        setValidationErrors({});

        setRegistrationError("");

        setFormData({
          lastName: "",
          firstName: "",
          birthDate: "",
          cityName: "",
          email: "",
          password: "",
        });

        setTimeout(() => {
          navigator("/login");
        }, 2000);
      }
    } catch (error: unknown) {
      console.log(error.response.data);

      setSuccess(false);

      setLoading(false);

      setValidationErrors(error.response?.data?.errors ?? {});

      if (error.status !== 500) {
        setRegistrationError(
          error.response?.data?.message ?? "Sikertelen regisztráció!"
        );
      } else {
        setRegistrationError("Sikertelen regisztráció!");
      }
    }
  };

  return (
    <>
      <Header />
      <div className="container register-container">
        <h1 className="container-title">Regisztráció</h1>

        <form
          className="content-container registration-form"
          action=""
          onSubmit={handleRequestSubmit}
        >
          <InputForm<RegisterForm>
            inputs={registerInputs}
            formData={formData}
            handleFunction={handleRegisterInput}
            errorMessage={validationErrors}
            options={{ cityName: cityOptions }}
          />

          <button className="btn border-btn" type="submit" disabled={loading}>
            Regisztráció
          </button>

          {registrationError && (
            <p className="message error error-process-status">
              {registrationError}
            </p>
          )}

          {success && (
            <p className="message success-message">Sikeres regisztráció!</p>
          )}
        </form>

        <div className="content-container back-to-login-container">
          <h2>Van már fiókod?</h2>

          <Link to="#" className="btn">
            Bejelentkezés
          </Link>
        </div>
      </div>

      <Footer />
    </>
  );
}

export default Register;
