import { useState, useEffect } from "react";
import { privateAxios } from "../../config/axiosConfig";
import type { ProfilePicture } from "./ProfilePictureComponentTypes";
import type { ProfilePictureComponentProps } from "./ProfilePictureComponentTypes";

const profilePictureCache = new Map<number, ProfilePicture>();

function ProfilePictureComponent({ userId, handleOnClick, additionalSytleClass }: ProfilePictureComponentProps) {
  const [profilePicture, setProfilePicture] = useState<ProfilePicture>({
    profilePictureUrl: null,
  });

  const [ppLoading, setPpLoading] = useState<boolean>(true);

  useEffect(() => {
    if (!userId) return;

    const fetchProfilePicture = async () => {
      if (profilePictureCache.has(userId)) {
        setProfilePicture(profilePictureCache.get(userId)!);
        setPpLoading(false);
        return;
      }

      try {
        const response = await privateAxios.get(
          `/user/files/public/${userId}/pp`
        );

        const data = response.data;

        profilePictureCache.set(userId, data);

        setProfilePicture(data);
      } catch {
        setProfilePicture({ profilePictureUrl: null });
      } finally {
        setPpLoading(false);
      }
    };

    setPpLoading(true);
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
