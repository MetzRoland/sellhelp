import { useRef } from "react";
import { Link } from "react-router";
import OptionsMenu from "../OptionsMenu/OptionsMenu";
import type { NavDropdownProps } from "./NavDropdownTypes";

function NavDropdown({
  label,
  menuKey,
  links,
  openMenu,
  toggleMenu,
}: NavDropdownProps) {
  const toggleRef = useRef<HTMLDivElement>(null);

  return (
    <div className="dropdown-toggle-container" ref={toggleRef}>
      <Link
        className="nav-link"
        onClick={() => toggleMenu(menuKey)}
        to="#"
      >
        {label}
      </Link>

      <OptionsMenu
        isOpen={openMenu === menuKey}
        onClose={() => toggleMenu(null)}
        toggleRef={toggleRef}
        links={links}
      />
    </div>
  );
}

export default NavDropdown;