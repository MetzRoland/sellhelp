import { Link } from "react-router";
import { useState, useRef, useEffect } from "react";
import { useAuth } from "../../contextProviders/AuthProvider/AuthContext";
import { useLoading } from "../../contextProviders/ProccessLoadProvider/ProccessLoadContext";
import ProfilePictureComponent from "../ProfilePictureComponent/ProfilePictureComponent";
import OptionsMenu from "../OptionsMenu/OptionsMenu";
import NavDropdown from "../NavDropdown/NavDropdown";

import "./Header.css";

function Header() {
  const { isAuthenticated, logout, user } = useAuth();
  const { setIsLoading, setLoadingMessage } = useLoading();

  const [openMenu, setOpenMenu] = useState<string | null>(null);
  // State for the hamburger menu
  const [isHamburgerOpen, setIsHamburgerOpen] = useState(false);

  const profileToggleRef = useRef<HTMLDivElement>(null);

  const hamburgerRef = useRef<HTMLButtonElement>(null);

  const [isHamburgerVisible, setIsHamburgerVisible] = useState(false);

  const toggleMenu = (menu: string | null) => {
    setOpenMenu((prev) => (prev === menu ? null : menu));
  };

  const toggleHamburger = () => {
    setIsHamburgerOpen(!isHamburgerOpen);
  };

  const isAdmin = user?.role !== "ROLE_USER";

  const userOptionsLinks = [{ url: "/users", label: "Felhasználók keresése" }];

  const postOptionLinks = [
    { url: "/posts", label: "Posztok böngészése" },
    ...(isAuthenticated
      ? [
          { url: "/posts/new", label: "Új poszt létrehozása" },
          { url: "/myposts", label: "Saját posztjaim" },
          { url: "/posts/involved", label: "Elvállalt posztjaim" },
        ]
      : []),
  ];

  const adminUserManagementLinks = [
    { url: "/userManagement", label: "Felhasználók kezelése" },
  ];

  const adminPostManagementLinks = [
    { url: "/postManagement", label: "Posztok kezelése" },
  ];

  useEffect(() => {
    const checkVisibility = () => {
      const el = hamburgerRef.current;
      if (!el) return;

      setIsHamburgerVisible(el.offsetParent !== null);
    };

    checkVisibility();
    window.addEventListener("resize", checkVisibility);

    return () => window.removeEventListener("resize", checkVisibility);
  }, []);

  const handleLogout = async () => {
    toggleMenu(null);
    setIsLoading(true);
    setLoadingMessage("Kijelentkezés...");
    await logout();
    setIsLoading(false);
    setLoadingMessage("");
  };

  return (
    <header className="header">
      <nav className="header-nav">
        {/* Hamburger Icon */}
        <button
          className="hamburger"
          ref={hamburgerRef}
          onClick={toggleHamburger}
        >
          <span className={`bar ${isHamburgerOpen ? "open" : ""}`}></span>
          <span className={`bar ${isHamburgerOpen ? "open" : ""}`}></span>
          <span className={`bar ${isHamburgerOpen ? "open" : ""}`}></span>
        </button>

        <div className={`left-options ${isHamburgerOpen ? "mobile-open" : ""}`}>
          {!isAuthenticated ? (
            <>
              <Link
                className="nav-link"
                to="/posts"
                onClick={() => setIsHamburgerOpen(false)}
              >
                Posztok keresése
              </Link>
              {isHamburgerVisible && (
                <>
                  <Link className="nav-link" to="/login">
                    Bejelentkezés
                  </Link>
                  <Link className="nav-link" to="/register">
                    Regisztrálás
                  </Link>
                </>
              )}
            </>
          ) : !isAdmin ? (
            <>
              <NavDropdown
                label="Áttekintés"
                menuKey="overview"
                links={userOptionsLinks}
                openMenu={openMenu}
                toggleMenu={toggleMenu}
              />
              <NavDropdown
                label="Posztok keresése"
                menuKey="posts"
                links={postOptionLinks}
                openMenu={openMenu}
                toggleMenu={toggleMenu}
              />
            </>
          ) : (
            <>
              <NavDropdown
                label="Felhasználói fiókok"
                menuKey="adminUsers"
                links={adminUserManagementLinks}
                openMenu={openMenu}
                toggleMenu={toggleMenu}
              />
              <NavDropdown
                label="Posztok kezelése"
                menuKey="adminPosts"
                links={adminPostManagementLinks}
                openMenu={openMenu}
                toggleMenu={toggleMenu}
              />
            </>
          )}
        </div>

        <div className="title-option">
          <Link className="nav-link main-page-link" to="/">
            SellHelp
          </Link>
        </div>

        <div className="right-options right-options-profile">
          {!isAuthenticated ? (
            <>
              {!isHamburgerVisible && (
                <>
                  <Link className="nav-link" to="/login">
                    Bejelentkezés
                  </Link>
                  <Link className="nav-link" to="/register">
                    Regisztrálás
                  </Link>
                </>
              )}
            </>
          ) : (
            <div className="dropdown-toggle-container" ref={profileToggleRef}>
              {user && (
                <ProfilePictureComponent
                  userId={user.id}
                  handleOnClick={() => toggleMenu("profile")}
                />
              )}
              <OptionsMenu
                isOpen={openMenu === "profile"}
                onClose={() => toggleMenu(null)}
                toggleRef={profileToggleRef}
                links={[
                  { url: "/home/settings", label: "Felhasználói adatok" },
                ]}
              >
                <button className="user-profile-option" onClick={handleLogout}>
                  Kijelentkezés
                </button>
              </OptionsMenu>
            </div>
          )}
        </div>
      </nav>
    </header>
  );
}

export default Header;
