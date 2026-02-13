import { useNavigate } from "react-router";
import type { PostViewProps } from "./PostViewTypes";
import ProfilePictureComponent from "../ProfilePictureComponent/ProfilePictureComponent";

import "./PostView.css";

function PostView({ post, handleOnClick }: PostViewProps){
    const navigate = useNavigate();

    const handleProfileClick = (userId: number) => {
        navigate(`/users/${userId}`);
    };

    return (
        <>
            <div className="content-container post-view-container" onClick={handleOnClick}>
                <h1>{post.title}</h1>

                <div className="post-details">
                    {post.description.length > 100 ? (
                        <p className="post-description">{post.description.slice(0, 100) + "..."}</p>
                    ) : (
                        <p className="post-description">{post.description}</p>
                    )}

                    {post.reward ? (
                        <p className="post-reward">{post.reward} Ft</p>
                    ) : (
                        <p className="post-reward">Nincs díj!</p>
                    )}

                    <div className="post-publisher">
                        <p>Poszt létrehozója:</p>

                        <div className="post-publisher-details">
                            <ProfilePictureComponent
                                userId={post.publisher.id}
                                additionalSytleClass="profile-picture-skeleton-small"
                                handleOnClick={(e: React.MouseEvent<HTMLImageElement>) => {
                                    e.stopPropagation();
                                    handleProfileClick(post.publisher.id);
                                }}
                            />
                            <p>{post.publisher.email}</p>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
}

export default PostView;