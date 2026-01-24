export interface UserUpdateForm {
  lastName?: string;
  firstName?: string;
  birthDate?: string;
  cityName?: string;
  email?: string;
  role?: string;
  isBanned?: string;
}

export interface UserUpdateValidationErrors {
  lastName?: string;
  firstName?: string;
  birthDate?: string;
  cityName?: string;
  email?: string;
  role?: string;
  isBanned?: string;
}