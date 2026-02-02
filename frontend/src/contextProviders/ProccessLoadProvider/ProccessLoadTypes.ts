export interface ProccessLoadContextType{
    isLoading: boolean;
    setIsLoading: (isLoading: boolean) => void;
    loadingMessage: string;
    setLoadingMessage: (loadingMessage: string) => void;
}