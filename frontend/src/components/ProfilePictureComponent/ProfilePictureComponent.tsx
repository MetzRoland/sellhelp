import { useState, useEffect } from "react";
import { privateAxios } from "../../config/axiosConfig";
import type { ProfilePicture } from "./ProfilePictureComponentTypes";
import type { ProfilePictureComponentProps } from "./ProfilePictureComponentTypes";

function ProfilePictureComponent({ userId, handleOnClick, additionalSytleClass }: ProfilePictureComponentProps) {
  const [profilePicture, setProfilePicture] = useState<ProfilePicture>({
    profilePictureUrl: null,
  });

  const [ppLoading, setPpLoading] = useState<boolean>(true);

  useEffect(() => {
    const fetchProfilePicture = async () => {
      try {
        const response = await privateAxios.get(`/user/files/public/${userId}/pp`);

        setProfilePicture(response.data);
      } catch {
        setProfilePicture({
          profilePictureUrl: null,
        });
      } finally {
        setPpLoading(false);
      }
    };

    fetchProfilePicture();
  }, [userId]);

  return (
    <>
      {ppLoading ? (
        <div className={"profile-picture-skeleton " + additionalSytleClass} />
      ) : (
        <img
          className="profile-picture-img"
          src={profilePicture.profilePictureUrl || "/images/profile.svg"}
          alt="Profile picture"
          onClick={handleOnClick}
        />
      )}
    </>
  );
}

export default ProfilePictureComponent;
