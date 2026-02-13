import type { Post } from "../PostsListComponent/PostsListComponentTypes";

export interface PostViewProps{
    post: Post;
    handleOnClick: () => void;
}