import Header from "../Header/Header";
import Footer from "../Footer/Footer";

import "./Login.css";

function Login(){
    return (
        <>
            <Header />
            <div className="container">
                <h1 className="container-title">Bejelentkezés</h1>
            </div>
            <Footer />
        </>
    );
}

export default Login;