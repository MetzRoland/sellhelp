export interface UserListProps {
  isAdmin?: boolean;
}

export interface UserAccountFilter {
  userName?: string;
  email?: string;
  city?: string;
  banned?: string;
  role?: string;
}

export interface UserInputField {
  name: keyof UserAccountFilter;
  type: "text" | "select";
  placeholder: string;
}

export interface UserRole {
  id: number;
  roleName: string;
}
