import { Link } from "react-router";
import { useEffect, useState, useRef } from "react";
import { useAuth } from "../../contextProviders/AuthProvider/AuthContext";
import { useLoading } from "../../contextProviders/ProccessLoadProvider/ProccessLoadContext";
import { privateAxios } from "../../config/axiosConfig";
import "./Header.css";

function Header() {
  const { isAuthenticated, logout } = useAuth();
  const [profilePicture, setProfilePicture] = useState<string | null>(null);
  const [ppLoading, setPpLoading] = useState<boolean>(true);
  const [isProfileOpen, setIsProfileOpen] = useState<boolean>(false);

  const { setIsLoading, setLoadingMessage } = useLoading();

  const profileRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    if (!isAuthenticated) return;
    if (profilePicture) return;

    const cachedPp = localStorage.getItem("profilePicture");

    if (cachedPp) {
      setProfilePicture(cachedPp);
      setPpLoading(false);
      return;
    }

    const fetchProfilePicture = async () => {
      try {
        const response = await privateAxios.get("/user/files/pp");
        setProfilePicture(response.data);
        localStorage.setItem("profilePicture", response.data);
      } catch {
        setProfilePicture(null);
      } finally {
        setPpLoading(false);
      }
    };

    fetchProfilePicture();
  }, [isAuthenticated, profilePicture]);

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
              {ppLoading ? (
                <div className="profile-picture-skeleton" />
              ) : profilePicture ? (
                <img
                  className="profile-picture-img"
                  src={profilePicture}
                  alt="Profile picture"
                  onClick={handleProfileDropDown}
                />
              ) : (
                <img
                  className="profile-picture-img"
                  src="/images/profile.svg"
                  alt="Profile picture"
                  onClick={handleProfileDropDown}
                />
              )}
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
                  onClick={() => {
                    handleProfileDropDown();
                    setIsProfileOpen(false);
                    setIsLoading(true);
                    setLoadingMessage("Kijelentkezés...");
                    logout();
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
