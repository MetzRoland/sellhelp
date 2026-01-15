import Header from "../Header/Header";
import Footer from "../Footer/Footer";

import "./PageNotFound.css";

function PageNotFound(){
    return (
        <>
            <Header />

            <div className="main-container page-not-found">
                <div className="content-container page-not-found-container">
                    <p className="page-not-found-status">404</p>
                    <p className="page-not-found-title">Az oldal nem található!</p>
                </div>
            </div>

            <Footer />
        </>
    )
}

export default PageNotFound;