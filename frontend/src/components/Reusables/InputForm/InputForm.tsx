import InputComponent from "../InputComponent/InputComponent";
import SelectComponent from "../SelectComponent/SelectComponents";
import TextareaComponent from "../TextAreaComponent/TextAreaComponent";
import { useAuth } from "../../../contextProviders/AuthProvider/AuthContext";
import type { InputFormProps } from "./InputFormTypes";

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
          <p className="setting-label">{input.userTitle ? input.userTitle : input.placeholder}</p>
          <div className="setting-input-row">
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
                disabled={
                  user?.authProvider === "GOOGLE" && input.name === "email"
                }
                onClick={() => disabledToggle(input.name)}
              >
                {disabledInputsMap[String(input.name)] ? "Módosítás" : "Mentés"}
              </button>
            )}
          </div>
        </div>
      ))}
    </>
  );
}

export default InputForm;
