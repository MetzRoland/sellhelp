import type { GoogleRegister } from "../../contextProviders/AuthProvider/AuthProviderTypes";
import type { FormFields } from "../genericTypes/FormFields";

export interface LoginFields {
  email: string;
  password: string;
  totpCode: string;
}

export interface LoginValidationErrors {
  email?: string;
  password?: string;
  totpCode?: string;
}

export type LoginForm = FormFields<LoginFields>;

export type GoogleRegisterForm = FormFields<GoogleRegister>;

export interface IsAdminLogin{
  isAdminLogin?: boolean;
}