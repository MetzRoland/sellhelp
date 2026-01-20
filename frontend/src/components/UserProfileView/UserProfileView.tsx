import type { UserProfileProps } from "./UserProfileViewTypes";

import "./UserProfileView.css";

function UserProfileView({
  userAccount,
  handleUserBanning,
  adminMode,
  handleRedirectToProfile
}: UserProfileProps) {
  return (
    <div className="content-container user-profile-container" onClick={handleRedirectToProfile}>
      <div className="user-info-container">
        <img
          className="profile-picture-img"
          src={userAccount.profilePicture ?? "/images/profile.svg"}
          alt="Profile picture"
        />
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
            handleUserBanning(userAccount.id, userAccount.banned)
          }}
        >
          {!userAccount.banned ? "Fiók tiltása" : "Fiók engedélyezése"}
        </button>
      )}
    </div>
  );
}

export default UserProfileView;
