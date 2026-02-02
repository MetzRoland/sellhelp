import { useState } from "react";
import type { ProccessLoadContextType } from "./ProccessLoadTypes";
import { ProccessLoadContext } from "./ProccessLoadContext";
import ProccessLoad from "../../components/ProcessLoad/ProccessLoad";

export function ProccessLoadProvider({ children }: { children: React.ReactNode }) {
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [loadingMessage, setLoadingMessage] = useState("");

    const values: ProccessLoadContextType = {
        isLoading,
        setIsLoading,
        loadingMessage,
        setLoadingMessage
    };

    return (
        <ProccessLoadContext.Provider value={values}>
            {isLoading && <ProccessLoad message={loadingMessage}/>}
            {children}
        </ProccessLoadContext.Provider>
    );
}