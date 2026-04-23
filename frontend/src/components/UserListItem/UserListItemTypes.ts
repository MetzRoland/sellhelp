export interface UserListItemProps {
  userId: number;
  email: string;
  date?: string;
  message?: string;
  highlightLabel?: string;
  onActionClick?: (e: React.MouseEvent<HTMLButtonElement>) => void;
  actionText?: string;
  disableNavigation?: boolean;
  btnDisabled?: boolean;
  isChatMessage?: boolean;
  isMyChatMessage?: boolean;
  onClickNavigationLink?: string;
};