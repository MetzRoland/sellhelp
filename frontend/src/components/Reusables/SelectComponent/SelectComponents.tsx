import type { SelectComponentType } from "./SelectComponentTypes";

function SelectComponent({
  errorMessage,
  inputName,
  handleFunction,
  options,
  defaultOption,
  isDisabled,
  selectValue
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
        disabled={isDisabled}
      >
        <option value={selectValue ? selectValue : ""} id="default-select-option" disabled hidden>
          {selectValue ? selectValue : defaultOption}
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
