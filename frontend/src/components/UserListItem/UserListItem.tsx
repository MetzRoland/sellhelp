import { useNavigate } from "react-router";
import ProfilePictureComponent from "../ProfilePictureComponent/ProfilePictureComponent";
import { formatDate } from "../Reusables/HelperFunctions/HelperFunctions";
import type { UserListItemProps } from "./UserListItemTypes";

import "./UserListItem.css";

function UserListItem({
  userId,
  email,
  date,
  message,
  highlightLabel,
  onActionClick,
  actionText,
  disableNavigation = false,
  btnDisabled = false
}: UserListItemProps) {
  const navigate = useNavigate();

  const handleContainerClick = () => {
    if (!disableNavigation) {
      navigate(`/users/${userId}`);
    }
  };

  return (
    <div className="user-list-item" onClick={handleContainerClick}>
      <div className="user-list-avatar">
        <ProfilePictureComponent
          additionalSytleClass="profile-picture-skeleton-small"
          userId={userId}
        />
      </div>

      <div className="user-list-content">
        <div className="user-list-header">
          <span className="user-list-author">
            {highlightLabel && (
              <span className="owner-span">{highlightLabel} </span>
            )}
            {email}
          </span>

          {date && (
            <span className="user-list-date">
              {formatDate(date)}
            </span>
          )}
        </div>

        {message && (
          <p className="user-list-message">{message}</p>
        )}
      </div>

      {actionText && onActionClick && (
        <button
          type="button"
          className="setting-btn"
          disabled={btnDisabled}
          onClick={(e) => {
            e.stopPropagation();
            onActionClick(e);
          }}
        >
          {actionText}
        </button>
      )}
    </div>
  );
}

export default UserListItem;
