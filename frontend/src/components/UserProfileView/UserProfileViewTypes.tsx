import type { User } from "../../contextProviders/AuthProvider/AuthProviderTypes";

export interface UserProfileProps {
  userAccount: User;
  handleUserBanning: (userId: number, isBanned: boolean) => void;
  adminMode?: boolean;
  handleRedirectToProfile: () => void;
}