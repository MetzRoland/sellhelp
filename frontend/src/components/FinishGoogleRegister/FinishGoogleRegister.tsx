import Header from "../Header/Header";
import Footer from "../Footer/Footer";
import InputForm from "../Reusables/InputForm/InputForm";
import type { GoogleRegister } from "../../contextProviders/AuthProvider/AuthProviderTypes";
import type { City } from "../Register/RegisterTypes";
import { publicAxios } from "../../config/axiosConfig";
import { useAuth } from "../../contextProviders/AuthProvider/AuthContext";
import { useState, useEffect } from "react";
import type { GoogleRegisterForm } from "../Login/LoginTypes";

function FinishGoogleRegister() {
  const googleRegisterInputs = [
    { name: "birthDate", type: "date", placeholder: "Születési dátum" },
    { name: "cityName", type: "select", placeholder: "Település" },
  ] as const;

  const [formData, setFormData] = useState<GoogleRegister>({
    cityName: "",
    birthDate: "",
  });

  const {googleRegisterErrors, setGoogleRegisterErrors, authError, finishGoogleRegistration} = useAuth();

  const [cities, setCities] = useState<City[]>([]);

  const [success, setSuccess] = useState<boolean>(false);

  const [loading, setLoading] = useState<boolean>(false);

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

    setGoogleRegisterErrors({
      cityName: "",
      birthDate: "",
    });
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setLoading(true);

    try {
      await finishGoogleRegistration({
        cityName: formData.cityName,
        birthDate: formData.birthDate,
      });

      setSuccess(true);
    } catch (err) {
      console.error(err);
      setSuccess(false);
    }
    finally{
        setLoading(false);
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
          onSubmit={handleSubmit}
        >
          <InputForm<GoogleRegisterForm>
            inputs={googleRegisterInputs}
            formData={formData}
            handleFunction={handleRegisterInput}
            errorMessage={googleRegisterErrors}
            options={{ cityName: cityOptions }}
          />

          <button className="btn border-btn" type="submit" disabled={loading}>
            Regisztráció
          </button>

          {authError && (
            <p className="message error error-process-status">
              {authError}
            </p>
          )}

          {success && (
            <p className="message success-message">Sikeres regisztráció!</p>
          )}
        </form>
      </div>

      <Footer />
    </>
  );
}

export default FinishGoogleRegister;
