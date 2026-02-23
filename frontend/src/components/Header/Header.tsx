import { Link } from "react-router";
import { useState, useRef } from "react";
import { useAuth } from "../../contextProviders/AuthProvider/AuthContext";
import { useLoading } from "../../contextProviders/ProccessLoadProvider/ProccessLoadContext";
import ProfilePictureComponent from "../ProfilePictureComponent/ProfilePictureComponent";
import OptionsMenu from "../OptionsMenu/OptionsMenu";

import "./Header.css";

function Header() {
  const { isAuthenticated, logout, user } = useAuth();
  const { setIsLoading, setLoadingMessage } = useLoading();

  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const [isAuthPostsOpen, setIsAuthPostsOpen] = useState(false);
  const [isAuthProfileOptionsOpen, setIsAuthProfileOptionsOpen] = useState(false);

  const profileToggleRef = useRef<HTMLDivElement>(null);
  const postsToggleRef = useRef<HTMLDivElement>(null);
  const profileOptionsToggleRef = useRef<HTMLDivElement>(null);

  const userOptionsLinks = [
    {url: "/users", label: "Felhasználók keresése"}
  ];

  const postOptionLinks = [
    {url: "/posts", label: "Posztok böngészése"}
  ];

  if(isAuthenticated){
    postOptionLinks.push({url: "/posts/new", label: "Poszt létrehozása"});
    postOptionLinks.push({url: "/myposts", label: "Saját posztjaim"});
    postOptionLinks.push({url: "/posts/involved", label: "Elvállalt posztjaim"});
  }

  const toggleProfile = () => {
    setIsProfileOpen((prev) => !prev);
  };

  const togglePosts = () => {
    setIsAuthPostsOpen((prev) => !prev);
  };

  const handleLogout = async () => {
    setIsProfileOpen(false);
    setIsLoading(true);
    setLoadingMessage("Kijelentkezés...");
    await logout();
    setIsLoading(false);
    setLoadingMessage("");
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
                Regisztrálás
              </Link>
            </div>
          </>
        ) : (
          <>
            <div className="left-options">
              <div className="dropdown-toggle-container" ref={profileOptionsToggleRef}>
                <Link className="nav-link" to="#" onClick={() => setIsAuthProfileOptionsOpen((prev) => !prev)}>
                  Áttekintés
                </Link>

                <OptionsMenu
                  isOpen={isAuthProfileOptionsOpen}
                  onClose={() => setIsAuthProfileOptionsOpen(false)}
                  toggleRef={profileOptionsToggleRef}
                  links={userOptionsLinks}
                />
              </div>

              <div className="dropdown-toggle-container" ref={postsToggleRef}>
                <Link className="nav-link" to="#" onClick={togglePosts}>
                  Posztok keresése
                </Link>

                <OptionsMenu
                  isOpen={isAuthPostsOpen}
                  onClose={() => setIsAuthPostsOpen(false)}
                  toggleRef={postsToggleRef}
                  links={postOptionLinks}
                />
              </div>
            </div>

            <div className="title-option">
              <Link className="nav-link main-page-link" to="/">
                SellHelp
              </Link>
            </div>

            <div
              className="right-options right-options-profile"
            >
              <div className="dropdown-toggle-container" ref={profileToggleRef}>
                {user && (
                  <ProfilePictureComponent
                    userId={user.id}
                    handleOnClick={toggleProfile}
                  />
                )}

                <OptionsMenu
                  isOpen={isProfileOpen}
                  onClose={() => setIsProfileOpen(false)}
                  toggleRef={profileToggleRef}
                  links={[{ url: "/home/settings", label: "Felhasználói adatok" }]}
                >
                  <button
                    className="user-profile-option"
                    onClick={handleLogout}
                  >
                    Kijelentkezés
                  </button>
                </OptionsMenu>
              </div>
            </div>
          </>
        )}
      </nav>
    </header>
  );
}

export default Header;
