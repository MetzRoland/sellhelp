import type { FormFields } from "../genericTypes/FormFields";

export interface NewPostFields {
  title: string;
  description: string;
  cityName: string;
  reward: string;
}

export interface NewPostValidationErrors {
  title?: string;
  description?: string;
  cityName?: string;
  reward?: string;
}

export type NewPostForm = FormFields<NewPostFields>;