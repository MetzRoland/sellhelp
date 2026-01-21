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
import { useLoading } from "../../contextProviders/ProccessLoadProvider/ProccessLoadContext";
import type { AxiosError } from "axios";

import "./Register.css";

function Register() {
    const registerInputs = [
        { name: "lastName", type: "text", placeholder: "Vezetéknév" },
        { name: "firstName", type: "text", placeholder: "Keresztnév" },
        { name: "birthDate", type: "date", placeholder: "Születési dátum" },
        { name: "cityName", type: "select", placeholder: "Válasszon települést" },
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

    const [registrationError, setRegistrationError] = useState("");
    const [success, setSuccess] = useState(false);
    const [cities, setCities] = useState<City[]>([]);

    const navigator = useNavigate();

    const { setIsLoading, setLoadingMessage } = useLoading();

    useEffect(() => {
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
    }, [setIsLoading]);

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
    };

    const handleRequestSubmit = async (
        e: React.FormEvent<HTMLFormElement>
    ) => {
        e.preventDefault();

        try {
            setIsLoading(true);
            setLoadingMessage("Regisztrálás...");

            const response = await publicAxios.post(
                "/auth/register",
                formData
            );

            if (response.status === 201) {
                setSuccess(true);
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
        } catch (err) {
            const error = err as AxiosError<{ message?: string; errors?: RegisterForm }>;

            setSuccess(false);
            setValidationErrors(
                error.response?.data?.errors ?? {}
            );

            setRegistrationError(
                error.response?.data?.message ??
                    "Sikertelen regisztráció!"
            );
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <>
            <Header />
            <div className="main-container register-container">
                <h1 className="container-title">Regisztráció</h1>

                <form
                    className="content-container registration-form"
                    onSubmit={handleRequestSubmit}
                >
                    <InputForm<RegisterForm>
                        inputs={registerInputs}
                        formData={formData}
                        handleFunction={handleRegisterInput}
                        errorMessage={validationErrors}
                        options={{ cityName: cityOptions }}
                    />

                    <button className="btn btn-highlight" type="submit">
                        Regisztráció
                    </button>

                    {registrationError && (
                        <p className="message error error-process-status">
                            {registrationError}
                        </p>
                    )}

                    {success && (
                        <p className="message success-message">
                            Sikeres regisztráció!
                        </p>
                    )}
                </form>

                <div className="content-container back-to-login-container">
                    <h2>Van már fiókod?</h2>
                    <Link to="/login" className="btn">
                        Bejelentkezés
                    </Link>
                </div>
            </div>
            <Footer />
        </>
    );
}

export default Register;
