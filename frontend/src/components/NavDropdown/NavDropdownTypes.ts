export interface NavDropdownProps {
  label: string;
  menuKey: string;
  links: { url: string; label: string }[];
  openMenu: string | null;
  toggleMenu: (menu: string | null) => void;
};