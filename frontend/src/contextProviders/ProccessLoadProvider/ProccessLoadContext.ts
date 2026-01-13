import { createContext, useContext } from "react";
import type { ProccessLoadContextType } from "./ProccessLoadTypes";

export const ProccessLoadContext = createContext<ProccessLoadContextType | undefined>(undefined);

export function useLoading() {
    const context = useContext(ProccessLoadContext);

    if (!context) {
        throw new Error(
            "useLoading must be used within a ProccessLoadProvider"
        );
    }

    return context;
}
