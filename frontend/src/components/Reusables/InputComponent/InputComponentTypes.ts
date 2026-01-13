export interface InputComponentType{
    errorMessage?: string,
    inputType: string,
    inputName: string,
    inputValue?: string,
    inputPlaceholder: string,
    handleFunction: (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => void
}