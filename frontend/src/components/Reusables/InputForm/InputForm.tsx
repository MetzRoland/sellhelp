import InputComponent from "../InputComponent/InputComponent";
import SelectComponent from "../SelectComponent/SelectComponents";
import type { FormFields } from "../../genericTypes/FormFields";

interface InputFormProps<T extends object> {
  inputs: readonly{
    name: keyof FormFields<T>;
    type: string;
    placeholder: string;
  }[];
  errorMessage?: Partial<Record<keyof FormFields<T>, string>>;
  handleFunction: (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => void;
  formData: FormFields<T>;
  options?: {
    [K in keyof FormFields<T>]?: { id: number; value: string; label: string }[];
  };
}

function InputForm<T extends object>({
  inputs,
  errorMessage,
  handleFunction,
  formData,
  options,
}: InputFormProps<T>) {
  return (
    <>
      {inputs.map((input) =>
        input.type !== "select" ? (
          <InputComponent
            key={String(input.name)}
            errorMessage={errorMessage?.[input.name]}
            inputType={input.type}
            inputName={input.name}
            inputValue={formData[input.name]}
            inputPlaceholder={input.placeholder}
            handleFunction={handleFunction}
          />
        ) : (
          <SelectComponent
            key={String(input.name)}
            errorMessage={errorMessage?.[input.name]}
            inputName={input.name}
            handleFunction={handleFunction}
            options={options?.[input.name] ?? []}
            defaultOption={input.placeholder}
          />
        )
      )}
    </>
  );
}

export default InputForm;
