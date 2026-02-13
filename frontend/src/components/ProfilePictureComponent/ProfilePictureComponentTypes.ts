export interface ProfilePicture{
    profilePictureUrl: string | null;
}

export interface ProfilePictureComponentProps{
    userId: number;
    handleOnClick?: (e: React.MouseEvent<HTMLImageElement>) => void;
    additionalSytleClass?: string;
}