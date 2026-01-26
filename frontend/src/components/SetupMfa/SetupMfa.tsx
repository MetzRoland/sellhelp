import { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router";
import { useLoading } from "../../contextProviders/ProccessLoadProvider/ProccessLoadContext";
import Header from "../Header/Header";
import Footer from "../Footer/Footer";
import InputForm from "../Reusables/InputForm/InputForm";
import type {
  SetupMfaForm,
  SetupMfaFormValidationErrors,
} from "./SetupMfaTypes";
import { privateAxios } from "../../config/axiosConfig";
import type { AxiosError } from "axios";

import "./SetupMfa.css";

interface SetupMfaResponse {
  totpSecret: string;
  qrCode: string;
  tempToken: string;
}

interface FirstTotpValidationRequest {
  totpSecret: string;
  qrCode: string;
  tempToken: string;
  totpCode: string;
}

function SetupMfa() {
  const navigate = useNavigate();

  const setupMfaInputs = [
    { name: "totpCode", type: "text", placeholder: "Hitelesítő kód" },
  ] as const;

  const { setIsLoading, setLoadingMessage } = useLoading();

  const [formData, setFormData] = useState<SetupMfaForm>({ totpCode: "" });
  const [qrCode, setQrCode] = useState<string | null>(null);
  const [tempToken, setTempToken] = useState<string | null>(null);
  const [totpSecret, setTotpSecret] = useState<string | null>(null);
  const [validationErrors, setValidationErrors] = useState<SetupMfaFormValidationErrors>({});
  const [setupMfaError, setSetupMfaError] = useState("");

  useEffect(() => {
    const fetchSetupMfaData = async () => {
      try {
        setIsLoading(true);
        setLoadingMessage("QR kód generálása...");

        const response = await privateAxios.get<SetupMfaResponse>("/auth/setup2fa");

        setQrCode(response.data.qrCode);
        setTempToken(response.data.tempToken);
        setTotpSecret(response.data.totpSecret);
      } catch (err) {
        console.error(err);
        setQrCode(null);
        setTempToken(null);
        setTotpSecret(null);
      } finally {
        setIsLoading(false);
      }
    };

    fetchSetupMfaData();
  }, [setIsLoading, setLoadingMessage]);

  const handleInputChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    setValidationErrors({ totpCode: "" });
    setSetupMfaError("");
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    try {
      setIsLoading(true);
      setLoadingMessage("Kétfaktoros hitelesítés bekapcsolása...");

      await privateAxios.post<FirstTotpValidationRequest>("/auth/enable2fa", {
        totpSecret,
        qrCode,
        tempToken,
        totpCode: formData.totpCode,
      });

      setTimeout(() => {
        navigate("/home/settings");
      }, 2000);
    } catch (err) {
      const error = err as AxiosError<{
        message?: string;
        errors?: SetupMfaFormValidationErrors;
      }>;

      setValidationErrors(error.response?.data?.errors ?? {});
      setSetupMfaError(error.response?.data?.message ?? "Helytelen adatok!");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <>
      <Header />

      <div className="main-container setupmfa-container">
        <h1 className="container-title">Kétfaktoros hitelesítés engedélyezése</h1>
        <form
          className="content-container setupmfa-content-container"
          onSubmit={handleSubmit}
        >
          {qrCode ? (
            <div className="qr-code-container">
              <h2>Szkennelje be a QR kódot!</h2>
              <img src={`data:image/png;base64,${qrCode}`} alt="QR code" />
            </div>
          ) : (
            <p className="message error error-process-status">Nem sikerült megjeleníteni a QR kódot!</p>
          )}

          <InputForm<SetupMfaForm>
            inputs={setupMfaInputs}
            formData={formData}
            handleFunction={handleInputChange}
            errorMessage={validationErrors}
          />

          <button className="btn btn-highlight" type="submit">
            Tovább
          </button>

          <Link to="/home/settings" className="btn">
            Vissza
          </Link>

          {setupMfaError && (
            <p className="message error error-process-status">{setupMfaError}</p>
          )}
        </form>
      </div>

      <Footer />
    </>
  );
}

export default SetupMfa;
