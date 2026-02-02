export interface SelectOption {
  id: number;
  label: string;
  value: string;
}

export interface SelectComponentType{
  errorMessage?: string,
  inputName: string,
  handleFunction: (e: React.ChangeEvent<HTMLSelectElement>) => void,
  options: SelectOption[],
  defaultOption: string,
  isDisabled?: boolean
  selectValue?: string
}