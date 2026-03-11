import { useState, useEffect, useLayoutEffect, type ReactNode } from "react";
import type { AxiosError } from "axios";
import { privateAxios, refreshAxios } from "../../config/axiosConfig";
import type {
  GoogleRegisterForm,
  LoginForm,
} from "../../components/Login/LoginTypes";
import type {
  InternalAxiosRequestConfigWithRetry,
  User,
  LoginCredentials,
  TotpCredentials,
  AuthContextType,
  GoogleRegister,
} from "./AuthProviderTypes";
import { AuthContext } from "./AuthContext";
import { useLocation, useNavigate } from "react-router";

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [authLoading, setAuthLoading] = useState(true);
  const [authError, setAuthError] = useState<string | null>(null);
  const [validationErrors, setValidationErrors] = useState<LoginForm>({
    email: "",
    password: "",
    totpCode: "",
  });
  const [googleRegisterErrors, setGoogleRegisterErrors] =
    useState<GoogleRegisterForm>({
      cityName: "",
      birthDate: "",
    });
  const [tempToken, setTempToken] = useState<string | null>(null);
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    const loadUser = async () => {
      if (location.pathname === "/finishGoogleRegistration") {
        setAuthLoading(false);
        return;
      }

      setAuthLoading(true);

      try {
        const res = await privateAxios.get<User>("/user/info");
        setUser(res.data);
        setAccessToken(res.data.accessToken);
      } catch {
        setUser(null);
        setAccessToken(null);
      } finally {
        setAuthLoading(false);
      }
    };

    loadUser();
  }, [location.pathname]);

  useLayoutEffect(() => {
    const authInterceptor = privateAxios.interceptors.request.use(
      (config: InternalAxiosRequestConfigWithRetry) => {
        if (accessToken && !config._retry) {
          config.headers.set("Authorization", `Bearer ${accessToken}`);
          console.log(accessToken);
        }
        return config;
      },
    );

    return () => {
      privateAxios.interceptors.request.eject(authInterceptor);
    };
  }, [accessToken]);

  useLayoutEffect(() => {
    let isRefreshing = false;

    const refreshInterceptor = privateAxios.interceptors.response.use(
      (response) => response,
      async (error: AxiosError) => {
        const originalRequest = error.config as
          | InternalAxiosRequestConfigWithRetry
          | undefined;

        const url = originalRequest?.url;

        const isMfaEndpoint =
          url?.includes("/auth/enable2fa") ||
          url?.includes("/user/update/password");

        if (isMfaEndpoint) {
          return Promise.reject(error);
        }

        if (
          error.response &&
          originalRequest &&
          !originalRequest._retry &&
          error.response.status === 401
        ) {
          if (isRefreshing) return Promise.reject(error);

          isRefreshing = true;
          setAuthLoading(true);

          try {
            const refreshRes = await refreshAxios.get("/auth/login/refresh");

            console.log(refreshRes.data.accessToken);
            setAccessToken(refreshRes.data.accessToken);
            originalRequest.headers.set(
              "Authorization",
              `Bearer ${refreshRes.data.accessToken}`,
            );

            const userInfo = await privateAxios.get<User>("/user/info");
            setUser(userInfo.data);

            originalRequest._retry = true;

            return privateAxios(originalRequest);
          } catch {
            setAccessToken(null);
            setUser(null);
            localStorage.removeItem("profilePicture");
          } finally {
            isRefreshing = false;
            setAuthLoading(false);
          }
        }

        return Promise.reject(error);
      },
    );

    return () => {
      privateAxios.interceptors.response.eject(refreshInterceptor);
    };
  }, []);

  const loginLocal = async (
    endpoint: string,
    credentials: LoginCredentials,
  ) => {
    try {
      const res = await privateAxios.post(endpoint, credentials);

      if (res.data.tempToken) setTempToken(res.data.tempToken);

      if (!res.data.tempToken) {
        const userInfo = await privateAxios.get<User>("/user/info");
        setUser(userInfo.data);
        setAccessToken(userInfo.data.accessToken);
      }

      setAuthError(null);
      setValidationErrors({ email: "", password: "", totpCode: "" });
    } catch (error) {
      const err = error as AxiosError<{ message?: string; errors?: LoginForm }>;
      setAuthError(err.response?.data?.message ?? "Sikertelen bejelentkezés!");
      setValidationErrors(
        err.response?.data?.errors ?? { email: "", password: "", totpCode: "" },
      );
    }
  };

  const verifyTotp = async (credentials: TotpCredentials) => {
    try {
      const res = await privateAxios.post("/auth/verify-totp", credentials);

      if (res.data.tempToken) setTempToken(res.data.tempToken);

      const userInfo = await privateAxios.get<User>("/user/info");
      setUser(userInfo.data);
      setAccessToken(userInfo.data.accessToken);

      setAuthError(null);
      setValidationErrors({ email: "", password: "", totpCode: "" });
    } catch (error) {
      const err = error as AxiosError<{ message?: string; errors?: LoginForm }>;
      setAuthError(err.response?.data?.message ?? "Helytelen hitelesítő kód!");
      setValidationErrors(
        err.response?.data?.errors ?? { email: "", password: "", totpCode: "" },
      );
    }
  };

  const handleGoogleLogin = async () => {
    window.location.href = "http://localhost:8080/auth/login/google";
  };

  const finishGoogleRegistration = async (registerData: GoogleRegister) => {
    try {
      const queryParams = new URLSearchParams(location.search);
      const tempToken = queryParams.get("tempToken");
      setTempToken(tempToken);

      await privateAxios.post(
        `/auth/google/register?tempToken=${tempToken}`,
        registerData,
      );

      const userInfo = await privateAxios.get<User>("/user/info");
      setUser(userInfo.data);
      setAccessToken(userInfo.data.accessToken);

      setAuthError(null);
      setGoogleRegisterErrors({ cityName: "", birthDate: "" });

      navigate("/home");
    } catch (error) {
      const err = error as AxiosError<{
        message?: string;
        errors?: GoogleRegister;
      }>;

      console.log(err.response?.data?.message);
      setAuthError(
        err.response?.data?.message ?? "Sikertelen google fiók regisztráció!",
      );
      setGoogleRegisterErrors(
        err.response?.data?.errors ?? { cityName: "", birthDate: "" },
      );

      throw err;
    }
  };

  const logout = async () => {
    try {
      await privateAxios.get("/user/logout");
    } catch (error) {
      console.error(error);
    } finally {
      setUser(null);
      setTempToken(null);
      setAuthError(null);
      setAccessToken(null);
      setValidationErrors({ email: "", password: "", totpCode: "" });

      localStorage.removeItem("profilePicture");
      navigate("/login");
    }
  };

  const value: AuthContextType = {
    user,
    isAuthenticated: !!accessToken,
    authLoading,
    loginLocal,
    verifyTotp,
    logout,
    authError,
    setAuthError,
    tempToken,
    setTempToken,
    validationErrors,
    setValidationErrors,
    accessToken,
    setAccessToken,
    finishGoogleRegistration,
    googleRegisterErrors,
    setGoogleRegisterErrors,
    handleGoogleLogin,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
