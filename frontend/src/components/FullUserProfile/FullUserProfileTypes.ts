import type { FormFields } from "../genericTypes/FormFields";

export interface UserUpdateForm {
  lastName?: string;
  firstName?: string;
  birthDate?: string;
  cityName?: string;
  email?: string;
  role?: string;
}

export type UserUpdateFormFields = FormFields<UserUpdateForm>;

export interface UserUpdateValidationErrors {
  lastName?: string;
  firstName?: string;
  birthDate?: string;
  cityName?: string;
  email?: string;
}