import type { UserProfileProps } from "./UserProfileViewTypes";
import ProfilePictureComponent from "../ProfilePictureComponent/ProfilePictureComponent";

import "./UserProfileView.css";

function UserProfileView({
  userAccount,
  handleUserBanning,
  adminMode,
  handleRedirectToProfile,
}: UserProfileProps) {
  return (
    <div
      className="content-container user-profile-container"
      onClick={handleRedirectToProfile}
    >
      <div className="user-info-container">
        <ProfilePictureComponent userId={userAccount.id} />
        <div className="user-infos">
          <p className="user-profile-name">
            {userAccount.lastName} {userAccount.firstName}
          </p>
          <p className="user-profile-email">{userAccount.email}</p>
        </div>
      </div>
      {adminMode && (
        <button
          className="btn btn-highlight"
          onClick={(e: React.MouseEvent<HTMLButtonElement>) => {
            e.stopPropagation();
            
            if (adminMode && handleUserBanning) {
              handleUserBanning(userAccount.id, userAccount.banned);
            }
          }}
        >
          {!userAccount.banned ? "Fiók tiltása" : "Fiók engedélyezése"}
        </button>
      )}
    </div>
  );
}

export default UserProfileView;
