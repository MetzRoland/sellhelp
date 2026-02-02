export interface RegisterForm {
  lastName?: string;
  firstName?: string;
  birthDate?: string;
  cityName?: string;
  email?: string;
  password?: string;
}

export interface RegisterValidationErrors {
  lastName?: string;
  firstName?: string;
  birthDate?: string;
  cityName?: string;
  email?: string;
  password?: string;
}

export interface City {
  id: number;
  cityName: string;
  county: string;
}