export interface TextareaComponentType {
  errorMessage?: string;
  inputName: string;
  inputValue?: string;
  inputPlaceholder: string;
  isDisabled?: boolean;
  rows?: number;
  cols?: number;
  handleFunction: (e: React.ChangeEvent<HTMLTextAreaElement>) => void;
}