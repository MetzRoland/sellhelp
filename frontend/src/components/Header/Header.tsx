import { Link } from "react-router";
import { useState, useRef } from "react";
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

  const profileToggleRef = useRef<HTMLDivElement>(null);

  const toggleMenu = (menu: string | null) => {
    setOpenMenu((prev) => (prev === menu ? null : menu));
  };

  const isAdmin = user?.role !== "ROLE_USER";

  const userOptionsLinks = [
    { url: "/users", label: "Felhasználók keresése" },
  ];

  const postOptionLinks = [
    { url: "/posts", label: "Posztok böngészése" },
    ...(isAuthenticated
      ? [
          { url: "/posts/new", label: "Poszt létrehozása" },
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
        {!isAuthenticated ? (
          <>
            <div className="left-options">
              <Link className="nav-link" to="/posts">
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
              {!isAdmin ? (
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
              <div
                className="dropdown-toggle-container"
                ref={profileToggleRef}
              >
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
                    {
                      url: "/home/settings",
                      label: "Felhasználói adatok",
                    },
                  ]}
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