import type { SelectOption } from "../SelectComponent/SelectComponentTypes";

export interface InputFormType{
    inputs: {
        name: keyof RegisterForm;
        type: string;
        placeholder: string;
      }[],
    errorMessage?: string,
    handleFunction: (e: React.ChangeEvent<HTMLInputElement>) => void,
    formData: FormData,
    options?: SelectOption[]
}