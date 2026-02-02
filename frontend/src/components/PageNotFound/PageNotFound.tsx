import Header from "../Header/Header";
import Footer from "../Footer/Footer";

import "./PageNotFound.css";

interface PageNotFoundProps {
  message?: string;
}

function PageNotFound({ message }: PageNotFoundProps) {
  return (
    <>
      <Header />

      <div className="main-container page-not-found">
        <div className="content-container page-not-found-container">
          <p className="page-not-found-status">404</p>
          <p className="page-not-found-title">
            {message ? message : "Az oldal nem található!"}
          </p>
        </div>
      </div>

      <Footer />
    </>
  );
}

export default PageNotFound;
