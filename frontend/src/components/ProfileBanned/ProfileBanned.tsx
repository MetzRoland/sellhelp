import { Link } from "react-router";
import Footer from "../Footer/Footer";
import Header from "../Header/Header";

import "./ProfileBanned.css";

function ProfileBanned() {
  return (
    <>
      <Header />

      <div className="main-container profile-banned-container">
        <div className="content-container profile-banned-content-container">
          <p className="message error error-process-status profile-banned-message">
            A fiókod tiltásra került!
          </p>
          <Link to="/" className="btn btn-highlight">
            Vissza a főoldalra
          </Link>
        </div>
      </div>

      <Footer />
    </>
  );
}

export default ProfileBanned;
