import type { ProccessLoadProp } from "./ProccessLoadTypes";
import "./ProccessLoad.css";

function ProccessLoad({ message }: ProccessLoadProp){
    return (
        <div className="proccess-load-container">
            <div className="loader-content">
                <span className="spinner" />
                <p>{message ? message : "Kérjük várjon..."}</p>
            </div>
        </div>
    );
}

export default ProccessLoad;