import InputComponent from "../InputComponent/InputComponent";
import SelectComponent from "../SelectComponent/SelectComponents";
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
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>,
  ) => void;
  formData: FormFields<T>;
  options?: {
    [K in keyof FormFields<T>]?: { id: number; value: string; label: string }[];
  };
  disabledInputsMap?: Record<string, boolean>;
  disabledToggle?: (inputName: string) => void;
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
  disabledToggle = () => {},
}: InputFormProps<T>) {
  const { user } = useAuth();

  return (
    <>
      {inputs.map((input) =>
        input.type !== "select" ? (
          <div className="setting-container">
            <InputComponent
              key={String(input.name)}
              errorMessage={errorMessage?.[input.name] || ""}
              inputType={input.type}
              inputName={String(input.name)}
              inputValue={formData[input.name]}
              inputPlaceholder={input.placeholder}
              isDisabled={disabledInputsMap?.[String(input.name)] ?? false}
              handleFunction={handleFunction}
            />

            {settingInputsMap?.[String(input.name)] && (
              <button
                type="button"
                className="setting-btn"
                disabled={
                  user?.authProvider === "GOOGLE" && input.name === "email"
                }
                onClick={() => disabledToggle(String(input.name))}
              >
                {disabledInputsMap[input.name] ? "Módosítás" : "Mentés"}
              </button>
            )}
          </div>
        ) : (
          <>
            <div className="setting-container">
              <SelectComponent
                key={String(input.name)}
                errorMessage={errorMessage?.[input.name] || ""}
                inputName={String(input.name)}
                handleFunction={handleFunction}
                isDisabled={disabledInputsMap?.[String(input.name)] ?? false}
                options={options?.[input.name] ?? []}
                defaultOption={input.placeholder}
                selectValue={formData[input.name]}
              />

              {settingInputsMap?.[String(input.name)] && (
                <button
                  type="button"
                  className="setting-btn"
                  onClick={() => disabledToggle(String(input.name))}
                >
                  {disabledInputsMap[input.name] ? "Módosítás" : "Mentés"}
                </button>
              )}
            </div>
          </>
        ),
      )}
    </>
  );
}

export default InputForm;
