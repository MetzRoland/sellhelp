export interface InputComponentType{
    errorMessage?: string,
    inputType: string,
    inputName: string,
    inputValue?: string,
    inputPlaceholder: string,
    isDisabled?: boolean,
    handleFunction: (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => void
}