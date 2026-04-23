import { useState, useEffect } from "react";
import { useNavigate } from "react-router";
import Header from "../Header/Header";
import Footer from "../Footer/Footer";
import type {
  NewPostForm,
  NewPostValidationErrors,
} from "./NewPostComponentTypes";
import InputForm from "../Reusables/InputForm/InputForm";
import { privateAxios, publicAxios } from "../../config/axiosConfig";
import { useLoading } from "../../contextProviders/ProccessLoadProvider/ProccessLoadContext";
import type { City } from "../Register/RegisterTypes";
import { AxiosError } from "axios";

function NewPostComponent() {
  const { setIsLoading, setLoadingMessage } = useLoading();

  const navigate = useNavigate();

  const [cities, setCities] = useState<City[]>([]);

  const newPostInputs = [
    { name: "title", type: "text", placeholder: "A poszt címe" },
    { name: "description", type: "textarea", placeholder: "Leírás" },
    { name: "cityName", type: "select", placeholder: "Válasszon települést" , userTitle: "Település"},
    { name: "reward", type: "number", placeholder: "Munkadíj" },
  ] as const;

  const [formData, setFormData] = useState<NewPostForm>({
    title: "",
    description: "",
    cityName: "",
    reward: "",
  });

  const [newPostError, setNewPostError] = useState("");

  const [validationErrors, setValidationErrors] =
    useState<NewPostValidationErrors>({});

  useEffect(() => {
    const fetchCities = async () => {
      try {
        setIsLoading(true);
        const response = await publicAxios.get("/api/public/cities");
        setCities(response.data);
      } catch {
        setCities([]);
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

  const handleInputChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>,
  ) => {
    const { name, value } = e.target;

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    setValidationErrors({});
  };

  const handleSubmitRequest = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    setIsLoading(true);
    setLoadingMessage("Poszt létrehozása...");

    try {
      const response = await privateAxios.post("/post/new", formData);

      navigate(`/posts/${response.data.id}`);
    } catch (err) {
      const error = err as AxiosError<{
        message?: string;
        errors?: NewPostForm;
      }>;

      setValidationErrors(error.response?.data?.errors ?? {});

      setNewPostError(
        error.response?.data?.message ?? "Sikertelen poszt létrehozás!",
      );
    } finally {
      setIsLoading(false);
      setLoadingMessage("");
    }
  };

  return (
    <>
      <Header />

      <div className="main-container">
        <h1 className="content-title">Új poszt létrehozása</h1>

        <form className="content-container" onSubmit={handleSubmitRequest}>
          <InputForm<NewPostForm>
            inputs={newPostInputs}
            formData={formData}
            handleFunction={handleInputChange}
            errorMessage={validationErrors}
            options={{ cityName: cityOptions }}
          />

          <button type="submit" className="btn btn-highlight">
            Poszt létrehozása
          </button>

          {newPostError && (
            <p className="message error error-process-status">
              {newPostError}
            </p>
          )}
        </form>
      </div>

      <Footer />
    </>
  );
}

export default NewPostComponent;
