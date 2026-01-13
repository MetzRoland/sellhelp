import type { SelectOption } from "../SelectComponent/SelectComponentTypes";
import type { RegisterForm } from "../../Register/RegisterTypes";

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