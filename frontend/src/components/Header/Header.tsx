import { Link } from "react-router";
import { useEffect, useState, useRef } from "react";
import { useAuth } from "../../contextProviders/AuthProvider/AuthContext";
import { useLoading } from "../../contextProviders/ProccessLoadProvider/ProccessLoadContext";
import ProfilePictureComponent from "../ProfilePictureComponent/ProfilePictureComponent";

import "./Header.css";

function Header() {
  const { isAuthenticated, logout , user } = useAuth();
  const [isProfileOpen, setIsProfileOpen] = useState<boolean>(false);

  const { setIsLoading, setLoadingMessage } = useLoading();

  const profileRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        profileRef.current &&
        !profileRef.current.contains(event.target as Node)
      ) {
        setIsProfileOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  const handleProfileDropDown = () => {
    setIsProfileOpen((prev) => !prev);
  };

  if(user === null){
    return;
  }

  return (
    <header className="header">
      <nav className="header-nav">
        {!isAuthenticated ? (
          <>
            <div className="left-options">
              <Link className="nav-link" to="#">
                Posztok keresése
              </Link>
            </div>
            <div className="title-option">
              <Link className="nav-link main-page-link" to="/">
                SellHelp
              </Link>
            </div>
            <div className="right-options">
              <Link className="nav-link" to="/login">
                Bejelentkezés
              </Link>
              <Link className="nav-link" to="/register">
                Regiszrálás
              </Link>
            </div>
          </>
        ) : (
          <>
            <div className="left-options">
              <Link className="nav-link" to="#">
                Áttekintés
              </Link>
              <Link className="nav-link" to="#">
                Posztok keresése
              </Link>
            </div>
            <div className="title-option">
              <Link className="nav-link main-page-link" to="/">
                SellHelp
              </Link>
            </div>
            <div
              className="right-options right-options-profile"
              ref={profileRef}
            >
              <ProfilePictureComponent
                userId={user.id} 
                handleOnClick={handleProfileDropDown}
              />
              <div
                className={`profile-options-container ${
                  isProfileOpen ? "open" : "closed"
                }`}
              >
                <Link className="user-profile-option" to="/profile">
                  Felhasználói adatok
                </Link>
                <button
                  className="user-profile-option"
                  onClick={async () => {
                    handleProfileDropDown();
                    setIsProfileOpen(false);
                    setIsLoading(true);
                    setLoadingMessage("Kijelentkezés...");
                    await logout();
                    setIsLoading(false);
                    setLoadingMessage("");
                  }}
                >
                  Kijelentkezés
                </button>
              </div>
            </div>
          </>
        )}
      </nav>
    </header>
  );
}

export default Header;
