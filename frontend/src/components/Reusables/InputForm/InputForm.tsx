import InputComponent from "../InputComponent/InputComponent";
import SelectComponent from "../SelectComponent/SelectComponents";
import TextareaComponent from "../TextAreaComponent/TextAreaComponent";
import type { FormFields } from "../../genericTypes/FormFields";
import { useAuth } from "../../../contextProviders/AuthProvider/AuthContext";

interface InputFormProps<T extends object> {
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

function InputForm<T extends object>({
  inputs,
  errorMessage,
  handleFunction,
  formData,
  disabledInputsMap = {},
  options,
  settingInputsMap = {},
  disabledToggle = async () => {},
}: InputFormProps<T>) {
  const { user } = useAuth();

  return (
    <>
      {inputs.map((input) => (
        <div className="setting-container" key={String(input.name)}>
          {input.type === "select" ? (
            <SelectComponent
              errorMessage={errorMessage?.[input.name] || ""}
              inputName={String(input.name)}
              handleFunction={handleFunction}
              isDisabled={disabledInputsMap?.[String(input.name)] ?? false}
              options={options?.[input.name] ?? []}
              defaultOption={input.placeholder}
              selectValue={formData[input.name]}
            />
          ) : input.type === "textarea" ? (
            <TextareaComponent
              errorMessage={errorMessage?.[input.name] || ""}
              inputName={String(input.name)}
              inputValue={formData[input.name]}
              inputPlaceholder={input.placeholder}
              isDisabled={disabledInputsMap?.[String(input.name)] ?? false}
              handleFunction={handleFunction}
            />
          ) : (
            <InputComponent
              errorMessage={errorMessage?.[input.name] || ""}
              inputType={input.type}
              inputName={String(input.name)}
              inputValue={formData[input.name]}
              inputPlaceholder={input.placeholder}
              isDisabled={disabledInputsMap?.[String(input.name)] ?? false}
              handleFunction={handleFunction}
            />
          )}

          {settingInputsMap?.[String(input.name)] && (
            <button
              type="button"
              className="setting-btn"
              disabled={user?.authProvider === "GOOGLE" && input.name === "email"}
              onClick={() => disabledToggle(input.name)}
            >
              {disabledInputsMap[String(input.name)] ? "Módosítás" : "Mentés"}
            </button>
          )}
        </div>
      ))}
    </>
  );
}

export default InputForm;
