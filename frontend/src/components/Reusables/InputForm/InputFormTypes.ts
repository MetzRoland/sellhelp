import type { SelectOption } from "../SelectComponent/SelectComponentTypes";
import type { RegisterForm } from "../../Register/RegisterTypes";
import type { FormFields } from "../../genericTypes/FormFields";

export interface InputFormProps<T extends object> {
  inputs: readonly {
    name: keyof FormFields<T>;
    type: string;
    placeholder: string;
  }[];
  errorMessage?: Partial<Record<keyof FormFields<T>, string>>;
  handleFunction: (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>,
  ) => void;
  formData: FormFields<T>;
  options?: {
    [K in keyof FormFields<T>]?: { id: number; value: string; label: string }[];
  };
  disabledInputsMap?: Record<string, boolean>;
  disabledToggle?: (inputName: keyof FormFields<T>) => Promise<void>;
  settingInputsMap?: Record<string, boolean>;
}

export interface InputFormType{
    inputs: {
        name: keyof RegisterForm;
        type: string;
        placeholder: string;
      }[],
    errorMessage?: string,
    handleFunction: (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => void,
    formData: FormData,
    options?: SelectOption[]
}