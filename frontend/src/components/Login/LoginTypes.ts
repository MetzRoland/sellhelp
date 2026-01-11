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