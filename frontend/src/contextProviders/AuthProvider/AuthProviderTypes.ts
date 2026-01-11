import type { LoginForm } from "../../components/Login/LoginTypes";
import type { InternalAxiosRequestConfig } from "axios";

export interface InternalAxiosRequestConfigWithRetry
  extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  mfa: boolean;
  accessToken: string;
}

export interface LoginCredentials {
  email: string;
  password: string;
}

export interface TotpCredentials {
  tempToken: string;
  totpCode: string;
}

export interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  authLoading: boolean;
  loginLocal: (credentials: LoginCredentials) => Promise<void>;
  verifyTotp: (credentials: TotpCredentials) => Promise<void>;
  logout: () => Promise<void>;
  authError: string | null;
  setAuthError: (error: string | null) => void;
  tempToken: string | null;
  setTempToken: (tempToken: string | null) => void;
  validationErrors: LoginForm;
  setValidationErrors: (validationErrors: LoginForm) => void;
  accessToken: string | null;
  setAccessToken: (accessToken: string | null) => void;
}