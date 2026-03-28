import { useEffect, useRef } from "react";
import { Link } from "react-router";
import type { OptionsMenuProps } from "./OptionMenuTypes";

import "./OptionsMenu.css";

function OptionsMenu({ isOpen, onClose, links = [], children, toggleRef }: OptionsMenuProps) {
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!isOpen) return;

    const handleClickOutside = (event: MouseEvent) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target as Node) &&
        !(toggleRef?.current?.contains(event.target as Node))
      ) {
        onClose();
      }
    };

    document.addEventListener("mousedown", handleClickOutside);

    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [isOpen, onClose, toggleRef]);

  useEffect(() => {
    if (!isOpen || !dropdownRef.current || !toggleRef?.current) return;

    const dropdown = dropdownRef.current;
    const toggle = toggleRef.current;
    const rect = dropdown.getBoundingClientRect();
    const toggleRect = toggle.getBoundingClientRect();

    let left = 0;

    const overflowRight = toggleRect.left + rect.width > window.innerWidth;

    if (overflowRight) {
      left = toggleRect.width - rect.width;
    }

    const overflowLeft = toggleRect.left + left < 0;
    
    if (overflowLeft) {
      left = -toggleRect.left;
    }

    dropdown.style.left = `${left}px`;
  }, [isOpen, toggleRef]);

  if (!isOpen) return null;

  return (
    <div ref={dropdownRef} className={`options-container open`}>
      {links.map((link, index) => (
        <Link key={index} to={link.url}>
          {link.label}
        </Link>
      ))}

      {children}
    </div>
  );
}

export default OptionsMenu;