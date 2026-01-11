import {
  useState,
  useEffect,
  useLayoutEffect,
  type ReactNode,
} from "react";
import type { AxiosError } from "axios";
import { privateAxios, refreshAxios } from "../../config/axiosConfig";
import type { LoginForm } from "../../components/Login/LoginTypes";
import type {InternalAxiosRequestConfigWithRetry, User, LoginCredentials, TotpCredentials, AuthContextType} from "./AuthProviderTypes";
import { AuthContext } from "./AuthContext";

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [authLoading, setAuthLoading] = useState(true);
  const [authError, setAuthError] = useState<string | null>(null);
  const [validationErrors, setValidationErrors] = useState<LoginForm>({
    email: "",
    password: "",
    totpCode: "",
  });
  const [tempToken, setTempToken] = useState<string | null>(null);
  const [accessToken, setAccessToken] = useState<string | null>(null);

  useEffect(() => {
    const loadUser = async () => {
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
  }, []);

  useLayoutEffect(() => {
    const authInterceptor = privateAxios.interceptors.request.use(
      (config: InternalAxiosRequestConfigWithRetry) => {
        if (accessToken && !config._retry) {
          config.headers.set("Authorization", `Bearer ${accessToken}`);
          console.log(accessToken);
        }
        return config;
      }
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
            originalRequest.headers.set("Authorization", `Bearer ${refreshRes.data.accessToken}`);

            const userInfo = await privateAxios.get<User>("/user/info");
            setUser(userInfo.data);

            originalRequest._retry = true;

            return privateAxios(originalRequest);
          } catch {
            setAccessToken(null);
            setUser(null);
          } finally {
            isRefreshing = false;
            setAuthLoading(false);
          }
        }

        return Promise.reject(error);
      }
    );

    return () => {
      privateAxios.interceptors.response.eject(refreshInterceptor);
    };
  }, []);

  const loginLocal = async (credentials: LoginCredentials) => {
    try {
      const res = await privateAxios.post("/auth/login", credentials);

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
        err.response?.data?.errors ?? { email: "", password: "", totpCode: "" }
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
        err.response?.data?.errors ?? { email: "", password: "", totpCode: "" }
      );
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
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
