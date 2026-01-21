import type { SelectComponentType } from "./SelectComponentTypes";

function SelectComponent({
  errorMessage,
  inputName,
  handleFunction,
  options,
  defaultOption,
}: SelectComponentType) {
  return (
    <div className="input-container">
      {errorMessage && (
        <span className="message error error-span">{errorMessage}</span>
      )}
      <select
        name={inputName}
        id={inputName}
        className="input-element select-input-element"
        onChange={handleFunction}
        defaultValue=""
      >
        <option value="" className="default-select-option" disabled hidden selected>
          {defaultOption}
        </option>
        {options.map((option) => {
          return (
            <option key={option.id} value={option.value}>
              {option.label}
            </option>
          );
        })}
      </select>
    </div>
  );
}

export default SelectComponent;
