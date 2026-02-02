import type { InputComponentType } from "./InputComponentTypes";

function InputComponent({errorMessage, inputType, inputName, inputValue, inputPlaceholder, handleFunction, isDisabled}: InputComponentType){
    return (
        <div className="input-container">
              {errorMessage && (
                <span className="message error error-span">
                  {errorMessage}
                </span>
              )}
                <input
                  type={inputType}
                  name={inputName}
                  value={inputValue || ""}
                  placeholder={inputPlaceholder}
                  onChange={handleFunction}
                  disabled={isDisabled}
                  className="input-element"
                />
        </div>
    );
}

export default InputComponent;