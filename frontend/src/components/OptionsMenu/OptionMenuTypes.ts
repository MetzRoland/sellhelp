export interface LinkInterface {
    url: string;
    label: string;
}

export interface OptionsMenuProps {
  isOpen: boolean;
  onClose: () => void;
  links?: LinkInterface[];
  children?: React.ReactNode;
  toggleRef?: React.RefObject<HTMLElement | null>;
}