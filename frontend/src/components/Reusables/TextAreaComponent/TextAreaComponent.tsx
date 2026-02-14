import type { TextareaComponentType } from "./TextAreaComponentTypes";

function TextareaComponent({
  errorMessage,
  inputName,
  inputValue,
  inputPlaceholder,
  isDisabled = false,
  rows = 12,
  cols,
  handleFunction,
}: TextareaComponentType) {
  return (
    <div className="input-container">
      {errorMessage && (
        <span className="message error error-span">{errorMessage}</span>
      )}
      <textarea
        name={inputName}
        value={inputValue || ""}
        placeholder={inputPlaceholder}
        onChange={handleFunction}
        disabled={isDisabled}
        rows={rows}
        cols={cols}
        className="input-element textarea-element"
      />
    </div>
  );
}

export default TextareaComponent;